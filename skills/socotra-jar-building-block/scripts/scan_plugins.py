#!/usr/bin/env python3
"""Plugin registry scanner — adapted from VelocityConverter sdk_introspect.py.

Enumerates every interface in ``com.socotra.deployment.customer`` whose name ends
with ``Plugin`` inside a Socotra SDK project's compiled JARs, and emits a Markdown
catalog of the EXACT plugin surface for that config: every declared method (all
overloads, ``default`` vs ``abstract``, full parameter/return types) plus the
components of every request/response type referenced in those signatures.

Works for single- and multi-product configs alike: the header lists every detected
product stem and the core-datamodel version. NOTHING about the interface set, the
method names or the overload counts is assumed — different datamodel versions
generate different SPI shapes (e.g. older versions emit no ``statelessRate``
methods, and some configs lack CancellationPlugin/ConfigMigrationPlugin entirely);
everything below is read verbatim from javap.

Usage:
    python3 scan_plugins.py --project <sdk-project-dir> [--out plugins.md]
    python3 scan_plugins.py --customer-jar customer-config.jar \\
                            --datamodel-jar core-datamodel-v1.6.180.jar

JARs are discovered under <project>/build/ or at the project root.
Requires javap (any JDK) on PATH.
"""

from __future__ import annotations

import argparse
import sys
from pathlib import Path

from introspect import (
    CUSTOMER_PACKAGE,
    accessor_map,
    datamodel_version,
    detect_products,
    discover_jars,
    display_type,
    is_interface,
    list_customer_classes,
    make_classpath,
    parse_method_signatures,
    require_javap,
)

SOCOTRA_PREFIXES = ("com.socotra.",)


def _referenced_socotra_types(methods: list[dict]) -> list[str]:
    """Socotra-package types named in the signatures (params + returns), in first-use
    order — these are the request/response types worth expanding."""
    seen: list[str] = []
    for m in methods:
        for t in [*m["params"], m["return_type"]]:
            base = t.strip()
            # Strip generic wrapper to its arguments as well as the raw type.
            for cand in _flatten_generic(base):
                if cand.startswith(SOCOTRA_PREFIXES) and cand not in seen:
                    seen.append(cand)
    return seen


def _flatten_generic(t: str) -> list[str]:
    """'List<Foo<Bar>>' → ['List', 'Foo', 'Bar'] (raw type names only)."""
    out: list[str] = []
    for piece in t.replace("<", " ").replace(">", " ").replace(",", " ").split():
        piece = piece.strip("?& ")
        if piece and piece not in ("extends", "super"):
            out.append(piece)
    return out


def _signature_line(m: dict) -> str:
    mods = " ".join(m["modifiers"])
    params = ", ".join(display_type(p) for p in m["params"])
    ret = display_type(m["return_type"])
    return f"{mods + ' ' if mods else ''}{ret} {m['name']}({params})"


def scan(classpath: str, customer_jar: Path,
         product_filter: str | None) -> tuple[list[str], dict]:
    """Returns (detected product stems, {InterfaceName: entry}). The plugin
    interface set is shared across all products in the customer package; the
    optional --product filter only narrows the reported stem list."""
    top_level = list_customer_classes(customer_jar)
    products = detect_products(top_level)
    if product_filter is not None:
        if product_filter not in products:
            print(f"WARNING: --product {product_filter!r} not among detected "
                  f"products {products} — reporting it anyway.", file=sys.stderr)
        products = [product_filter]

    plugin_names = [n for n in top_level if n.endswith("Plugin")]
    catalog: dict[str, dict] = {}
    for name in plugin_names:
        fqcn = f"{CUSTOMER_PACKAGE}.{name}"
        if not is_interface(classpath, fqcn):
            continue  # only plugin *interfaces* form the registry surface
        methods = [
            m for m in parse_method_signatures(classpath, fqcn)
            if m["params"] or m["return_type"] != "void"
        ]
        catalog[name] = {
            "fqcn": fqcn,
            "methods": methods,
            "referenced": _referenced_socotra_types(methods),
        }
    return products, catalog


def render_markdown(products: list[str], catalog: dict, classpath: str,
                    customer_jar: Path, datamodel_jar: Path) -> str:
    lines: list[str] = []
    title = ", ".join(products) if products else "(no product stems detected)"
    dm_version = datamodel_version(datamodel_jar)
    method_count = sum(len(v["methods"]) for v in catalog.values())
    lines.append(f"# Socotra plugin surface — {title}")
    lines.append("")
    lines.append(f"Source JARs: `{customer_jar.name}` + `{datamodel_jar.name}`")
    lines.append(f"core-datamodel version: **{dm_version or 'unknown'}**")
    plural = "s" if len(products) != 1 else ""
    lines.append(f"Detected product{plural} ({len(products)}): "
                 + (", ".join(f"**{p}**" for p in products) or "**none**"))
    lines.append(f"Plugin interfaces: **{len(catalog)}** · declared methods: **{method_count}**")
    lines.append("")
    lines.append("Every signature below is read from the compiled JARs via `javap` — it is the")
    lines.append("exact method surface a plugin implementation for this config may override.")
    lines.append("The generated SPI shape (interface set, request-record overloads, presence of")
    lines.append("methods like `statelessRate`) varies with the core-datamodel version and the")
    lines.append("products defined; trust only what is listed here.")
    lines.append("")

    for name in sorted(catalog):
        entry = catalog[name]
        lines.append(f"## {name}")
        lines.append("")
        lines.append(f"`{entry['fqcn']}`")
        lines.append("")
        lines.append(f"### Methods ({len(entry['methods'])})")
        lines.append("")
        lines.append("```java")
        for m in entry["methods"]:
            lines.append(_signature_line(m) + ";")
        lines.append("```")
        lines.append("")

        expandable = [(t, accessor_map(classpath, t)) for t in entry["referenced"]]
        expandable = [(t, acc) for t, acc in expandable if acc]
        if expandable:
            lines.append("### Request / response types")
            lines.append("")
            for fqcn, acc in expandable:
                lines.append(f"#### `{display_type(fqcn)}`")
                lines.append("")
                lines.append(f"`{fqcn}`")
                lines.append("")
                lines.append("| accessor | returns |")
                lines.append("|---|---|")
                for accessor, ret in acc.items():
                    lines.append(f"| `{accessor}()` | `{display_type(ret)}` |")
                lines.append("")
    return "\n".join(lines) + "\n"


def main() -> int:
    ap = argparse.ArgumentParser(
        description="Scan a Socotra SDK project's JARs for the config's exact plugin surface.")
    ap.add_argument("--project", type=Path, default=None,
                    help="SDK project dir; jars found under build/ or the dir itself")
    ap.add_argument("--customer-jar", type=Path, default=None)
    ap.add_argument("--datamodel-jar", type=Path, default=None)
    ap.add_argument("--product", default=None,
                    help="Optional product-stem filter for the header (default: "
                         "report ALL detected products)")
    ap.add_argument("--out", type=Path, default=None,
                    help="Output Markdown file (default: stdout)")
    args = ap.parse_args()

    require_javap()
    customer_jar, datamodel_jar = discover_jars(args.project, args.customer_jar, args.datamodel_jar)
    classpath = make_classpath(customer_jar, datamodel_jar)

    products, catalog = scan(classpath, customer_jar, args.product)
    if not catalog:
        print("ERROR: no plugin interfaces found in the customer package — "
              "is this a built Socotra SDK project?", file=sys.stderr)
        return 1

    md = render_markdown(products, catalog, classpath, customer_jar, datamodel_jar)
    if args.out:
        args.out.parent.mkdir(parents=True, exist_ok=True)
        args.out.write_text(md, encoding="utf-8")
        method_count = sum(len(v["methods"]) for v in catalog.values())
        print(f"Wrote {args.out}")
        print(f"{len(catalog)} plugin interfaces · {method_count} methods · "
              f"products={','.join(products) or 'none'} · "
              f"datamodel=v{datamodel_version(datamodel_jar) or '?'}")
    else:
        print(md, end="")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
