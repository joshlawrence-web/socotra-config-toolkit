# JAR introspection tooling

Executable tooling for introspecting **any** Socotra SDK project's compiled JARs, so
guidance can be config-explicit instead of relying on this skill's bundled ZenCover
example. Point the scripts at a target project's checkout and they report the exact
plugin surface and data model the platform generated for *that* config — single- or
multi-product.

**JAR layout:** both layouts are handled automatically. `customer-config.jar` and
the newest `core-datamodel-v*.jar` (sources/javadoc jars excluded) are searched
under `<project>/build/` first, then at the project root itself — some configs ship
jars at the top level rather than in a Gradle `build/` dir. Explicit
`--customer-jar`/`--datamodel-jar` paths always win.

**Multi-product configs:** one customer package can define several products (e.g.
`BasicCreditCardProtection` + `PremiumCreditCardProtection` in one
`customer-config.jar`). All product stems are detected and reported; `--product` is
an optional *filter*, never a requirement. A product stem is a `{Stem}Quote` class
(`*QuickQuote` excluded); because exposures also generate `{Stem}Quote` classes,
stems carrying a `{Stem}Product` marker class are preferred (falling back to
`{Stem}Segment`, then to all quote stems — a Segment or QuickQuote is NOT required).

**Datamodel version matters:** the generated SPI *shape* varies with the
`core-datamodel` version and the products defined — interface set (e.g. some configs
have no `CancellationPlugin`/`ConfigMigrationPlugin`), request-record overload
counts (one set per product), and which methods exist on each interface. Never
assume a fixed surface: scan the target jars and trust only what javap reports. The
parsed datamodel version is included in every output header.

**Prerequisite:** a JDK with `javap` on PATH (e.g. `brew install openjdk`). All
scripts are pure Python stdlib — no pip installs. They fail fast with a clear
message if `javap` is missing or the JARs cannot be found.

## introspect.py — shared library

Not a CLI. The javap-backed introspection core (adapted from VelocityConverter's
`sdk_introspect.py`): memoised `javap` invocation, JAR discovery (`build/` then
project root; newest `core-datamodel-v*.jar`), datamodel-version parsing from the
jar filename, class enumeration from the JAR index via `zipfile`, zero-arg accessor
maps, full method-signature parsing (overloads, `default` vs `abstract`,
generics-safe parameter splitting), `Optional<T>`/`Collection<T>`/`List<T>`
unwrapping, multi-product stem detection (`detect_products`), and the schema/path
BFS used by `build_catalog.py`.

## scan_plugins.py — plugin registry scanner

Finds every interface in `com.socotra.deployment.customer` whose name ends with
`Plugin` (confirmed as an interface via `javap`), then emits one Markdown catalog:
all declared methods per interface with full signatures (every overload preserved,
across all products), plus a component table (accessor → return type) for each
request/response type the signatures reference. The header lists every detected
product and the core-datamodel version. Run this against a target project before
writing plugin Java — it tells you the exact interfaces, overloads and request
records that exist for that config.

```
python3 scan_plugins.py --project <sdk-project-dir> [--out plugins.md]
python3 scan_plugins.py --customer-jar <jar> --datamodel-jar <jar> [--product Stem]
```

## build_catalog.py — data-path catalog builder

BFS from the config's root types — by default the union, over **all** detected
products, of `{Stem}Quote`, `{Stem}Segment`, `{Stem}Policy`, `{Stem}QuickQuote`
(whichever exist) plus any top-level `*Account` class (account types are not
product-stem named: `PersonalAccount`, `BankCustomerAccount`, …) — over zero-arg
accessors, unwrapping `Optional`/`Collection`/`List` wrappers, emitting JSON of
shape `{products, datamodel_version, roots: {RootType: {fqcn, paths: {dot.path:
returnType}}}}`. Every reachable field path up to `--depth` segments (default 3),
e.g. `items.data.purchaseDate → java.time.LocalDate`. `--root` (repeatable)
overrides detection entirely; `--product` narrows the default roots to one product.
Use it to verify a data path exists before generating accessor chains.

```
python3 build_catalog.py --project <sdk-project-dir> [--product Stem] [--root SimpleName]... [--depth 3] [--out catalog.json]
```

## examples/

Illustrative output from real runs against two differently shaped configs:

- `zencover-plugins.md` / `zencover-catalog.json` — single product **ZenCover**,
  jars under `build/`, core-datamodel **v1.7.61**: 18 plugin interfaces, 48
  methods (RatePlugin: 3 `rate` + 3 `statelessRate` overloads); catalog of 4 roots
  (Quote/Segment/QuickQuote/PersonalAccount), 522 paths at depth 3.
- `credit-card-protection-plugins.md` / `credit-card-protection-catalog.json` —
  TWO products (**BasicCreditCardProtection**, **PremiumCreditCardProtection**) in
  one customer package, jars at the project root, core-datamodel **v1.6.180**: a
  different SPI shape — only 11 plugin interfaces (no CancellationPlugin or
  ConfigMigrationPlugin), 64 methods, RatePlugin with 6 `rate` overloads (one per
  product × Quote/QuickQuote/Segment request record); catalog of 7 roots, 1519
  paths at depth 3.
