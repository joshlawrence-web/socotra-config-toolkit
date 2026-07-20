---
name: socotra-rating-plugin
description: 'Construct a valid Socotra RatePlugin for the active socotra-config. Builds on socotra-jar-building-block (read it first for platform structure + JAR introspection). Adds the rating-specific contract — which charges are legal on which element, when to use .rate() vs .amount(), the overload set per product, rate-table lookups — derived by reading the config JSON, plus the rating concepts and worked patterns needed to write genuine pricing logic rather than boilerplate. Runs with NO network and NO code execution: the rating contract is derived by reading the config files and reasoning (no script to run), reference material is inert, and JAR introspection is delegated to the socotra-jar-building-block skill. Use whenever writing or reviewing a Socotra rating plugin.'
---

# Socotra Rating Plugin

Build a `RatePlugin` implementation that is correct **for one specific config**. Rating is the plugin type most sensitive to the active configuration: the legal `(element, charge)` pairs, the rate-vs-amount decision, the overload set, and the rate tables are all config artifacts. Guessing any of them produces code that compiles and then throws at runtime.

This skill is a **child of `socotra-jar-building-block`**. Read that skill first — it owns the platform structure (the `RatePlugin` interface, `RatingSet`/`RatingItem` signatures and constructor constraints, `ChargeType`, `DataFetcher`, `ResourceSelector`, `MoneyService`, BigDecimal rules) and the JAR introspection. This skill does **not** repeat that material; it adds the rating semantics and a config-side derivation method that JAR introspection does not provide.

> **No network, no code execution.** This skill runs entirely by reading files and reasoning. The rating contract is derived by reading the `socotra-config` JSON directly (the method in "Derive the rating contract by reading the config" below) — there is no script to run. Everything under `references/` (including `references/examples/`) is inert reference material.
>
> **Transitive dependency on `socotra-jar-building-block` (JAR introspection is still code).** JAR introspection — reading the generated `customer-config.jar` for the exact overloads, request-record shapes, and accessor return types — is owned by the `socotra-jar-building-block` skill and is **out of scope for this code-free skill**: that skill's JAR-introspection tooling remains Python and was intentionally left runnable. Wherever this skill says "confirm against the JAR," it means **use the `socotra-jar-building-block` skill to introspect the JAR** (delegate to it by name); do not attempt to introspect the JAR yourself here.

## The two sources of truth, and the gap this skill fills

A rating plugin is pinned by two artifacts that must agree:

1. **The generated JAR** (`customer-config.jar`) — exact `rate(...)` overloads, request-record shapes, accessor return types. Introspect it via the `socotra-jar-building-block` skill. **Never assume type names from another config.**
2. **The `socotra-config` JSON** — which charges each element declares (`charges: [...]`), and each charge's `handling` (→ `.rate()` vs `.amount()`). These two facts are **not** visible in JAR introspection output, and they are exactly what rating code gets wrong.

You derive the missing half — a per-product "rating contract" (overloads to implement, every chargeable element with its legal charges and the required `RatingItem` method, and the rate tables with their `makeKey` signatures) — by **reading the config tree yourself**, following "Derive the rating contract by reading the config" below.

## Workflow

1. **Locate both artifacts.** The SDK project has `customer-config.jar` + `core-datamodel-v*.jar` (the `socotra-jar-building-block` skill's introspection locates them); the `socotra-config` directory (containing `products/`, `charges/`, usually `coverages/`, `exposures/`, `tables/`) holds the JSON. They may be in the same repo or two.

2. **Pin the JAR surface** — use the **`socotra-jar-building-block` skill to introspect the JAR** (gives exact, not derived, names): the exact `rate(...)` overloads and request records, plus the accessor paths and their return types. Keep that skill's output at hand (it calls these the "plugins" and "catalog" views) to reconcile against the contract you derive next. **This is the one code-bearing step and it lives in the other skill** — this skill issues no commands.

3. **Derive the rating contract** from the config JSON by reading it yourself — follow **"Derive the rating contract by reading the config"** below. It yields, per product: the candidate overloads, and for every element its legal charges each tagged `.rate()` or `.amount()`, plus the rate tables and their `makeKey` signatures. Hand-write it (Write tool) as `contract.md` (and optionally `contract.json`) if you want a persisted artifact. **A ⚠️ on an element means the config did not declare a `charges` array there — resolve it (config review / `Chargeable.charges()`) before charging that element.**

4. **Reconcile.** Type names in the contract are *derived from config naming* (e.g. `TermLife20LevelQuoteRequest`, `ChargeType.premium`). Confirm each against the `socotra-jar-building-block` skill's JAR introspection before using it. Where they disagree, the JAR wins.

5. **Understand before coding.** Read `references/rating-concepts.md` — what a rate vs. an amount means to the platform, how duration enters, charge handling, the dispatch overloads, and the `RatingSet` contract. This is what keeps the logic genuine instead of a filled-in template.

6. **Write the plugin** in `package com.socotra.deployment.customer`, `implements RatePlugin`. Match a pattern in `references/pattern-catalog.md` to the config's shape (flat charge, table-driven base × factor, multi-exposure, multi-product overloads, range-table interpolation, segment/transaction-level, retention/cancellation).

7. **Validate before finishing** against `references/authoring-and-validation.md` — the constructor constraints, the duplicate-item rule, rate/amount, legal-charge, and overload-coverage checks that catch the runtime failures.

## What rating plugins get wrong (the failures this skill prevents)

- **`.rate()` vs `.amount()`.** Driven by the charge's `handling` (`normal`→rate, `flat`/`retention`→amount), which lives in the config, defaults to `normal`, and is invisible in the JAR. Wrong choice throws `amount is required for charge 'X'…` (or the rate equivalent) at `RatingItem` construction. The contract computes this per charge.
- **Charging an element that doesn't declare the charge.** `Chargeable.validateRatingSet` rejects `(elementLocator, chargeType)` pairs the config never granted. The contract lists the legal set per element.
- **Duplicate rating items.** Two `RatingItem`s with the same `elementLocator` + `chargeType` throw in the `RatingSet` constructor. One item per pair.
- **Implementing the wrong overload set.** Quote-only is common in demos and often wrong: transaction/renewal pricing flows through `rate(<Product>Request)` (segment-level). Multi-product configs need one overload set *per product*. Pin via the `socotra-jar-building-block` skill's JAR introspection.
- **Guessed type/accessor names.** Confirm against the JAR; the contract's names are candidates.

## Cautions when adding conditional logic

Conditional rating requests arrive as prose ("if territory is coastal, apply 1.2×; waive the fee on subscription terms"). Prose is non-exhaustive and names things that may not exist. Two disciplines keep this realistic — **ground, don't guess** and **disclose every fill-in** — because the compile gate (`socotra-verify-deploy`) catches typed-symbol typos but never catches a confidently-wrong reference to a value, a silent default, or the wrong branch.

**Ground every reference before you write the condition.** A condition reads an input and matches a value; confirm both are real first:
- *Input resolves to a generated symbol.* Confirm the accessor (`quote.data().territory()`) actually exists via the `socotra-jar-building-block` skill's JAR introspection. If it does not, **stop** — do not substitute a plausible-looking accessor.
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

## Derive the rating contract by reading the config (no script)

This is a **reading-and-reasoning** procedure — no script, no execution. You open the `socotra-config` JSON files, resolve inheritance, walk the element tree, and hand-write the contract with the Write tool. It reproduces exactly what the retired `derive_rating_contract.py` computed; follow it literally so nothing is lost. Worked outputs to compare against live in `references/examples/` (`term-life-contract.md` + `.json` — a config that declares **no** `charges` arrays, so every element is flagged ⚠️; `accident-and-health-contract.md` — a config that declares charges and handling, so the legal-charge / rate-vs-amount columns are populated).

A `socotra-config` root is a directory containing `products/` and `charges/` (and usually `coverages/`, `exposures/`, `policyLines/`, `tables/`). **Each component is a folder holding a `config.json`** — the folder name is the component's config name (e.g. `charges/Premium/config.json`).

### Step 0 — load the component folders

Read every `config.json` under each of `charges/`, `products/`, `coverages/`, `exposures/`, `policyLines/`, `tables/`. Key each parsed object by its **folder name**. For element resolution (below), build one lookup of `coverages` + `exposures` + `policyLines` keyed by **lower-cased** folder name (so a PascalCase `contents` ref matches a camelCase folder), remembering each entry's kind (`exposure` / `coverage` / `policyLine`). Policy lines are a normal intermediate container between a product and its exposures (auto/commercial), are chargeable in their own right, and resolve and recurse exactly like an exposure.

### Step 1 — resolve product `extend` inheritance

For each product, build its `extend` chain: start at the product, follow the `extend` field to its parent, repeat until there is no `extend` or you revisit a name (guard against cycles). Then merge, and this merge is exact:

- **Scalars (everything except `contents`, `charges`, `data`):** walk the chain **parents-first** and let each nearer layer overwrite — so the **child wins** on scalar keys (`displayName`, `abstract`, etc.).
- **`contents` (the element ref list):** union across the chain, **child entries first**, skipping duplicates (preserve first-seen order).
- **`charges`:** union the same way (child-first, dedup). Track separately whether **any** layer in the chain declared a `charges` key at all. If **no** layer declared one, the merged product has **no `charges` key** — that is what later flags the element ⚠️ (an empty `[]` is different: it means "declared, but bears no charges"). Do not synthesize an empty array when none was declared.
- **`data`:** union of field definitions, child-first (first-seen wins per field).

Note the resolved `abstract` flag: **abstract products are not rated directly** (list them separately); **concrete products** (`abstract` falsy) each get a rate overload set.

### Step 2 — determine each charge's handling → RatingItem method

For every charge named in a `charges` array, open `charges/<Name>/config.json` and read its **`handling`** field. This is a **CONFIG fact, not in the JAR**, and it is the single thing rating code most often gets wrong — **read it from the charge config; never infer it from the charge's name.** Map it:

| `handling` (from config) | required RatingItem method |
|---|---|
| `normal` | `.rate(value)` |
| `flat` | `.amount(value)` |
| `retention` | `.amount(value)`  *(cancellation/retention charges only)* |

**`handling` defaults to `normal` when the key is absent** → `.rate(value)` (mark it `*(default)*` in the table so the reader knows it was defaulted, not declared). An unrecognized value should be surfaced explicitly (e.g. `.???(value)  # unknown handling 'X'`) rather than silently mapped. Also capture, per charge: `category` (default `?` if absent), `invoicing` (default `scheduled`), and the generated enum constant `ChargeType.<camel(name)>` (see the naming rule below) — flagged as "confirm against the JAR."

### Step 3 — the config→Java name rule (`camel`)

To turn a config component name into its generated Java member (accessor / enum constant): **if the name begins with a run of 2+ capitals it is an acronym and is kept verbatim** (`GST` → `GST`, `CGLBodilyInjury` → `CGLBodilyInjury`, `PABodilyInjury` → `PABodilyInjury`); **otherwise lower-case only the single leading capital** (`PersonalVehicle` → `personalVehicle`, `Premium` → `premium`). Lower-casing the first letter of an acronym produces names the JAR never generates (`gST`, `cGLBodilyInjury`) — do not do it. Every name you derive this way is a **candidate** to confirm against the JAR.

### Step 4 — walk the `contents` element tree (enumerate every chargeable element)

Produce a depth-first list of chargeable nodes per product:

1. **The product itself is chargeable** — emit it first as kind `product`, accessor `quote  (or segment)`, locator `quote.locator()  /  segment.locator()`, with its resolved `charges` (from Step 1).
2. For each ref in the product's merged `contents`, **visit** depth-first (cap recursion at depth 8 to bound cycles):
   - **Strip the trailing modifier**, one of: `?` = optional (nullable child record → null-check before charging); `+` = repeating, 1+ (collection accessor → iterate); `*` = repeating, 0+ (collection accessor → iterate); `!` = required (non-Optional component). No modifier = single, required.
   - **Resolve** the base name (case-insensitively) against the coverage/exposure/policyLine lookup.
     - **Not found →** emit an `UNRESOLVED` node (accessor `<parent>.<camel(base)?>`, locator `n/a — unresolved ref`, note: ref not found under coverages/, exposures/ or policyLines/ — may be a sub-product or naming/case mismatch, confirm via JAR). Do not recurse.
     - **Found →** accessor is `camel(realName)`. If repeating (`+`/`*`), the accessor path is `<parent>.<accessor>s()[i]  # CONFIRM collection accessor+name via JAR` (pluralization is **not** reliably guessable — flag it); otherwise `<parent>.<accessor>()`. The element's locator accessor is that path (without the `# …` comment) + `.locator()`. Attach the element's `charges` (from its own config), then **recurse into its `contents`** with the parent path set to this element's accessor path.
   - Record each node's kind (`product`/`exposure`/`coverage`/`policyLine`/`UNRESOLVED`), config name, accessor path, locator accessor, modifier meaning, and its legal charges.

**Legal charges per element:** if the element's config has **no `charges` key**, the node is *undeclared* → flag ⚠️ "**no `charges` array in config** — cannot derive; confirm via JAR `Chargeable.charges()` / config review." If it has a `charges` key that is empty, render "*(empty charges array — element bears no charges)*." Otherwise list each charge with its handling and method from Step 2.

### Step 5 — rate tables and `makeKey` signatures

For each folder under `tables/`, read its `config.json` `columns` map. **Key columns** are those with `isKey: true`, **in declared order**; the rest are value columns. Derive:

- `makeKey` signature: `<TableName>.makeKey(<keyCol1>, <keyCol2>, …)` in declared key order (if no `isKey` columns are found, note `makeKey(...)  # no isKey columns found`).
- value columns and their `dataType` (default `?`).
- `selectionTimeBasis` (default `?`).
- whether it is a **range table** — true if the config has `rangeStart` or `rangeEnd`, or its `type` contains `"range"`. Range tables use `getRangeTable(...).getRecord(key, boundValue)` / `interpolate(...)`.
- Note that generated column accessors usually return `String` → convert to `BigDecimal` yourself.

### Step 6 — candidate overloads per concrete product

For each concrete product, the candidate rate overloads to consider (prune to what the JAR actually generates) are:

- `rate(<Product>QuoteRequest request)`
- `rate(<Product>QuickQuoteRequest request)`
- `rate(<Product>Request request)  # segment/transaction level; request.segment() is Optional`

### Step 7 — reconcile against the JAR

Every Java name you derived (`<Product>QuoteRequest`, `ChargeType.x`, accessor paths, pluralized collection accessors) is derived from config naming and must be **confirmed against the JAR via the `socotra-jar-building-block` skill** — the overloads/request-records view for the exact `rate(...)` signatures, and the accessor/catalog view for the exact accessor paths and return types. Where a name differs, **the JAR wins**; treat the contract as the map of *intent* (which charges, which handling), not of *names*. (The retired script had an optional accessor cross-check that matched contract accessor paths case-insensitively against a catalog; reproduce that judgement by comparing your derived accessors to the jar-building-block accessor view and noting any you could not confirm, rather than telling the user to "fix" a merely case-different name.)

### The contract's output shape (hand-write this)

Emit the same structure the script produced, so the artifact is drop-in comparable to `references/examples/`:

**Markdown (`contract.md`):**
1. Title `# Rating contract (derived from config)` and the config dir path.
2. A blockquote disclaimer: Java type names are derived from config naming — confirm against the JAR (via the `socotra-jar-building-block` skill) before relying on them.
3. **Concrete products** (each needs rate overloads) and, if any, **Abstract base products** (not rated directly).
4. **Charges defined in this config** — a table: `| Charge | category | handling | RatingItem method | ChargeType (confirm) |`, with defaulted handling marked `*(default)*`, and the note "handling defaults to `normal` when omitted -> use `.rate()`."
5. **Rate tables** — per table: name + (range table / table) + `selectionTimeBasis`, the `makeKey` key line, value columns with types, the range-table note if applicable, and the String-conversion note.
6. **Per-product rating contract** — for each concrete product: the candidate overloads, then a "Chargeable elements and their LEGAL charges" table `| Element | kind | locator | legal charges (-> method) |` (charges rendered as `` `Name` handling -> `.method(value)` `` joined by line breaks; ⚠️ for undeclared), then a `> ⚠️` summary line listing any elements without a derivable charge set.
7. **Before you write code** — a short checklist: (1) use the `socotra-jar-building-block` skill to get the EXACT `rate(...)` overloads + request-record component names; (2) use it to confirm the accessor paths exist and their return types; (3) map each charge to `.rate()` vs `.amount()` using the handling column — do not guess from the charge's name; (4) emit at most one RatingItem per `(elementLocator, chargeType)` — duplicates throw.

**JSON (`contract.json`, optional):** mirror the same data — `config_dir`, `concrete_products`, `abstract_products`, `charges_defined` (per charge: name, configured, category, handling, handling_explicit, invoicing, ratingitem_method, chargetype_constant), `tables` (per table: name, key_columns, value_columns, value_types, selection_time_basis, is_range, makekey), and `products` (per product: name, display_name, candidate_overloads, elements[] with kind/config_name/accessor_path/locator_accessor/modifier/charges/charges_declared).

## References

- `references/examples/` — inert worked outputs of the derivation above (`term-life-contract.md` + `.json`, `accident-and-health-contract.md`), smoke-tested against real EC demo configs. Reference material only — nothing to run; compare your hand-derived contract against these.
- `references/rating-concepts.md` — the rating mental model: rate vs amount vs duration, charge handling, overload dispatch and which fires when, `RatingSet`/`RatingItem` semantics for rating, `MoneyService` rate conversion, BigDecimal discipline.
- `references/pattern-catalog.md` — production-grade patterns keyed to config shape, each with a clean worked snippet and pitfalls. **Demo plugins are referenced for mechanics only — they are not production-grade; their anti-patterns (loose `double` math, unchecked `Optional.get()`, quote-only overloads) are called out explicitly.**
- `references/authoring-and-validation.md` — a decision flow for assembling the plugin and a pre-submit validation checklist.
- `references/future-improvements.md` — running notes on observed gaps in this skill and its derivation method (policy-line resolution, acronym name heuristics, Global-vs-Product plugin scoping). Read before relying on the contract's accessor/ChargeType guesses or answering "split the rater per product"; append new cases as they surface.

## Maintenance

- **No network, no code execution.** This skill contains no runnable scripts. The rating contract is produced by the reading-and-reasoning method in "Derive the rating contract by reading the config" above; `references/examples/` holds inert worked outputs to compare against. Keep it that way — do not add scripts to this skill.
- The rating contract was **previously** produced by a `scripts/derive_rating_contract.py`; that script has been retired and its full logic folded into the prose method above. If you find a config case the method mishandles, fix the prose (and log it in `references/future-improvements.md`) rather than reintroducing a script.
- **Transitive dependency:** JAR introspection is delegated to the `socotra-jar-building-block` skill, whose introspection tooling remains Python and was intentionally left out of this code-free pass. This skill stays code-free; the JAR-reading step lives in that sibling skill.
