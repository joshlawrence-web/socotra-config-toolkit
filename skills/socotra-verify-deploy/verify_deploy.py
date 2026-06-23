#!/usr/bin/env python3
"""
verify_deploy.py — pre-deploy verifier for Socotra EC plugin projects.

Catches the failures that otherwise only surface on deploy, so an LLM (or a
human) can iterate in a tight local loop instead of round-tripping to a tenant.

Three layers, fastest/most-certain first:

  tier1  COMPILE   Compile the plugin Java against the generated customer-config.jar
                   (+ core-datamodel + deps). Because Socotra realises config as
                   generated symbols, this also catches most "config reference"
                   typos: coverage/charge/table names, table key signatures and
                   column accessors, quote/segment/item types. Reuses the project's
                   own gradle `compileJava` (authoritative classpath) when possible,
                   else falls back to direct javac over build/*.jar.

  tier2-stale GUARD  customer-config.jar is built SERVER-SIDE from socotra-config/.
                     If the config tree is newer than the jar, the generated symbols
                     are stale and the compile result is not trustworthy. Warns and
                     tells you to refresh.

  tier2-untyped LINT Best-effort advisory for references the compiler genuinely
                     cannot see: untyped JsonNode string keys (.get("x") / .path("x")
                     / .has("x")). Flagged for manual review, never fails the build.

Exit code 0 = no compile errors (verdict PASS, warnings allowed).
Exit code 1 = compile errors (verdict FAIL).
Exit code 2 = could not run (project not found / no plugins / toolchain missing).

Usage:
    verify_deploy.py [PROJECT_DIR] [--json] [--no-gradle] [--quiet]
"""
from __future__ import annotations

import argparse
import json
import os
import re
import subprocess
import sys
import tempfile
from dataclasses import dataclass, field, asdict
from pathlib import Path

# --- javac diagnostic line: path:line: error|warning: message ---------------
_DIAG_RE = re.compile(r"^(?P<file>.+\.java):(?P<line>\d+): (?P<sev>error|warning): (?P<msg>.*)$")
# untyped JsonNode-style string access the compiler cannot verify
_UNTYPED_RE = re.compile(r"\.(?:get|path|has|findValue|findPath)\(\s*\"([^\"]+)\"\s*\)")
_SRC_JAR_RE = re.compile(r"-(sources|javadoc)\.jar$")
_CORE_DM_RE = re.compile(r"core-datamodel-v(\d+)\.(\d+)\.(\d+)\.jar$")


@dataclass
class Finding:
    tier: str            # tier1 | tier2-stale | tier2-untyped
    severity: str        # error | warning | info
    message: str
    file: str | None = None
    line: int | None = None


@dataclass
class Report:
    project: str
    mode: str = "unknown"            # gradle | javac | none
    verdict: str = "UNKNOWN"         # PASS | FAIL | ERROR
    findings: list[Finding] = field(default_factory=list)
    plugin_files: list[str] = field(default_factory=list)

    def add(self, **kw):
        self.findings.append(Finding(**kw))

    @property
    def errors(self):
        return [f for f in self.findings if f.severity == "error"]


# --------------------------------------------------------------------------- #
# project discovery
# --------------------------------------------------------------------------- #
def find_plugin_sources(root: Path) -> tuple[list[Path], list[Path]]:
    """Return (gradle_sources, loose_sources).

    gradle_sources : .java under src/main/java (compiled by `gradle compileJava`)
    loose_sources  : .java under socotra-config/plugins/java (older layout; not
                     covered by compileJava — we javac these directly)
    """
    gradle_src = sorted((root / "src" / "main" / "java").rglob("*.java")) if (root / "src" / "main" / "java").is_dir() else []
    loose_src = sorted((root / "socotra-config" / "plugins" / "java").rglob("*.java")) if (root / "socotra-config" / "plugins" / "java").is_dir() else []
    return gradle_src, loose_src


def classpath_jars(build_dir: Path) -> list[Path]:
    """All runtime jars in build/, excluding -sources/-javadoc and keeping only the
    single highest core-datamodel version (multiple accumulate over time)."""
    jars = [p for p in build_dir.glob("*.jar") if not _SRC_JAR_RE.search(p.name)]
    core = [p for p in jars if _CORE_DM_RE.search(p.name)]
    if len(core) > 1:
        def ver(p):
            m = _CORE_DM_RE.search(p.name)
            return tuple(int(x) for x in m.groups())
        newest = max(core, key=ver)
        jars = [p for p in jars if p not in core] + [newest]
    return jars


# --------------------------------------------------------------------------- #
# tier 2a — staleness guard
# --------------------------------------------------------------------------- #
def newest_mtime(path: Path) -> float:
    latest = 0.0
    for p in path.rglob("*"):
        if p.is_file():
            latest = max(latest, p.stat().st_mtime)
    return latest


def check_staleness(report: Report, root: Path):
    cfg = root / "socotra-config"
    jar = root / "build" / "customer-config.jar"
    if not cfg.is_dir():
        return
    if not jar.exists():
        report.add(tier="tier2-stale", severity="warning",
                   message="build/customer-config.jar missing — generated symbols unavailable; "
                           "run `./gradlew refreshReferenceDatamodel` to build it from socotra-config/.")
        return
    cfg_t = newest_mtime(cfg)
    jar_t = jar.stat().st_mtime
    if cfg_t > jar_t:
        days = (cfg_t - jar_t) / 86400
        report.add(tier="tier2-stale", severity="warning",
                   message=f"socotra-config/ is newer than customer-config.jar by {days:.0f}d — "
                           f"generated symbols may be STALE, so compile results are not authoritative. "
                           f"Run `./gradlew refreshReferenceDatamodel` (network) before trusting a PASS.")


# --------------------------------------------------------------------------- #
# tier 2b — untyped string-reference lint
# --------------------------------------------------------------------------- #
def lint_untyped(report: Report, sources: list[Path], root: Path):
    for src in sources:
        try:
            text = src.read_text(encoding="utf-8", errors="replace")
        except OSError:
            continue
        for i, line in enumerate(text.splitlines(), 1):
            stripped = line.lstrip()
            if stripped.startswith("//") or stripped.startswith("*"):
                continue
            for m in _UNTYPED_RE.finditer(line):
                report.add(tier="tier2-untyped", severity="info",
                           file=_rel(src, root), line=i,
                           message=f'untyped string key "{m.group(1)}" — not compile-checked; '
                                   f"verify it exists in the config/aux data manually.")


# --------------------------------------------------------------------------- #
# tier 1 — compile
# --------------------------------------------------------------------------- #
def parse_diagnostics(report: Report, output: str, root: Path):
    """Parse javac diagnostics. Folds the trailing `symbol:`/`location:` detail
    lines into the message (so `cannot find symbol` becomes actionable), and
    dedupes — gradle echoes each diagnostic more than once."""
    lines = output.splitlines()
    seen = {(f.tier, f.file, f.line, f.message, f.severity) for f in report.findings}
    found = 0
    for idx, line in enumerate(lines):
        m = _DIAG_RE.match(line.strip())
        if not m:
            continue
        found += 1
        f = m.group("file")
        try:
            f = _rel(Path(f), root)
        except ValueError:
            pass
        msg = m.group("msg")
        detail = []
        for nxt in lines[idx + 1: idx + 5]:
            s = nxt.strip()
            if s.startswith(("symbol:", "location:")):
                detail.append(re.sub(r"\s+", " ", s))
        if detail:
            msg = f"{msg} ({'; '.join(detail)})"
        key = ("tier1", f, int(m.group("line")), msg, m.group("sev"))
        if key in seen:
            continue
        seen.add(key)
        report.add(tier="tier1", severity=m.group("sev"),
                   file=f, line=int(m.group("line")), message=msg)
    return found


def compile_gradle(report: Report, root: Path) -> bool:
    """Run the project's own compileJava. Returns True if gradle actually ran the
    compiler (success or genuine compile errors); False if gradle could not
    configure (so the caller should fall back to javac)."""
    gradlew = root / "gradlew"
    if not gradlew.exists():
        return False
    proc = subprocess.run(
        [str(gradlew), "compileJava", "--offline", "--console=plain", "--rerun-tasks"],
        cwd=root, capture_output=True, text=True,
    )
    out = proc.stdout + "\n" + proc.stderr
    n = parse_diagnostics(report, out, root)
    if proc.returncode == 0:
        report.mode = "gradle"
        return True
    # Did the failure come from the compiler, or from gradle config (plugin not
    # resolvable offline, etc.)? If we parsed real javac diagnostics, trust them.
    if n > 0 or "Compilation failed" in out or "> Task :compileJava FAILED" in out:
        report.mode = "gradle"
        return True
    # gradle infra problem — let javac fallback try.
    report.add(tier="tier1", severity="info",
               message="gradle compileJava could not configure offline; falling back to direct javac. "
                       f"(gradle exit {proc.returncode})")
    return False


def compile_javac(report: Report, root: Path, sources: list[Path]) -> None:
    build_dir = root / "build"
    jars = classpath_jars(build_dir) if build_dir.is_dir() else []
    if not jars:
        report.add(tier="tier1", severity="error",
                   message="no jars in build/ to compile against — run `./gradlew refreshReferenceDatamodel` first.")
        return
    sep = ";" if os.name == "nt" else ":"
    cp = sep.join(str(j) for j in jars)
    multi = [p for p in jars if _CORE_DM_RE.search(p.name)]
    if multi:
        report.add(tier="tier1", severity="info",
                   message=f"javac fallback using {multi[0].name}; if the project pins a different "
                           f"datamodel version this may be inaccurate — prefer the gradle path.")
    with tempfile.TemporaryDirectory() as tmp:
        proc = subprocess.run(
            ["javac", "-cp", cp, "-d", tmp, *[str(s) for s in sources]],
            capture_output=True, text=True,
        )
        parse_diagnostics(report, proc.stdout + "\n" + proc.stderr, root)
    report.mode = "javac"


# --------------------------------------------------------------------------- #
def _rel(p: Path, root: Path) -> str:
    try:
        return str(p.resolve().relative_to(root.resolve()))
    except ValueError:
        return str(p)


def run(root: Path, use_gradle: bool) -> Report:
    report = Report(project=str(root))
    if not (root / "socotra-config").is_dir() and not (root / "build.gradle.kts").exists():
        report.verdict = "ERROR"
        report.add(tier="tier1", severity="error",
                   message=f"{root} doesn't look like a Socotra config project "
                           "(no socotra-config/ or build.gradle.kts).")
        return report

    gradle_src, loose_src = find_plugin_sources(root)
    all_src = gradle_src + loose_src
    report.plugin_files = [_rel(s, root) for s in all_src]
    if not all_src:
        report.verdict = "ERROR"
        report.add(tier="tier1", severity="error",
                   message="no plugin .java found under src/main/java or socotra-config/plugins/java.")
        return report

    # tier 2a — always cheap, run first so its warning frames the compile result
    check_staleness(report, root)

    # tier 1 — compile
    compiled_via_gradle = False
    if use_gradle and gradle_src and (root / "gradlew").exists():
        compiled_via_gradle = compile_gradle(report, root)
    if not compiled_via_gradle:
        # javac path: compile everything we found
        compile_javac(report, root, all_src)
    elif loose_src:
        # gradle handled src/main/java; loose plugins still need checking
        compile_javac(report, root, loose_src)

    # tier 2b — untyped lint
    lint_untyped(report, all_src, root)

    report.verdict = "FAIL" if report.errors else "PASS"
    return report


# --------------------------------------------------------------------------- #
def render_human(report: Report) -> str:
    lines = []
    icon = {"PASS": "✅", "FAIL": "❌", "ERROR": "⚠️", "UNKNOWN": "?"}[report.verdict]
    lines.append(f"{icon} VERDICT: {report.verdict}  (compile mode: {report.mode}, "
                 f"{len(report.plugin_files)} plugin file(s))")
    by_tier = {}
    for f in report.findings:
        by_tier.setdefault(f.tier, []).append(f)
    order = ["tier1", "tier2-stale", "tier2-untyped"]
    label = {"tier1": "COMPILE", "tier2-stale": "STALENESS", "tier2-untyped": "UNTYPED REFS"}
    for tier in order:
        fs = by_tier.get(tier)
        if not fs:
            continue
        lines.append(f"\n  [{label[tier]}]")
        for f in fs:
            loc = f"{f.file}:{f.line}  " if f.file and f.line else (f"{f.file}  " if f.file else "")
            sev = {"error": "ERR ", "warning": "WARN", "info": "note"}[f.severity]
            lines.append(f"    {sev}  {loc}{f.message}")
    if report.verdict == "PASS" and any(f.tier == "tier2-stale" for f in report.findings):
        lines.append("\n  ⚠ PASS is provisional: config is newer than the generated jar (see STALENESS).")
    return "\n".join(lines)


def main(argv=None):
    ap = argparse.ArgumentParser(description="Pre-deploy verifier for Socotra EC plugin projects.")
    ap.add_argument("project", nargs="?", default=".", help="project dir (default: cwd)")
    ap.add_argument("--json", action="store_true", help="emit JSON")
    ap.add_argument("--no-gradle", action="store_true", help="force direct javac, skip gradle")
    ap.add_argument("--quiet", action="store_true", help="suppress human summary (with --json)")
    args = ap.parse_args(argv)

    root = Path(args.project).expanduser().resolve()
    report = run(root, use_gradle=not args.no_gradle)

    if args.json:
        payload = asdict(report)
        payload["findings"] = [asdict(f) for f in report.findings]
        print(json.dumps(payload, indent=2))
    if not args.quiet:
        if args.json:
            print(render_human(report), file=sys.stderr)
        else:
            print(render_human(report))

    if report.verdict == "ERROR":
        return 2
    return 1 if report.verdict == "FAIL" else 0


if __name__ == "__main__":
    sys.exit(main())
