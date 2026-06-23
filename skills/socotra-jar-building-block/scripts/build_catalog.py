#!/usr/bin/env python3
"""Data-path catalog builder — adapted from VelocityConverter sdk_introspect.py / build_schema_index.py.

BFS the compiled data model from its root types — for EVERY detected product:
{Stem}Quote, {Stem}Segment, {Stem}Policy, {Stem}QuickQuote (whichever exist), plus
any top-level *Account class — over zero-arg accessors, unwrapping
Optional<T>/Collection<T>/List<T>, and emit a JSON catalog of every reachable
dot-path and its return type. This gives an LLM the complete navigable field
surface of ANY Socotra SDK project's data model, single- or multi-product.

Usage:
    python3 build_catalog.py --project <sdk-project-dir> [--depth 3] [--out catalog.json]
    python3 build_catalog.py --project <dir> --product BasicCreditCardProtection
    python3 build_catalog.py --project <dir> --root ZenCoverQuote --root ZenCoverSegment
    python3 build_catalog.py --customer-jar customer-config.jar \\
                             --datamodel-jar core-datamodel-v1.6.180.jar

JARs are discovered under <project>/build/ or at the project root.
Requires javap (any JDK) on PATH.
"""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

from introspect import (
    CUSTOMER_PACKAGE,
    _class_exists,
    build_path_catalog,
    datamodel_version,
    default_roots,
    detect_products,
    discover_jars,
    list_customer_classes,
    make_classpath,
    require_javap,
)


def main() -> int:
    ap = argparse.ArgumentParser(
        description="Build a {root: {dotPath: returnType}} catalog from a Socotra SDK project's JARs.")
    ap.add_argument("--project", type=Path, default=None,
                    help="SDK project dir; jars found under build/ or the dir itself")
    ap.add_argument("--customer-jar", type=Path, default=None)
    ap.add_argument("--datamodel-jar", type=Path, default=None)
    ap.add_argument("--product", default=None,
                    help="Optional product-stem filter (default: roots for ALL "
                         "detected products)")
    ap.add_argument("--root", action="append", default=None, metavar="SimpleName",
                    help="Root type simple name (repeatable); overrides detection. "
                         "Default: union over all detected products of "
                         "{Stem}Quote/{Stem}Segment/{Stem}Policy/{Stem}QuickQuote "
                         "plus any *Account class — whichever exist.")
    ap.add_argument("--depth", type=int, default=3,
                    help="Max path depth in segments (default: 3)")
    ap.add_argument("--out", type=Path, default=None,
                    help="Output JSON file (default: stdout)")
    args = ap.parse_args()

    require_javap()
    customer_jar, datamodel_jar = discover_jars(args.project, args.customer_jar, args.datamodel_jar)
    classpath = make_classpath(customer_jar, datamodel_jar)

    top_level = list_customer_classes(customer_jar)
    products = detect_products(top_level)
    if args.product is not None:
        if args.product not in products:
            print(f"WARNING: --product {args.product!r} not among detected "
                  f"products {products} — using it anyway.", file=sys.stderr)
        products = [args.product]

    # Resolve root FQCNs: explicit --root simple names win, else convention-detected.
    if args.root:
        root_fqcns: list[str] = []
        for simple in args.root:
            fqcn = simple if "." in simple else f"{CUSTOMER_PACKAGE}.{simple}"
            if not _class_exists(classpath, fqcn):
                print(f"ERROR: root type not found on classpath: {fqcn}", file=sys.stderr)
                return 1
            root_fqcns.append(fqcn)
    else:
        if not products:
            print("ERROR: could not detect any product stems (no {Stem}Quote "
                  "classes) — pass --product or --root.", file=sys.stderr)
            return 1
        root_fqcns = default_roots(classpath, products, top_level)
        if not root_fqcns:
            print(f"ERROR: no conventional root types found for products {products} "
                  "(tried Quote/Segment/Policy/QuickQuote suffixes and *Account) — "
                  "pass --root.", file=sys.stderr)
            return 1

    roots: dict[str, dict] = {}
    for fqcn in root_fqcns:
        simple = fqcn.rsplit(".", 1)[-1]
        roots[simple] = {
            "fqcn": fqcn,
            "paths": build_path_catalog(classpath, fqcn, max_depth=args.depth),
        }

    catalog = {
        "generated_by": "socotra-jar-building-block/scripts/build_catalog.py",
        "customer_jar": customer_jar.name,
        "datamodel_jar": datamodel_jar.name,
        "datamodel_version": datamodel_version(datamodel_jar),
        "products": products,
        "depth": args.depth,
        "roots": roots,
    }

    text = json.dumps(catalog, indent=2, sort_keys=False) + "\n"
    if args.out:
        args.out.parent.mkdir(parents=True, exist_ok=True)
        args.out.write_text(text, encoding="utf-8")
        path_count = sum(len(r["paths"]) for r in roots.values())
        print(f"Wrote {args.out}")
        print(f"products={','.join(products)} · {len(roots)} roots · "
              f"{path_count} paths · depth {args.depth}")
    else:
        print(text, end="")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
