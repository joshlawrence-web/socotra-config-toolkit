#!/usr/bin/env python3
"""Offline linter for a socotra-config/ folder tree.

Encodes the validation rules in ../references/config-schema.md so a config can
be sanity-checked WITHOUT tenant credentials (the gradle `validateConfig` task
uploads the bundle to the tenant API and needs apiUrl + tenantLocator + PAT).

This is a pre-flight, not a replacement: it catches the documented failure
classes (types, name formats, dangling references, charge/invoicing rules,
numbering escapes). A clean lint still needs a remote `validateConfig` before
deploy. Exit 0 = clean, 1 = errors found, 2 = usage/IO problem.

Usage: python3 lint_config.py [path/to/socotra-config]
"""
import json
import re
import sys
from pathlib import Path

SECTIONS = {
    "accounts", "products", "policyLines", "exposureGroups", "exposures",
    "coverages", "coverageTerms", "charges", "installmentPlans",
    "numberingPlans", "tables",
}
# sections whose entities other entities may reference via contents
CONTAINABLE = {"policyLines", "exposureGroups", "exposures", "coverages"}
CHARGE_CATEGORIES = {"premium", "tax", "fee", "surcharge", "credit", "nonfinancial"}
# observed valid in deployed tenant configs: int, datetime (schema doc understates)
FIELD_TYPES = {"string", "decimal", "boolean", "date", "timestamp", "int", "datetime"}
PLATFORM_FIELDS = {
    "policyStartDate", "policyEndDate", "effectiveDate", "expirationDate",
    "policyNumber", "quoteNumber", "policyId", "status", "state",
    "cancellationDate", "createdAt", "updatedAt", "createdBy", "updatedBy",
    "totalPremium", "writtenPremium", "balance",
}
QUANT = re.compile(r"[!?*+]$")


def strip_quant(ref: str) -> str:
    return QUANT.sub("", ref)


class Lint:
    def __init__(self):
        self.errors, self.warnings = [], []

    def err(self, where, msg):
        self.errors.append(f"{where}: {msg}")

    def warn(self, where, msg):
        self.warnings.append(f"{where}: {msg}")


def load_tree(root: Path, lint: Lint):
    """{section: {Name: body}} plus the root config."""
    tree = {s: {} for s in SECTIONS}
    unlinted = []
    for section_dir in sorted(p for p in root.iterdir() if p.is_dir()):
        known = section_dir.name in SECTIONS
        if not known:
            # real configs carry many more sections (automations, documents,
            # delinquencyPlans, dataTypes, ...) — load for reference resolution,
            # lint nothing beyond JSON validity
            unlinted.append(section_dir.name)
            tree.setdefault(section_dir.name, {})
        for entity_dir in sorted(p for p in section_dir.iterdir() if p.is_dir()):
            cfg = entity_dir / "config.json"
            where = f"{section_dir.name}/{entity_dir.name}"
            if not cfg.is_file():
                lint.err(where, "missing config.json")
                continue
            try:
                body = json.loads(cfg.read_text())
            except json.JSONDecodeError as e:
                lint.err(where, f"invalid JSON: {e}")
                continue
            if entity_dir.name in body and isinstance(body[entity_dir.name], dict):
                lint.err(where, "body wrapped in its own name key — config.json must hold the entity body only")
            tree[section_dir.name][entity_dir.name] = body
    if unlinted:
        lint.warn("sections", f"present but not linted (JSON validity only): {', '.join(unlinted)}")
    return tree


def check_data_fields(where, data, custom_types, lint: Lint):
    if not isinstance(data, dict):
        lint.err(where, "data must be an object")
        return
    for name, spec in data.items():
        w = f"{where}.data.{name}"
        if name in PLATFORM_FIELDS:
            lint.err(w, "platform-managed field name — never define as custom data")
        if not isinstance(spec, dict):
            lint.err(w, "field spec must be an object")
            continue
        ftype = str(spec.get("type", ""))
        base = ftype.rstrip("?*+")
        if base == "integer":
            lint.err(w, 'type "integer" is not defined — use "decimal" (or "int")')
        elif base not in FIELD_TYPES and base not in custom_types:
            lint.err(w, f'unknown field type "{ftype}" (not built-in, no dataTypes/{base} entity)')
        for bound in ("min", "max"):
            if bound in spec and not isinstance(spec[bound], str):
                lint.err(w, f'"{bound}" must be a STRING (e.g. "0"), got {type(spec[bound]).__name__}')
        if "options" in spec and not isinstance(spec["options"], list):
            lint.err(w, '"options" must be an array')


def check_refs(where, refs, targets, kind, lint: Lint):
    """Every reference (quantifier stripped) must resolve to a defined entity."""
    if refs is None:
        return
    if not isinstance(refs, list):
        lint.err(where, f"{kind} must be an array")
        return
    for ref in refs:
        name = strip_quant(str(ref))
        if name not in targets:
            lint.err(where, f'{kind} references "{name}" — no such entity defined')


def check_term(where, body, lint: Lint):
    has_options, has_value = "options" in body, "value" in body
    if has_options == has_value:
        lint.err(where, "coverage term needs EITHER options OR value, not both/neither")
    if has_options:
        defaults = 0
        for key in body["options"]:
            k = key.lstrip("*")
            defaults += key.startswith("*")
            # documented failure is uppercase START (O_1000000); camelCase deploys fine
            if k and k[0].isupper():
                lint.err(f"{where}.options.{key}", "option key must start lowercase (uppercase start fails name-format validation)")
        if defaults > 1:
            lint.err(where, "more than one default (*) option")


def check_auto_created_terms(tree, lint: Lint):
    """A term referenced with ! needs a default (* option or free value)."""
    terms = tree["coverageTerms"]
    for section in ("products", "policyLines", "exposureGroups", "exposures", "coverages"):
        for name, body in tree[section].items():
            for ref in body.get("coverageTerms", []) or []:
                ref = str(ref)
                if not ref.endswith("!"):
                    continue
                tname = strip_quant(ref)
                term = terms.get(tname)
                if term and "options" in term and not any(k.startswith("*") for k in term["options"]):
                    lint.err(f"{section}/{name}", f'auto-created (!) term "{tname}" has no default (*) option')


def lint_config(root: Path) -> Lint:
    lint = Lint()
    root_cfg = root / "config.json"
    root_body = {}
    if not root_cfg.is_file():
        lint.err("config.json", "missing root config.json (global settings)")
    else:
        try:
            root_body = json.loads(root_cfg.read_text())
        except json.JSONDecodeError as e:
            lint.err("config.json", f"invalid JSON: {e}")
    for key in ("defaultCurrency", "defaultTimeZone", "defaultTermDuration", "defaultDurationBasis"):
        if key not in root_body:
            lint.err("config.json", f"missing required root key {key}")
    for key in ("contactRoles", "lossCategories"):
        for v in root_body.get(key, []) or []:
            if v != v.lower():
                lint.err(f"config.json.{key}", f'"{v}" must be lowercase')
    for k, v in root_body.items():
        if k in SECTIONS and isinstance(v, dict) and v:
            lint.err("config.json", f'root contains a "{k}" map — entities belong in {k}/<Name>/config.json folders, not the root file')

    tree = load_tree(root, lint)
    containable = {n for s in CONTAINABLE for n in tree[s]}
    custom_types = set(tree.get("dataTypes", {}))

    for section in SECTIONS:
        for name, body in tree[section].items():
            where = f"{section}/{name}"
            if section in ("accounts", "products", "policyLines", "exposureGroups", "exposures", "coverages") \
                    and not isinstance(body.get("abstract"), bool):
                lint.err(where, 'missing "abstract": false (or true for base templates)')
            if "data" in body:
                check_data_fields(where, body["data"], custom_types, lint)
            check_refs(where, body.get("contents"), containable, "contents", lint)
            check_refs(where, body.get("coverageTerms"), tree["coverageTerms"], "coverageTerms", lint)
            check_refs(where, body.get("charges"), tree["charges"], "charges", lint)

    for name, body in tree["products"].items():
        where = f"products/{name}"
        check_refs(where, body.get("eligibleAccountTypes"), tree["accounts"], "eligibleAccountTypes", lint)
        if body.get("defaultInstallmentPlan") and strip_quant(body["defaultInstallmentPlan"]) not in tree["installmentPlans"]:
            lint.err(where, f'defaultInstallmentPlan "{body["defaultInstallmentPlan"]}" — no such installment plan defined')
        if (body.get("numberingString") or body.get("numberingTrigger")) and not body.get("numberingPlan"):
            lint.err(where, "numberingString/numberingTrigger require a numberingPlan")
        if body.get("numberingPlan") and body["numberingPlan"] not in tree["numberingPlans"]:
            lint.err(where, f'numberingPlan "{body["numberingPlan"]}" — no such numbering plan defined')

    for name, body in tree["coverageTerms"].items():
        check_term(f"coverageTerms/{name}", body, lint)
    check_auto_created_terms(tree, lint)

    for name, body in tree["charges"].items():
        where = f"charges/{name}"
        cat = body.get("category")
        if cat is None:
            lint.err(where, "missing category")
        elif cat not in CHARGE_CATEGORIES:
            # deployed configs show extra categories (invoiceFee, nonFinancial) — warn, don't fail
            lint.warn(where, f'category "{cat}" outside documented set {sorted(CHARGE_CATEGORIES)} — verify against validateConfig (remap hints: commission→nonfinancial, discount→credit)')
        if body.get("handling", "normal") == "normal" and body.get("invoicing") in ("immediate", "next"):
            lint.err(where, f'invoicing "{body["invoicing"]}" is not allowed with handling "normal" — use handling "flat"')

    for name, body in tree["numberingPlans"].items():
        fmt = body.get("format", "")
        bad = re.sub(r"\\.|\{[a-z]+\}|#", "", fmt)
        if re.search(r"[A-Za-z]", bad):
            lint.err(f"numberingPlans/{name}", f'format "{fmt}" has unescaped literal characters — escape each with \\ (e.g. "\\G\\L-######")')

    # orphans: defined but referenced by nothing (warning — validateConfig may allow)
    referenced = set()
    for section in SECTIONS:
        for body in tree[section].values():
            for key in ("contents", "coverageTerms", "charges", "eligibleAccountTypes"):
                for ref in body.get(key, []) or []:
                    referenced.add(strip_quant(str(ref)))
    for section in ("coverages", "coverageTerms", "charges", "exposures", "policyLines", "exposureGroups"):
        for name in tree[section]:
            if name not in referenced:
                lint.warn(f"{section}/{name}", "defined but referenced by nothing (orphan)")
    return lint


def demo():
    """Self-check against a minimal in-memory fixture (assert-based)."""
    import tempfile
    with tempfile.TemporaryDirectory() as td:
        root = Path(td) / "socotra-config"
        def w(rel, obj):
            p = root / rel
            p.parent.mkdir(parents=True, exist_ok=True)
            p.write_text(json.dumps(obj))
        w("config.json", {"defaultCurrency": "USD", "defaultTimeZone": "UTC",
                          "defaultTermDuration": 12, "defaultDurationBasis": "months",
                          "contactRoles": ["Agent"]})
        w("accounts/Consumer/config.json", {"abstract": False, "data": {
            "age": {"type": "integer", "min": 0}}})
        w("products/Auto/config.json", {"abstract": False,
            "eligibleAccountTypes": ["Consumer"], "contents": ["Vehicle+"],
            "coverageTerms": ["Ded!"], "charges": ["Premium"],
            "defaultInstallmentPlan": "FullPay"})
        w("exposures/Vehicle/config.json", {"abstract": False, "contents": ["Liability!"]})
        w("coverages/Liability/config.json", {"abstract": False, "charges": ["Premium"]})
        w("coverageTerms/Ded/config.json", {"type": "deductible",
            "options": {"D500": {"value": 500}}})
        w("charges/Premium/config.json", {"category": "premium", "handling": "normal",
                                          "invoicing": "immediate"})
        lint = lint_config(root)
        blob = "\n".join(lint.errors)
        for expected in ("Agent", "integer", '"min" must be a STRING', "FullPay",
                         "lowercase", "no default (*) option", "not allowed with handling"):
            assert expected in blob, f"missed rule: {expected}\n{blob}"
        assert not any("Liability" in e for e in lint.errors)
    print("selftest OK")


def main():
    if len(sys.argv) > 1 and sys.argv[1] == "--selftest":
        demo()
        return 0
    root = Path(sys.argv[1] if len(sys.argv) > 1 else "socotra-config")
    if not root.is_dir():
        print(f"error: {root} is not a directory", file=sys.stderr)
        return 2
    lint = lint_config(root)
    for e in lint.errors:
        print(f"ERROR   {e}")
    for w in lint.warnings:
        print(f"warning {w}")
    print(f"\n{len(lint.errors)} error(s), {len(lint.warnings)} warning(s) — "
          + ("clean; still run ./gradlew validateConfig before deploy" if not lint.errors else "fix errors, then re-lint"))
    return 1 if lint.errors else 0


if __name__ == "__main__":
    sys.exit(main())
