#!/usr/bin/env python3
"""Rating-contract deriver — the config-side complement to the bedrock JAR scanners.

The bedrock skill's ``scan_plugins.py``/``build_catalog.py`` read the GENERATED JAR
(exact plugin overloads, reachable data paths). They cannot tell you the two facts a
rating plugin most often gets wrong, because those live in the ``socotra-config`` JSON:

  1. Which charges are LEGAL on which element  (the ``charges: [...]`` array per element).
     Returning a RatingItem for an (element, charge) pair the config never declared is
     rejected by ``Chargeable.validateRatingSet``.
  2. Each charge's HANDLING -> whether the RatingItem needs ``.rate(...)`` or ``.amount(...)``.
     ``handling: normal`` -> ``.rate()``; ``flat``/``retention`` -> ``.amount()``. Picking
     wrong throws at RatingItem construction ("amount is required for charge 'X'...").

This script walks a socotra-config directory tree and emits a per-product "rating
contract": the overloads to implement, every chargeable element with its legal charges
and the required RatingItem method, and the rate tables with their makeKey signatures.

It is config-only (no JDK needed). Names it emits for generated Java types
(``<Product>QuoteRequest``, ``ChargeType.<name>``, accessor methods) are DERIVED from
config naming conventions and must be CONFIRMED against the JAR with the bedrock
scanners before they are trusted — every such field is marked. Pass ``--catalog
catalog.json`` (output of the bedrock ``build_catalog.py``) to have the script verify
element accessor paths against the real generated surface instead of guessing.

Usage:
    python3 derive_rating_contract.py --config <socotra-config-dir> [--out contract.md]
    python3 derive_rating_contract.py --config <dir> --json contract.json
    python3 derive_rating_contract.py --config <dir> --catalog catalog.json   # cross-check accessors

Pure Python stdlib. A socotra-config dir is one containing products/, charges/, and
usually coverages/, exposures/, tables/ subdirectories (each component is a folder
holding a config.json).
"""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

# Charge handling -> the RatingItem builder method the platform requires.
# Source of truth: core-datamodel RatingItem canonical constructor (see bedrock
# references/coremodel-classes.md). normal -> rate; flat/retention -> amount.
HANDLING_METHOD = {
    "normal": ".rate(value)",
    "flat": ".amount(value)",
    "retention": ".amount(value)  # cancellation/retention charges only",
}
DEFAULT_HANDLING = "normal"      # ChargeType.handling() default when omitted in config
DEFAULT_INVOICING = "scheduled"  # ChargeType.invoicing() default

# Trailing modifiers on element references inside a "contents" array.
CONTENT_MODIFIERS = {
    "?": "optional (nullable child record -> null-check before charging)",
    "+": "repeating, 1+ (collection accessor -> iterate)",
    "*": "repeating, 0+ (collection accessor -> iterate)",
    "!": "required (non-Optional component)",
}


def _load_json(path: Path) -> dict:
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except Exception as exc:  # noqa: BLE001 - report and continue
        print(f"  ! could not parse {path}: {exc}", file=sys.stderr)
        return {}


def _camel(name: str) -> str:
    """Map a config component name to its generated Java member name (accessor / enum
    constant). The generator lower-cases a single leading capital (``PersonalVehicle`` ->
    ``personalVehicle``) but preserves a leading run of 2+ capitals verbatim, because those
    are acronyms (``GST`` -> ``GST``, ``CGLBodilyInjury`` -> ``CGLBodilyInjury``,
    ``PABodilyInjury`` -> ``PABodilyInjury``). Lower-casing the first letter of an acronym
    produces names the JAR never generates (``gST``, ``cGLBodilyInjury``)."""
    if not name:
        return name
    if len(name) >= 2 and name[0].isupper() and name[1].isupper():
        return name  # acronym / already-capitalized -> kept verbatim by the generator
    return name[0].lower() + name[1:]


def _strip_modifier(ref: str) -> tuple[str, str]:
    """Split a contents ref like 'Child?' or 'Classification+' into (base, modifier)."""
    if ref and ref[-1] in CONTENT_MODIFIERS:
        return ref[:-1], ref[-1]
    return ref, ""


class Config:
    """Parsed view of a socotra-config directory tree."""

    def __init__(self, root: Path):
        self.root = root
        self.charges = self._load_components("charges")
        self.products = self._load_components("products")
        self.coverages = self._load_components("coverages")
        self.exposures = self._load_components("exposures")
        self.policyLines = self._load_components("policyLines")
        self.tables = self._load_components("tables")
        # Lower-cased lookup so PascalCase refs match camelCase dirs. Policy lines are a
        # normal intermediate container between a product and its exposures (auto/commercial)
        # and are chargeable in their own right, so they resolve and recurse like exposures.
        self._elem_by_lower = {}
        for kind, store in (("exposure", self.exposures), ("coverage", self.coverages),
                            ("policyLine", self.policyLines)):
            for nm, cfg in store.items():
                self._elem_by_lower[nm.lower()] = (kind, nm, cfg)

    def _load_components(self, subdir: str) -> dict:
        base = self.root / subdir
        out = {}
        if not base.is_dir():
            return out
        for child in sorted(base.iterdir()):
            cfg_file = child / "config.json"
            if cfg_file.is_file():
                out[child.name] = _load_json(cfg_file)
        return out

    def lookup_element(self, ref_base: str):
        return self._elem_by_lower.get(ref_base.lower())

    # ---- product inheritance (extend chain) --------------------------------
    def resolved_product(self, name: str) -> dict:
        """Merge a product with its 'extend' ancestors. Child wins on scalars;
        contents/charges/data are unioned (child entries first)."""
        cfg = self.products.get(name, {})
        chain = []
        seen = set()
        cur = name
        while cur and cur in self.products and cur not in seen:
            seen.add(cur)
            chain.append(self.products[cur])
            cur = self.products[cur].get("extend")
        merged: dict = {}
        contents: list = []
        charges: list = []
        charges_declared = False  # did ANY layer in the extend chain declare a charges array?
        data: dict = {}
        # Walk parents-first so child overrides scalars; collect contents/charges/data.
        for layer in reversed(chain):
            for k, v in layer.items():
                if k not in ("contents", "charges", "data"):
                    merged[k] = v
        for layer in chain:  # child-first for union ordering
            for c in layer.get("contents", []) or []:
                if c not in contents:
                    contents.append(c)
            if "charges" in layer:
                charges_declared = True
                for c in layer.get("charges", []) or []:
                    if c not in charges:
                        charges.append(c)
            for k, v in (layer.get("data", {}) or {}).items():
                data.setdefault(k, v)
        merged["contents"] = contents
        if charges_declared:  # omit key entirely when undeclared -> element_charges flags it
            merged["charges"] = charges
        merged["data"] = data
        merged["_extends_chain"] = [n for n in seen]
        return merged


def charge_detail(cfg: Config, charge_name: str) -> dict:
    spec = cfg.charges.get(charge_name, {})
    handling = (spec.get("handling") or DEFAULT_HANDLING).lower()
    method = HANDLING_METHOD.get(handling, f".???(value)  # unknown handling '{handling}'")
    return {
        "name": charge_name,
        "configured": charge_name in cfg.charges,
        "category": spec.get("category", "?"),
        "handling": handling,
        "handling_explicit": "handling" in spec,
        "invoicing": (spec.get("invoicing") or DEFAULT_INVOICING),
        "ratingitem_method": method,
        "chargetype_constant": f"ChargeType.{_camel(charge_name)}",
    }


def element_charges(cfg: Config, element_cfg: dict) -> dict:
    """Return {'charges': [charge_detail...], 'declared': bool} for one element."""
    declared = element_cfg.get("charges")
    if declared is None:
        return {"charges": [], "declared": False}
    return {"charges": [charge_detail(cfg, c) for c in declared], "declared": True}


def walk_element_tree(cfg: Config, product_name: str, prod_cfg: dict) -> list:
    """Depth-first list of chargeable nodes under a product.
    Node = {kind, config_name, accessor_path, locator_accessor, modifier, charges...}.
    """
    nodes = []

    def visit(ref: str, parent_path: str, depth: int):
        base, mod = _strip_modifier(ref)
        found = cfg.lookup_element(base)
        if not found:
            nodes.append({
                "kind": "UNRESOLVED",
                "config_name": base,
                "accessor_path": f"{parent_path}.<{_camel(base)}?>",
                "locator_accessor": "n/a — unresolved ref",
                "modifier": CONTENT_MODIFIERS.get(mod, "single, required"),
                "note": "ref not found under coverages/, exposures/ or policyLines/ (may be a "
                        "sub-product or naming/case mismatch) — confirm via JAR",
                "charges": [],
                "charges_declared": False,
            })
            return
        kind, real_name, elem_cfg = found
        repeating = mod in ("+", "*")
        accessor = _camel(real_name)
        if repeating:
            accessor_path = f"{parent_path}.{accessor}s()[i]  # CONFIRM collection accessor+name via JAR"
        else:
            accessor_path = f"{parent_path}.{accessor}()"
        ch = element_charges(cfg, elem_cfg)
        nodes.append({
            "kind": kind,
            "config_name": real_name,
            "accessor_path": accessor_path,
            "locator_accessor": f"{accessor_path.split('  #')[0]}.locator()",
            "modifier": CONTENT_MODIFIERS.get(mod, "single, required"),
            "charges": ch["charges"],
            "charges_declared": ch["declared"],
        })
        if depth < 8:
            for child_ref in elem_cfg.get("contents", []) or []:
                visit(child_ref, accessor_path.split("  #")[0], depth + 1)

    # The product itself is chargeable (root locator = quote.locator()/segment.locator()).
    prod_ch = element_charges(cfg, prod_cfg)
    nodes.append({
        "kind": "product",
        "config_name": product_name,
        "accessor_path": "quote  (or segment)",
        "locator_accessor": "quote.locator()  /  segment.locator()",
        "modifier": "root element",
        "charges": prod_ch["charges"],
        "charges_declared": prod_ch["declared"],
    })
    for ref in prod_cfg.get("contents", []) or []:
        visit(ref, "quote", 1)
    return nodes


def table_detail(cfg: Config, name: str) -> dict:
    spec = cfg.tables.get(name, {})
    cols = spec.get("columns", {}) or {}
    key_cols = [c for c, meta in cols.items() if (meta or {}).get("isKey")]
    val_cols = [c for c, meta in cols.items() if not (meta or {}).get("isKey")]
    range_meta = spec.get("rangeStart") or spec.get("rangeEnd") or ("range" in (spec.get("type", "")))
    return {
        "name": name,
        "key_columns": key_cols,
        "value_columns": val_cols,
        "value_types": {c: (cols[c] or {}).get("dataType", "?") for c in val_cols},
        "selection_time_basis": spec.get("selectionTimeBasis", "?"),
        "is_range": bool(range_meta),
        "makekey": f"{name}.makeKey({', '.join(key_cols)})" if key_cols else f"{name}.makeKey(...)  # no isKey columns found",
    }


def _catalog_paths(catalog: dict | None) -> set[str]:
    """Flatten every reachable dot-path across all roots in a build_catalog.py JSON."""
    if not catalog:
        return set()
    paths: set[str] = set()
    for root in (catalog.get("roots") or {}).values():
        for p in (root.get("paths") or {}):
            paths.add(p)
    return paths


def _to_dotpath(accessor_path: str) -> str:
    """Normalize a contract accessor like 'quote.insured().termInsurance().locator()'
    to a build_catalog dot-path like 'insured.termInsurance'."""
    p = accessor_path.split("  #")[0].strip()
    for prefix in ("quote.", "segment.", "quote", "segment"):
        if p.startswith(prefix):
            p = p[len(prefix):]
            break
    p = p.replace("()", "").replace("[i]", "")
    p = p.strip(". ")
    if p.endswith(".locator"):
        p = p[: -len(".locator")]
    return p


def _verify_in_catalog(dotpath: str, paths: set[str]) -> bool:
    """A path is confirmed if it exists as a catalog path or as the prefix of one.
    Matched case-INSENSITIVELY: the contract's accessor names are derived from config
    naming, so a casing difference vs the JAR's real spelling is not a missing path — it
    would otherwise produce a false ✗ telling the user to 'fix' a correct accessor."""
    if not dotpath:
        return True  # root element (the product) — always present
    dl = dotpath.lower()
    return any(p.lower() == dl or p.lower().startswith(dl + ".") for p in paths)


def build_contract(cfg: Config, catalog: dict | None) -> dict:
    cat_paths = _catalog_paths(catalog)
    concrete = [n for n, c in cfg.products.items() if not c.get("abstract", False)]
    abstract = [n for n, c in cfg.products.items() if c.get("abstract", False)]
    products = []
    for name in concrete:
        prod_cfg = cfg.resolved_product(name)
        nodes = walk_element_tree(cfg, name, prod_cfg)
        if catalog is not None:
            for n in nodes:
                if n["kind"] == "product":
                    n["catalog_verified"] = True
                else:
                    n["catalog_verified"] = _verify_in_catalog(_to_dotpath(n["accessor_path"]), cat_paths)
        products.append({
            "name": name,
            "display_name": prod_cfg.get("displayName", name),
            "candidate_overloads": [
                f"rate({name}QuoteRequest request)",
                f"rate({name}QuickQuoteRequest request)",
                f"rate({name}Request request)  # segment/transaction level; request.segment() is Optional",
            ],
            "elements": nodes,
        })
    return {
        "config_dir": str(cfg.root),
        "concrete_products": concrete,
        "abstract_products": abstract,
        "charges_defined": {n: charge_detail(cfg, n) for n in sorted(cfg.charges)},
        "tables": [table_detail(cfg, n) for n in sorted(cfg.tables)],
        "products": products,
        "catalog_cross_checked": catalog is not None,
    }


# --------------------------------------------------------------------------- #
# Rendering
# --------------------------------------------------------------------------- #
def render_md(contract: dict) -> str:
    L = []
    a = L.append
    a("# Rating contract (derived from config)\n")
    a(f"Config: `{contract['config_dir']}`\n")
    a("> Java type names below (`<Product>QuoteRequest`, `ChargeType.x`, accessors) are "
      "DERIVED from config naming. **Confirm against the JAR** with the bedrock "
      "`scan_plugins.py` (exact overloads) and `build_catalog.py` (exact accessors) "
      "before relying on them.\n")

    a(f"**Concrete products** (each needs rate overloads): {', '.join(contract['concrete_products']) or '(none)'}")
    if contract["abstract_products"]:
        a(f"**Abstract base products** (not rated directly): {', '.join(contract['abstract_products'])}")
    a("")

    a("## Charges defined in this config\n")
    a("| Charge | category | handling | RatingItem method | ChargeType (confirm) |")
    a("|---|---|---|---|---|")
    for nm, d in contract["charges_defined"].items():
        exp = "" if d["handling_explicit"] else " *(default)*"
        a(f"| {nm} | {d['category']} | {d['handling']}{exp} | `{d['ratingitem_method']}` | `{d['chargetype_constant']}` |")
    a("\n*handling defaults to `normal` when omitted -> use `.rate()`.*\n")

    if contract["tables"]:
        a("## Rate tables (ResourceSelector lookups)\n")
        for t in contract["tables"]:
            kind = "range table" if t["is_range"] else "table"
            a(f"### {t['name']} ({kind}) — selectionTimeBasis `{t['selection_time_basis']}`")
            a(f"- key: `{t['makekey']}`")
            vt = ", ".join(f"{c}:{ty}" for c, ty in t["value_types"].items()) or "(none)"
            a(f"- value columns: {vt}")
            if t["is_range"]:
                a("- range table -> use `getRangeTable(...).getRecord(key, boundValue)` / `interpolate(...)`")
            a("- NOTE: generated column accessors usually return `String` — convert to BigDecimal yourself.\n")

    a("## Per-product rating contract\n")
    for p in contract["products"]:
        a(f"### Product: {p['name']}  ({p['display_name']})\n")
        a("Overloads to consider implementing (prune to what the JAR actually generates):")
        for o in p["candidate_overloads"]:
            a(f"- `{o}`")
        a("")
        has_cat = any("catalog_verified" in n for n in p["elements"])
        a("Chargeable elements and their LEGAL charges:\n")
        if has_cat:
            a("(`accessor?` column: ✓ = path confirmed in build_catalog.py (case-insensitive); "
              "✗ = could not confirm — verify the accessor name/path against the JAR)")
        hdr = "| Element | kind | locator | legal charges (-> method) |"
        if has_cat:
            hdr = "| Element | kind | accessor? | locator | legal charges (-> method) |"
        a(hdr)
        a("|---|---|---|---|" + ("---|" if has_cat else ""))
        for n in p["elements"]:
            if n["charges_declared"]:
                if n["charges"]:
                    charges = "<br>".join(
                        f"`{c['name']}` {c['handling']} -> `{c['ratingitem_method']}`" for c in n["charges"]
                    )
                else:
                    charges = "*(empty charges array — element bears no charges)*"
            else:
                charges = "⚠️ **no `charges` array in config** — cannot derive; confirm via JAR `Chargeable.charges()` / config review"
            loc = n.get("locator_accessor", "n/a")
            if has_cat:
                mark = {True: "✓", False: "✗"}.get(n.get("catalog_verified"), "—")
                a(f"| `{n['config_name']}` | {n['kind']} | {mark} | `{loc}` | {charges} |")
            else:
                a(f"| `{n['config_name']}` | {n['kind']} | `{loc}` | {charges} |")
        a("")
        # surface ambiguity prominently
        undeclared = [n["config_name"] for n in p["elements"] if not n["charges_declared"]]
        if undeclared:
            a(f"> ⚠️ Elements without a derivable charge set: {', '.join(undeclared)}. "
              "A missing `charges` array means this script cannot prove which charges are "
              "legal there. Confirm before emitting RatingItems for them.\n")
    a("## Before you write code\n")
    a("1. Run the bedrock `scan_plugins.py --project <dir>` to get the EXACT `rate(...)` "
      "overloads and request-record component names for this config.")
    a("2. Run the bedrock `build_catalog.py --project <dir>` to confirm the accessor "
      "paths above exist (e.g. `insured.termInsurance` -> the real return type).")
    a("3. Map each charge to `.rate()` vs `.amount()` using the handling column above — "
      "do not guess from the charge's name.")
    a("4. Emit at most one RatingItem per (elementLocator, chargeType) — duplicates throw.")
    return "\n".join(L)


def main() -> int:
    ap = argparse.ArgumentParser(description="Derive a rating contract from a socotra-config tree.")
    ap.add_argument("--config", required=True, help="path to the socotra-config directory")
    ap.add_argument("--out", help="write Markdown contract here (default: stdout)")
    ap.add_argument("--json", help="also write the raw contract as JSON here")
    ap.add_argument("--catalog", help="build_catalog.py JSON output, to cross-check accessor paths")
    args = ap.parse_args()

    root = Path(args.config).expanduser().resolve()
    if not root.is_dir():
        print(f"error: --config {root} is not a directory", file=sys.stderr)
        return 2
    if not (root / "products").is_dir() or not (root / "charges").is_dir():
        print(f"warning: {root} has no products/ or charges/ subdir — is this a socotra-config root?",
              file=sys.stderr)

    catalog = None
    if args.catalog:
        catalog = _load_json(Path(args.catalog).expanduser()) or None

    cfg = Config(root)
    contract = build_contract(cfg, catalog)

    md = render_md(contract)
    if args.out:
        Path(args.out).write_text(md, encoding="utf-8")
        print(f"wrote {args.out}")
    else:
        print(md)
    if args.json:
        Path(args.json).write_text(json.dumps(contract, indent=2), encoding="utf-8")
        print(f"wrote {args.json}", file=sys.stderr)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
