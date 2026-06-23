---
name: socotra-rating-plugin
description: Construct a valid Socotra RatePlugin for the active socotra-config. Builds on socotra-jar-building-block (read it first for platform structure + JAR introspection). Adds the rating-specific contract — which charges are legal on which element, when to use .rate() vs .amount(), the overload set per product, rate-table lookups — derived mechanically from the config JSON, plus the rating concepts and worked patterns needed to write genuine pricing logic rather than boilerplate. Use whenever writing or reviewing a Socotra rating plugin.
---

# Socotra Rating Plugin

Build a `RatePlugin` implementation that is correct **for one specific config**. Rating is the plugin type most sensitive to the active configuration: the legal `(element, charge)` pairs, the rate-vs-amount decision, the overload set, and the rate tables are all config artifacts. Guessing any of them produces code that compiles and then throws at runtime.

This skill is a **child of `socotra-jar-building-block`**. Read that skill first — it owns the platform structure (the `RatePlugin` interface, `RatingSet`/`RatingItem` signatures and constructor constraints, `ChargeType`, `DataFetcher`, `ResourceSelector`, `MoneyService`, BigDecimal rules) and the `javap`-based JAR scanners. This skill does **not** repeat that material; it adds the rating semantics and a config-side deriver that those scanners do not provide.

## The two sources of truth, and the gap this skill fills

A rating plugin is pinned by two artifacts that must agree:

1. **The generated JAR** (`customer-config.jar`) — exact `rate(...)` overloads, request-record shapes, accessor return types. Read it with the bedrock's `scan_plugins.py` and `build_catalog.py`. **Never assume type names from another config.**
2. **The `socotra-config` JSON** — which charges each element declares (`charges: [...]`), and each charge's `handling` (→ `.rate()` vs `.amount()`). These two facts are **not** visible in the JAR scanners' output, and they are exactly what rating code gets wrong.

`scripts/derive_rating_contract.py` reads the config tree and emits the missing half: a per-product "rating contract" — overloads to implement, every chargeable element with its legal charges and the required `RatingItem` method, and the rate tables with their `makeKey` signatures.

## Workflow

1. **Locate both artifacts.** The SDK project has `customer-config.jar` + `core-datamodel-v*.jar` (bedrock `scan_plugins.py` finds them); the `socotra-config` directory (containing `products/`, `charges/`, usually `coverages/`, `exposures/`, `tables/`) holds the JSON. They may be in the same repo or two.

2. **Pin the JAR surface** (bedrock tooling — gives exact, not derived, names):
   ```bash
   python3 <building-block>/scripts/scan_plugins.py  --project <sdk-dir> --out plugins.md
   python3 <building-block>/scripts/build_catalog.py --project <sdk-dir> --out catalog.json
   ```
   `plugins.md` lists the exact `rate(...)` overloads and request records; `catalog.json` confirms accessor paths and their return types.

3. **Derive the rating contract** from the config JSON:
   ```bash
   python3 scripts/derive_rating_contract.py --config <socotra-config-dir> --out contract.md --json contract.json
   # optional: --catalog catalog.json to note where accessors should be cross-checked
   ```
   The contract tells you, per product: the candidate overloads, and for every element its legal charges each tagged `.rate()` or `.amount()`. **A ⚠️ on an element means the config did not declare a `charges` array there — resolve it (config review / `Chargeable.charges()`) before charging that element.**

4. **Reconcile.** Type names in the contract are *derived from config naming* (e.g. `TermLife20LevelQuoteRequest`, `ChargeType.premium`). Confirm each against `plugins.md`/`catalog.json` before using it. Where they disagree, the JAR wins.

5. **Understand before coding.** Read `references/rating-concepts.md` — what a rate vs. an amount means to the platform, how duration enters, charge handling, the dispatch overloads, and the `RatingSet` contract. This is what keeps the logic genuine instead of a filled-in template.

6. **Write the plugin** in `package com.socotra.deployment.customer`, `implements RatePlugin`. Match a pattern in `references/pattern-catalog.md` to the config's shape (flat charge, table-driven base × factor, multi-exposure, multi-product overloads, range-table interpolation, segment/transaction-level, retention/cancellation).

7. **Validate before finishing** against `references/authoring-and-validation.md` — the constructor constraints, the duplicate-item rule, rate/amount, legal-charge, and overload-coverage checks that catch the runtime failures.

## What rating plugins get wrong (the failures this skill prevents)

- **`.rate()` vs `.amount()`.** Driven by the charge's `handling` (`normal`→rate, `flat`/`retention`→amount), which lives in the config, defaults to `normal`, and is invisible in the JAR. Wrong choice throws `amount is required for charge 'X'…` (or the rate equivalent) at `RatingItem` construction. The contract computes this per charge.
- **Charging an element that doesn't declare the charge.** `Chargeable.validateRatingSet` rejects `(elementLocator, chargeType)` pairs the config never granted. The contract lists the legal set per element.
- **Duplicate rating items.** Two `RatingItem`s with the same `elementLocator` + `chargeType` throw in the `RatingSet` constructor. One item per pair.
- **Implementing the wrong overload set.** Quote-only is common in demos and often wrong: transaction/renewal pricing flows through `rate(<Product>Request)` (segment-level). Multi-product configs need one overload set *per product*. Pin via `plugins.md`.
- **Guessed type/accessor names.** Confirm against the JAR; the contract's names are candidates.

## Cautions when adding conditional logic

Conditional rating requests arrive as prose ("if territory is coastal, apply 1.2×; waive the fee on subscription terms"). Prose is non-exhaustive and names things that may not exist. Two disciplines keep this realistic — **ground, don't guess** and **disclose every fill-in** — because the compile gate (`socotra-verify-deploy`) catches typed-symbol typos but never catches a confidently-wrong reference to a value, a silent default, or the wrong branch.

**Ground every reference before you write the condition.** A condition reads an input and matches a value; confirm both are real first:
- *Input resolves to a generated symbol.* Confirm the accessor (`quote.data().territory()`) actually exists via the bedrock `catalog.json` / `scan_plugins.py`. If it does not, **stop** — do not substitute a plausible-looking accessor.
- *Literal resolves to a real config value.* `"coastal"`, `"Subscription"`, `"P"` must match an actual enum constant / table key / allowed value. If the allowed set can't be confirmed from config, flag it — don't hardcode the guess.

**Disclose every branch the request didn't specify.** Append an assumptions block to the edit — one line per fill-in, so the user catches a wrong assumption in seconds instead of after deploy:
```
Added: coastal territory surcharge
  ASSUMED non-coastal → factor 1.00 (request silent on default)
  ASSUMED territory null → non-coastal, no surcharge
  HARDCODED 1.20 inline — not a rate table (request gave a literal)
  APPLIED to ChargeType.premium on AccidentalDamage (only rated coverage)
  ⚠ "coastal" could NOT be confirmed against config values — verify
```

**Stop and ask — don't proceed — when:** a referenced input field isn't in the generated symbols; a matched literal can't be confirmed against config; a non-exhaustive condition has no stated default *and* the default moves price materially; the target charge/element is ambiguous; or the rule's ordering/precedence interacts with logic already in the plugin.

**When you do fill a gap, fill it safe and say so:** null input → passthrough / no surcharge (never throw, never silently zero a premium), always an explicit `else`, named constant over magic literal. Make edits **additive and idempotent** — add a `getTerritoryFactor()` method and wire it into the composition (the `references/pattern-catalog.md` factor-method shape), never rewrite `rate()`. This keeps the diff reviewable and the assumptions auditable.

## References

- `references/rating-concepts.md` — the rating mental model: rate vs amount vs duration, charge handling, overload dispatch and which fires when, `RatingSet`/`RatingItem` semantics for rating, `MoneyService` rate conversion, BigDecimal discipline.
- `references/pattern-catalog.md` — production-grade patterns keyed to config shape, each with a clean worked snippet and pitfalls. **Demo plugins are referenced for mechanics only — they are not production-grade; their anti-patterns (loose `double` math, unchecked `Optional.get()`, quote-only overloads) are called out explicitly.**
- `references/authoring-and-validation.md` — a decision flow for assembling the plugin and a pre-submit validation checklist.
- `references/future-improvements.md` — running notes on observed gaps in this skill and its tooling (policy-line resolution, acronym name heuristics, Global-vs-Product plugin scoping). Read before relying on the contract deriver's accessor/ChargeType guesses or answering "split the rater per product"; append new cases as they surface.

## scripts/

- `derive_rating_contract.py` — config-only (no JDK), pure stdlib. Walks a `socotra-config` tree (resolving product `extend` inheritance and the `contents` element tree) and emits the rating contract as Markdown (`--out`) and/or JSON (`--json`). Smoke-tested across the EC demo configs. See `scripts/README.md`.
