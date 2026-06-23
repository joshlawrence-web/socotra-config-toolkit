---
name: socotra-jar-building-block
description: Foundation skill for Socotra Enterprise Core Java — under-the-hood structure (coremodel classes, DataFetcher, generated customer package, plugin interface dispatch) plus javap-based introspection tooling to scan any target project's JARs. Use whenever writing or reviewing Java for any Socotra plugin to ensure valid signatures, types, and overloads. Plugin-type-specific guidance (rating, underwriting, cancellation, …) belongs in dedicated child skills built on this one.
---

# Socotra Java Building Blocks (foundation)

Ground truth + tooling for valid Socotra plugin Java. This skill covers the **platform structure shared by all plugin types**. Per-plugin semantics (how to write a good rating plugin, underwriting flag policy, cancellation flows, …) belong in separate child skills that build on this one.

## The two layers

1. **core-datamodel JAR** — stable platform API. v1.7.61 sources bundled in `sources/` (386 files): `com.socotra.coremodel`, `com.socotra.deployment` (incl. `DataFetcher`), `com.socotra.developer`, `com.socotra.platform.tools`. The generated `coremodel.protobuf` wire classes are excluded — they are not part of the plugin API. `sources/VERSION` records the bundled version.
2. **Generated `com.socotra.deployment.customer` package** — regenerated from each configuration. Contains the plugin SPI interfaces (`RatePlugin`, `ValidationPlugin`, …) and typed product records (`{Product}Quote`, `{Product}Segment`, …). **Everything here varies per configuration**: a config may define multiple products (one overload set per product per interface), the interface SET itself depends on config features and datamodel version, and all type names follow config naming. Two contrasting real examples in `generated-examples/`: `zencover/` (single product, datamodel v1.7.61, 18 interfaces) and `credit-card-protection/` (two products, v1.6.180, 11 interfaces).

## Workflow

1. **Identify the target project.** Socotra SDK projects have `customer-config.jar` (generated product model) and `core-datamodel-v*.jar`, under `build/` or at the project root.
2. **Scan the target's actual surface — never assume from examples** (requires JDK `javap`; both scripts detect all products in the config and report the datamodel version):
   ```bash
   # Exact plugin interfaces + ALL overloads + request/response component tables for THIS config
   python3 scripts/scan_plugins.py --project <sdk-project-dir> --out plugins.md

   # Every reachable data path and return type from each product's root types (Quote/Segment/…) + account types
   python3 scripts/build_catalog.py --project <sdk-project-dir> --out catalog.json
   ```
   Sample outputs in `scripts/examples/`. Details in `scripts/README.md`.
3. **For platform API questions** (DataFetcher, services, coremodel types) read the reference docs, and when unsure about an exact signature, grep the bundled sources — never guess:
   ```bash
   grep -rn "methodName" sources/com/socotra/
   ```
4. **Write plugin code** in `package com.socotra.deployment.customer`, implementing the generated interface. Dispatch is by parameter-type overload (automation plugins use abstract methods instead).

## References (read selectively)

- `references/architecture.md` — how the layers fit; `@Plugin`/`@AutomationPlugin` annotations; the 19-entry PluginType dispatch table; typed request/response flow; package map.
- `references/datafetcher.md` — complete `DataFetcher` surface (40 methods, grouped by domain), generics usage (`<T> T getQuote` → your generated quote type), plus `TableRecordFetcher`, `RangeTableRecordFetcher`, `ConstraintsFetcher`, `ResourceSelector`, `MoneyService`, `TimeService`, `EventsService`, `AuxDataService`, `SearchService`.
- `references/plugin-interfaces.md` — structural catalog of every plugin interface with overload patterns, request-record accessor conventions (`request.quote()`, `quote.items()`, `quote.data().field()`), and the five verified per-configuration variation axes (product naming, multi-product overload sets, feature-dependent overloads, datamodel-version drift, varying interface set). Child skills go deeper per plugin type.
- `references/coremodel-classes.md` — key classes plugin code touches: `RatingSet`/`RatingItem`, `Charge`, `ULID`, `Policy`/`Transaction`/`Term`, quote/segment interfaces, enums, grep conventions for the rest.
- `references/execution-paths.md` — high-level invocation map per platform object (Account, Quote, Transaction, Term, Invoice, Delinquency, documents, ...): which plugin types fire at which lifecycle moment/state, and what the return value controls. Map only — per-plugin semantics stay in child skills.

## Platform-wide gotchas

- **No Money class.** Monetary values are plain `BigDecimal`; `MoneyService` only rounds to currency scale (HALF_EVEN platform-wide).
- **Deprecated — do not use**: `DataFetcher.getSegments(ULID)` (use `getSegmentByTransaction`), 2-arg `TimeService.calculateDuration` (miscomputes mid-term segments — use 3-arg form).
- **Generics need product context**: `<T> T getQuote(...)` returns your generated type; the cast target comes from the customer package, so always confirm the product's type names via `scan_plugins.py`/`build_catalog.py` or the project's `build/customer-config-source.jar`.
- If the target project pins a core-datamodel version other than 1.7.61, prefer that project's sources/javadoc JAR for exact platform signatures.

## Building child skills on this foundation

A per-plugin skill (e.g. `socotra-rating-plugin`) should: (a) reference this skill for structure and tooling rather than duplicating it; (b) ship the plugin-type-specific semantics — request/response contract details, validation rules (e.g. `RatingItem` constructor constraints), idioms, and worked examples; (c) instruct running `scan_plugins.py` against the target project to pin exact overloads before writing code.
