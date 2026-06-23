#!/usr/bin/env python3
"""Shared javap-based JAR introspection library — adapted from VelocityConverter sdk_introspect.py.

Generalised for use against ANY Socotra SDK project's compiled JAR set: no
product-, Velocity- or document-pipeline-specific logic, single- or multi-product.
The authority is the compiled JAR pair (``customer-config.jar`` +
``core-datamodel-v*.jar``) — found under ``<project>/build/`` or at the project
root — read via ``javap``, so everything reported here is the exact surface the
JVM sees.

Used by the two CLI tools in this directory:

    scan_plugins.py  — enumerate every *Plugin interface and its full method surface
    build_catalog.py — BFS the product data model into a {root: {path: type}} catalog

Pure stdlib. Requires ``javap`` (any modern JDK) on PATH.
"""

from __future__ import annotations

import os
import re
import shutil
import subprocess
import sys
import zipfile
from collections import deque
from functools import lru_cache
from pathlib import Path

CUSTOMER_PACKAGE = "com.socotra.deployment.customer"
CUSTOMER_PREFIX = "com/socotra/deployment/customer/"

# Zero-arg methods that are structural noise in a *data* catalog (record
# boilerplate, builder plumbing, transport conversions) — not navigable fields.
NOISE_METHODS = frozenset({
    "toString", "hashCode", "getClass",
    "builder", "toBuilder", "toProto", "toTransport",
})


# ---------------------------------------------------------------------------
# Environment checks
# ---------------------------------------------------------------------------


def require_javap() -> None:
    """Fail fast with a clear message if javap is not on PATH."""
    if shutil.which("javap") is None:
        sys.exit(
            "ERROR: `javap` not found on PATH. These tools introspect compiled JARs "
            "via javap, which ships with every JDK. Install a JDK (e.g. "
            "`brew install openjdk`) or add your JDK's bin/ directory to PATH."
        )


# ---------------------------------------------------------------------------
# JAR discovery
# ---------------------------------------------------------------------------


def _version_key(jar: Path) -> tuple:
    """Sort key from a core-datamodel-vX.Y.Z.jar filename."""
    m = re.search(r"v(\d+)\.(\d+)\.(\d+)", jar.name)
    return tuple(int(g) for g in m.groups()) if m else (0, 0, 0)


def datamodel_version(jar: Path) -> str | None:
    """Version string parsed from a core-datamodel jar filename
    (core-datamodel-v1.6.180.jar → '1.6.180'), or None if unparseable."""
    m = re.search(r"v(\d+\.\d+\.\d+)", jar.name)
    return m.group(1) if m else None


def _jar_search_dirs(project_root: Path) -> list[Path]:
    """Directories searched for jars, in priority order: <project>/build/, then
    the project root itself (some configs ship jars at the top level)."""
    return [d for d in (project_root / "build", project_root) if d.is_dir()]


def default_datamodel_jar(project_root: Path) -> Path | None:
    """Newest core-datamodel-v*.jar under <project>/build/ or <project>/ root
    (excluding sources/javadoc jars)."""
    candidates = [
        j
        for d in _jar_search_dirs(project_root)
        for j in d.glob("core-datamodel-v*.jar")
        if "-sources" not in j.name and "-javadoc" not in j.name
    ]
    return max(candidates, key=_version_key) if candidates else None


def discover_jars(
    project: Path | None,
    customer_jar: Path | None = None,
    datamodel_jar: Path | None = None,
) -> tuple[Path, Path]:
    """Resolve (customer-config.jar, core-datamodel.jar) for a Socotra SDK project.

    Explicit paths win; otherwise search ``<project>/build/`` then ``<project>/``
    itself — configs ship jars in either layout. Exits with a clear message if
    either jar cannot be found."""
    if customer_jar is None:
        if project is None:
            sys.exit("ERROR: pass --project <dir> or an explicit --customer-jar.")
        for d in _jar_search_dirs(project):
            cand = d / "customer-config.jar"
            if cand.exists():
                customer_jar = cand
                break
        else:
            sys.exit(
                f"ERROR: customer-config.jar not found under {project}/build/ or {project}/.\n"
                "Run the SDK build first (e.g. `./gradlew build`), or pass --customer-jar."
            )
    if not customer_jar.exists():
        sys.exit(f"ERROR: customer jar not found: {customer_jar}")

    if datamodel_jar is None:
        if project is None:
            sys.exit("ERROR: pass --project <dir> or an explicit --datamodel-jar.")
        datamodel_jar = default_datamodel_jar(project)
    if datamodel_jar is None or not datamodel_jar.exists():
        sys.exit(
            "ERROR: no core-datamodel-v*.jar found under <project>/build/ or <project>/ "
            "(pass --datamodel-jar explicitly)."
        )
    return customer_jar, datamodel_jar


def make_classpath(*jars: Path) -> str:
    return os.pathsep.join(str(j) for j in jars)


# ---------------------------------------------------------------------------
# Class enumeration via zipfile (no JVM round-trip)
# ---------------------------------------------------------------------------


def list_customer_classes(customer_jar: Path, include_nested: bool = False) -> list[str]:
    """Simple names of classes in com.socotra.deployment.customer, from the JAR index.

    By default only top-level classes (inner classes containing '$' are skipped);
    with ``include_nested=True`` nested names are returned as ``Outer$Inner``."""
    names: list[str] = []
    with zipfile.ZipFile(customer_jar) as zf:
        for entry in zf.namelist():
            if not entry.startswith(CUSTOMER_PREFIX) or not entry.endswith(".class"):
                continue
            rel = entry[len(CUSTOMER_PREFIX):-6]
            if "/" in rel:
                continue  # sub-package — not the flat customer package
            if "$" in rel and not include_nested:
                continue
            names.append(rel)
    return sorted(names)


# ---------------------------------------------------------------------------
# JAR introspection via javap
# ---------------------------------------------------------------------------


@lru_cache(maxsize=None)
def _javap(classpath: str, fqcn: str, public_only: bool = True) -> tuple[int, str]:
    """Run javap on a class; return (returncode, stdout+stderr). $ marks nested types.

    Memoised per (classpath, fqcn) within a process — the BFS walks the same
    types repeatedly."""
    cmd = ["javap", "-classpath", classpath]
    if public_only:
        cmd.append("-public")
    cmd.append(fqcn)
    proc = subprocess.run(cmd, capture_output=True, text=True)
    return proc.returncode, proc.stdout + proc.stderr


def _class_exists(classpath: str, fqcn: str) -> bool:
    rc, _ = _javap(classpath, fqcn)
    return rc == 0


def is_interface(classpath: str, fqcn: str) -> bool:
    """True iff javap reports the type as an interface."""
    rc, out = _javap(classpath, fqcn)
    if rc != 0:
        return False
    for line in out.splitlines():
        if line.lstrip().startswith(("public ", "interface ", "abstract ", "final ")) and "{" in line:
            return re.search(r"\binterface\s", line) is not None
    return False


_ZERO_ARG_METHOD_RE = re.compile(
    r"^\s*public\s+(?:static\s+|final\s+|abstract\s+|default\s+)*"
    r"(?:<[^>]*>\s+)?"
    r"([\w.$<>?,\s\[\]]+?)\s+"   # return type
    r"(\w+)\(\s*\)\s*;"          # zero-arg method name
)


@lru_cache(maxsize=None)
def _zero_arg_methods(classpath: str, fqcn: str) -> dict[str, str]:
    """Map {methodName: returnTypeString} for public zero-arg accessors.

    When a name has covariant/bridge overloads (e.g. record `data()`), prefer
    the most specific (non java.lang.Object) return type."""
    rc, out = _javap(classpath, fqcn)
    methods: dict[str, str] = {}
    if rc != 0:
        return methods
    for line in out.splitlines():
        m = _ZERO_ARG_METHOD_RE.match(line)
        if not m:
            continue
        ret, name = m.group(1).strip(), m.group(2)
        if name in methods and methods[name] != "java.lang.Object":
            continue  # keep the more specific return type already recorded
        methods[name] = ret
    return methods


def accessor_map(classpath: str, fqcn: str, drop_noise: bool = True) -> dict[str, str]:
    """Zero-arg accessors of a type, optionally with record/builder noise removed."""
    methods = _zero_arg_methods(classpath, fqcn)
    if not drop_noise:
        return dict(methods)
    return {n: r for n, r in methods.items() if n not in NOISE_METHODS}


# ---------------------------------------------------------------------------
# Full method-signature parsing (any arity — for the plugin scanner)
# ---------------------------------------------------------------------------

_METHOD_LINE_RE = re.compile(
    r"^\s*public\s+"
    r"((?:(?:abstract|default|static|final|synchronized|native)\s+)*)"  # modifiers
    r"(?:<[^>]*>\s+)?"                                                  # type params
    r"([\w.$<>?,\s\[\]]+?)\s+"                                          # return type
    r"(\w+)\s*\(([^)]*)\)\s*"                                           # name(params)
    r"(?:throws\s+[^;]+)?;"
)


def _split_params(params: str) -> list[str]:
    """Split a javap parameter list on top-level commas (generics-safe)."""
    parts: list[str] = []
    depth = 0
    current = ""
    for ch in params:
        if ch == "<":
            depth += 1
        elif ch == ">":
            depth -= 1
        elif ch == "," and depth == 0:
            parts.append(current.strip())
            current = ""
            continue
        current += ch
    if current.strip():
        parts.append(current.strip())
    return parts


def parse_method_signatures(classpath: str, fqcn: str) -> list[dict]:
    """All public method signatures of a type, as javap declares them.

    Returns a list of ``{modifiers, return_type, name, params}`` dicts in
    declaration order; overloads are preserved as separate entries. Fields,
    constructors and static initialisers are skipped."""
    rc, out = _javap(classpath, fqcn)
    methods: list[dict] = []
    if rc != 0:
        return methods
    for line in out.splitlines():
        m = _METHOD_LINE_RE.match(line)
        if not m:
            continue
        modifiers = m.group(1).split()
        methods.append({
            "modifiers": modifiers,
            "return_type": m.group(2).strip(),
            "name": m.group(3),
            "params": _split_params(m.group(4)),
        })
    return methods


# ---------------------------------------------------------------------------
# Type helpers
# ---------------------------------------------------------------------------


def _unwrap_type(ret: str) -> str | None:
    """Strip Optional<>/Collection<>/List<>/Set<>/Iterable<> wrappers; return the
    inner FQCN, or None for primitives / java.lang scalars that cannot be
    navigated further."""
    inner = ret.strip()
    m = re.match(r"[\w.]*(?:Optional|Collection|List|Set|Iterable)<(.+)>$", inner)
    if m:
        inner = m.group(1).strip()
    inner = re.sub(r"<.*>$", "", inner).strip()
    if "." not in inner or inner.startswith("java."):
        return None
    return inner


def _short_name(fqcn: str) -> str:
    """Human-friendly class name: drop package + outer$nested prefix."""
    return fqcn.rsplit(".", 1)[-1].rsplit("$", 1)[-1]


def display_type(fqcn: str) -> str:
    """Readable type name: socotra packages shortened, nested '$' shown as '.',
    generic arguments shortened recursively. Non-socotra types keep their FQCN."""
    def _one(t: str) -> str:
        t = t.strip()
        m = re.match(r"^([\w.$]+)<(.+)>$", t)
        if m:
            outer, args = m.group(1), m.group(2)
            inner = ", ".join(_one(a) for a in _split_params(args))
            return f"{_one(outer)}<{inner}>"
        if t.startswith((CUSTOMER_PACKAGE + ".", "com.socotra.coremodel.",
                         "com.socotra.deployment.", "com.socotra.platform.")):
            return t.rsplit(".", 1)[-1].replace("$", ".")
        if t.startswith("java.lang."):
            return t.rsplit(".", 1)[-1]
        return t.replace("$", ".")
    return _one(fqcn)


# ---------------------------------------------------------------------------
# Product / root detection
# ---------------------------------------------------------------------------


def detect_products(top_level_names: list[str]) -> list[str]:
    """Derive ALL product stems from generated class names — a config may define
    several products in one customer package (e.g. BasicCreditCardProtection +
    PremiumCreditCardProtection alongside single-product ZenCover).

    A candidate stem is any ``{Stem}Quote`` class (``*QuickQuote`` excluded — its
    stem is bogus). Exposure types also generate ``{Stem}Quote`` classes
    (ItemQuote, FraudProtectionQuote, …), so when stronger product signals exist
    we keep only stems that carry one, in order of strength:

    1. ``{Stem}Product`` exists  — the generated product marker class;
    2. ``{Stem}Segment`` exists  — exposures never get segments.

    If neither signal appears for any stem, all ``{Stem}Quote`` stems are returned
    (a Segment/QuickQuote is NOT required — some configs lack them). Sorted,
    possibly empty."""
    names = set(top_level_names)
    quote_stems = sorted(
        n[: -len("Quote")]
        for n in names
        if n.endswith("Quote") and not n.endswith("QuickQuote") and len(n) > len("Quote")
    )
    for marker in ("Product", "Segment"):
        marked = [s for s in quote_stems if f"{s}{marker}" in names]
        if marked:
            return marked
    return quote_stems


def detect_product(top_level_names: list[str]) -> str | None:
    """Back-compat single-product helper: the sole detected stem, else None."""
    stems = detect_products(top_level_names)
    return stems[0] if len(stems) == 1 else None


def default_roots(
    classpath: str,
    products: list[str],
    top_level_names: list[str] | None = None,
) -> list[str]:
    """Conventional root FQCNs that actually exist, unioned over ALL products:
    {Stem}Quote, {Stem}Segment, {Stem}Policy, {Stem}QuickQuote — whichever exist —
    plus any top-level ``*Account`` class (account types are not product-stem
    named: PersonalAccount, BankCustomerAccount, …)."""
    roots: list[str] = []
    for stem in products:
        for suffix in ("Quote", "Segment", "Policy", "QuickQuote"):
            fqcn = f"{CUSTOMER_PACKAGE}.{stem}{suffix}"
            if fqcn not in roots and _class_exists(classpath, fqcn):
                roots.append(fqcn)
    for name in top_level_names or []:
        if name.endswith("Account") and "$" not in name:
            fqcn = f"{CUSTOMER_PACKAGE}.{name}"
            if fqcn not in roots and _class_exists(classpath, fqcn):
                roots.append(fqcn)
    return roots


# ---------------------------------------------------------------------------
# Schema BFS
# ---------------------------------------------------------------------------


def build_schema_index(classpath: str, root_fqcns: list[str], max_depth: int = 3) -> dict:
    """BFS from each root; collect {SimpleTypeName: {fqcn, fields}} for all
    reachable non-primitive types up to max_depth hops."""
    index: dict[str, dict] = {}
    queue: deque[tuple[str, int]] = deque((fqcn, 0) for fqcn in root_fqcns)
    visited: set[str] = set()
    while queue:
        fqcn, depth = queue.popleft()
        if fqcn in visited or depth > max_depth:
            continue
        visited.add(fqcn)
        simple = fqcn.rsplit(".", 1)[-1].rsplit("$", 1)[-1]
        methods = accessor_map(classpath, fqcn)
        if not methods:
            continue
        index[simple] = {"fqcn": fqcn, "fields": {}}
        for name, ret in methods.items():
            index[simple]["fields"][name] = {"return_type": ret}
            inner = _unwrap_type(ret)
            if inner and inner not in visited:
                queue.append((inner, depth + 1))
    return index


def build_path_catalog(classpath: str, root_fqcn: str, max_depth: int = 3) -> dict[str, str]:
    """BFS from one root over zero-arg accessors, unwrapping Optional/Collection/
    List, producing a flat {dotPath: returnType} map of every reachable field
    path up to ``max_depth`` segments. Cycles are cut by never re-expanding a
    type already on the current ancestor chain."""
    paths: dict[str, str] = {}
    # (path-prefix, type, depth, ancestor chain)
    queue: deque[tuple[str, str, int, tuple[str, ...]]] = deque(
        [("", root_fqcn, 0, (root_fqcn,))]
    )
    while queue:
        prefix, fqcn, depth, chain = queue.popleft()
        for name, ret in sorted(accessor_map(classpath, fqcn).items()):
            path = f"{prefix}.{name}" if prefix else name
            paths[path] = ret
            inner = _unwrap_type(ret)
            if inner and inner not in chain and depth + 1 < max_depth:
                queue.append((path, inner, depth + 1, chain + (inner,)))
    return paths
