# Authoring flow and pre-submit validation

Use this once you have the rating contract (hand-derived by reading the config per SKILL.md's "Derive the rating contract by reading the config") and the JAR surface (the overloads/request-records view and the accessor/catalog view, obtained by using the `socotra-jar-building-block` skill to introspect the JAR). It turns those into a plugin and then gates it against the runtime failures.

## Authoring decision flow

Walk these in order; each links to the concept/pattern that answers it.

1. **How many concrete products?** (the contract's "Concrete products".) One → one overload set. Several → one set per product; plan a shared private helper (pattern 4).

2. **Which overloads does the JAR generate, per product?** (the `socotra-jar-building-block` skill's JAR introspection, `RatePlugin` section — authoritative.) Implement the ones the lifecycle needs:
   - Quotes priced → `rate(<Product>QuoteRequest)`.
   - Quick quotes used → `rate(<Product>QuickQuoteRequest)`.
   - Endorsements / renewals / cancellations priced → `rate(<Product>Request)` (segment-level, pattern 6). **Don't skip this** unless the product is quote-only.
   - Leave `statelessRate(...)` to its default unless you have a stateless-rating need.

3. **What is the chargeable element layering?** (the contract's per-product element table, cross-checked with the `socotra-jar-building-block` skill's accessor/catalog introspection.) Single product-level charge → pattern 1/2. Repeating exposures → iterate, charge each exposure locator → pattern 3. Nested coverages → null-check each child and charge its locator.

4. **For each element, which charges are legal and how are they handled?** (the contract's element table.) Each charge is tagged `.rate()` (handling `normal`) or `.amount()` (handling `flat`/`retention`). **An element flagged ⚠️ has no derivable charge set** — resolve via config review / JAR `Chargeable.charges()` before charging it.

5. **Where do rate factors come from?** No tables → constants (pattern 1). Tables → `ResourceSelector` + `makeKey(...)` from the contract (pattern 2). Banded → range table (pattern 5).

6. **Is the value a rate or a total?** If the charge is `normal` (`.rate()`) and your math yields a **term total**, convert with `MoneyService.getRateForTargetAmount(total, duration)` before setting `.rate(...)` — otherwise the platform re-multiplies by duration (see `rating-concepts.md`).

7. **Assemble**: build `RatingItem`s into a `List` (locals only — no instance fields), one per `(elementLocator, chargeType)`, then `RatingSet.builder().ok(true).ratingItems(items).build()`.

## Pre-submit validation checklist

Run every item before declaring the plugin done. Each maps to a runtime exception or a silent mispricing.

**Will-throw-at-runtime (must all pass):**
- [ ] **Signatures match the JAR exactly.** Every `rate(...)` parameter type appears verbatim in the `socotra-jar-building-block` skill's JAR introspection. No invented request types. `@Override` on each.
- [ ] **rate vs amount matches handling.** Every `normal` charge uses `.rate(...)`; every `flat`/`retention` charge uses `.amount(...)`. (Cross-check each item's charge against the contract's handling column.) Wrong → `amount/rate is required for charge 'X'` at construction.
- [ ] **At most one of amount/rate per item**, and the right one is present for the charge's handling. (`referenceRate` defaults to `rate` — leave it unless intentionally overriding.)
- [ ] **No duplicate `(elementLocator, chargeType)`.** Sum contributions and emit one item per pair. Duplicate → `RatingSet` constructor throws.
- [ ] **Every charged `(element, charge)` is granted in config.** No item targets a charge the element's `charges` array (per contract) doesn't list, and no item targets a ⚠️ element without first resolving its legal set. Ungranted → `validateRatingSet` rejects.
- [ ] **ChargeType constants exist** in the generated enum (confirmed via the `socotra-jar-building-block` skill's JAR introspection), spelled as the config charge name with a lower-case first letter.
- [ ] **Accessor paths exist** (confirmed via the `socotra-jar-building-block` skill's accessor/catalog introspection): `quote.<exposure>()`, `…[i]`, `.data().<field>()`, child-coverage accessors. Repeating elements use the real collection accessor name (pluralization is not guessable — confirm).

**Will-silently-misprice (review each):**
- [ ] **No `double` money math.** All amounts/rates computed in `BigDecimal` with explicit `RoundingMode` (`HALF_EVEN`).
- [ ] **No raw `divide`** on possibly non-terminating decimals (needs scale + `RoundingMode`).
- [ ] **Rate is per-duration-unit**, not a term total, for `normal` charges.
- [ ] **Table misses handled deliberately** — fail loud unless a default is genuinely intended; no accidental `orElse(ONE)`.
- [ ] **`Optional` guarded, not `.get()`-ed** — `request.segment()` and every `getRecord(...)` use `map(...).orElse*`/`ifPresent`.
- [ ] **Nullable children null-checked** — exposure/peril/coverage child accessors return `null` (not `Optional`); guard before `.locator()`.
- [ ] **No mutable instance/static state** on the plugin class; everything per-call is local.
- [ ] **Segment overload implemented** if the product supports endorsements/renewals (else they won't reprice).

**Build / deploy:**
- [ ] Class in `com.socotra.deployment.customer` (or a sub-package), `implements RatePlugin`, **not** annotated `@Plugin` (the interface carries it). Generated files untouched.
- [ ] Validate with IDE diagnostics. Do **not** run `./gradlew build` (the SDK template's build task is not the validation path; use `refreshReferenceDatamodel`/`validateConfig`/`overwriteConfigOnTenant` per the SDK skill).

## When the contract and JAR disagree

The contract is derived from config naming and can drift from the JAR (datamodel version, generator quirks, abstract bases, package nesting). **The JAR wins.** If the `socotra-jar-building-block` skill's JAR introspection shows a different overload name or a different accessor than the contract guessed, use the JAR's and treat the contract as the map of *intent* (which charges, which handling), not of *names*.
