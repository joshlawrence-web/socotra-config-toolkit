# Rating concepts — what the numbers mean to the platform

The bedrock skill gives the *signatures* (`RatingSet`, `RatingItem`, `ChargeType`, the `RatePlugin` overloads). This document gives the *meaning* — the model you need so the pricing logic is reasoned rather than copied. Read it before writing rating code.

## What a RatePlugin actually returns

A `RatePlugin` is invoked whenever the platform needs a price: a quote priced, a quick-quote, or a transaction (issuance, change, renewal, cancellation, …) priced at the segment level. Each invocation returns a `RatingSet` = `ok` flag + a `Collection<RatingItem>`. The platform takes your rating items and turns them into persisted `Charge`s on the term.

You are **not** computing final dollar amounts on the policy. You are describing, per chargeable element, *how much* of *what kind* of charge applies, in the unit the charge's handling dictates. The platform then applies duration, currency rounding, and invoicing.

## The single most important decision: rate vs. amount

Every `RatingItem` carries **either** a `rate` **or** an `amount` — never both, never neither (for financial charges). Which one is mandated by the charge's `handling`, configured in `charges/<Name>/config.json` and **defaulting to `normal` when absent**:

| `handling` | builder field | meaning |
|---|---|---|
| `normal` | `.rate(value)` | a **per-duration-unit rate**. The platform multiplies it by the segment's duration to get the charge amount. A full-term premium of 1200 on an annual policy is expressed as the per-year rate that, times the term duration, yields 1200. |
| `flat` | `.amount(value)` | a **fixed amount**, applied once, not scaled by duration (policy fees, invoice fees). |
| `retention` | `.amount(value)` | a fixed amount used by `CancellationPlugin` for retention charges. |

Get this wrong and `RatingItem`'s canonical constructor throws at construction time (`amount is required for charge 'X' but it is null`, or the symmetric rate error). **`handling` is not in the JAR** — it is a config fact. `derive_rating_contract.py` reads it for you and tags each charge. Do not infer it from the charge's *name* (`Fee`/`PolicyFee` are often `normal`, not `flat`; see term-life, which rates its `fees` charge with `.rate()` because it declares no `handling`).

### Rate is per-duration-unit — converting a target total

Because `normal` charges are rates scaled by duration, if your rating logic produces a **target total** for the term, convert it to a rate before putting it on the item:

```java
// MoneyService.getRateForTargetAmount(amount, duration) = amount / duration (HALF_EVEN)
BigDecimal rate = new MoneyService(quote.currency().orElse("USD"))
        .getRateForTargetAmount(targetTotal, request.duration());
```

`request.duration()` and `request.durationBasis()` are on the quote/segment request records (see the bedrock interface catalog). If you instead emit the target total *as a rate*, the platform multiplies it by duration again and over-charges.

## ChargeType: the generated enum vs the coremodel interface

`RatingItem.chargeType(...)` takes a `com.socotra.coremodel.ChargeType` — an **interface**. The concrete enum (`ChargeType.premium`, `ChargeType.tax`, `ChargeType.providerPremium`, …) is **generated per config** in `com.socotra.deployment.customer`, one enum constant per `charges/<Name>` folder. The constant name is the charge folder name with a lower-case first letter (`Premium` → `ChargeType.premium`, `ProviderPremium` → `ChargeType.providerPremium`). The contract derives these; confirm against the JAR. The enum implements the interface, so its `category()`/`handling()`/`invoicing()` come straight from the config.

## You can only charge what the element was granted

Each product/exposure/coverage declares a `charges: [...]` array naming the charges that may land on it. The platform validates your `RatingSet` against this (`Chargeable.validateRatingSet`): a `RatingItem` whose `(elementLocator, chargeType)` is not granted is rejected. So the legal universe of items is the cross-product of *chargeable elements you reached* × *charges each declares*. The contract enumerates this; respect it. When an element has no `charges` array (the contract flags it ⚠️), you cannot prove what is legal there from config alone — confirm before charging it.

## elementLocator: where the charge lands

`RatingItem.elementLocator` is the `ULID` of the element the charge applies to — the product (`quote.locator()` / `segment.locator()`), an exposure (`quote.<exposure>().locator()` or `…[i].locator()` for repeating), or a coverage (`…<coverage>().locator()`). Charging the product vs. a coverage is a modelling choice that must match how the config grants charges and how downstream reporting expects them. The contract shows the locator accessor per element.

**One item per (elementLocator, chargeType).** If a coverage accrues premium from several rating steps, sum them and emit one item — two items for the same pair throw in the `RatingSet` constructor.

## The overload set: which method fires when

`RatePlugin` dispatches by request type (all methods are `default` no-ops; override only what you implement). For each product the JAR generates (confirm exact names with `scan_plugins.py`):

- `rate(<Product>QuoteRequest)` — a full quote is priced. Request exposes `quote()`, `duration()`, `durationBasis()`.
- `rate(<Product>QuickQuoteRequest)` — a quick quote (a lighter quote shape). Generated for every concrete product.
- `rate(<Product>Request)` — **segment/transaction-level** pricing: issuance, change, renewal, etc. Request exposes `policy()`, `transaction()`, `segment()` (an **`Optional`**), `duration()`, `durationBasis()`. This is the path most transaction flows take after the initial quote, and it is the one demos most often skip.
- `statelessRate(...)` twins — default to delegating to the stateful method; override only for stateless-rating use cases.

Multi-product configs carry a **full set per product** (e.g. France + Spain in payment-protection). Implement each product's overloads, or factor shared logic into a private helper keyed by locator + the product's data (see the multi-product pattern).

### Plugin scoping: Global vs Product

A multi-product config does **not** have to be one rate plugin. Per the [Plugins Overview](https://docs.socotra.com/configuration/plugins/overview.html), any plugin interface can be implemented **Global** (`/plugins/java`) **or Product-scoped** (`<productName>/plugins/java`); when both exist, the **Product implementation takes precedence**. So when asked to "split the rater per product," the accurate answer is *either* keep one Global `RatePlugin` delegating to per-product helpers, *or* move to product-scoped plugin folders — not "the platform only allows one rate plugin."

Caveats before recommending the product-scoped move:

- The gradle SDK template (`src/main/java/...`) deploys as **Global**. Product-scoping uses the config-folder layout (`socotra-config/products/<name>/plugins/java`); confirm the target project actually supports per-product folders first.
- **Isolation rule** (same overview): "each plugin is executed in an isolated memory space… calling methods belonging to a different plugin is not supported." Two product-scoped plugins therefore **cannot share a common helper plugin** — a shared `RatingMath`-style util must be co-located in each product folder, not referenced across them.

Typical structure: the quote overload reads `request.quote()`; the segment overload reads `request.segment()` (guard the `Optional`) and falls back to the interface default when empty:

```java
@Override
public RatingSet rate(AcmeRequest request) {
    return request.segment()
        .map(seg -> price(seg.locator(), seg.data(), request.duration()))
        .orElseGet(() -> RatePlugin.super.rate(request));   // no segment -> platform default
}
```

## ResourceSelector: rate tables are config, surfaced as JAR classes

Rate factors live in config `tables/`; each becomes a generated record class with a static `makeKey(...)`. Resolve them through `ResourceSelector`, which versions tables by selection time relative to a context object (the quote/transaction/segment):

```java
var row = ResourceSelector.get(quote)                 // or ResourceSelectorFactory.getInstance().getSelector(quote)
        .getTable(BaseRates.class)
        .getRecord(BaseRates.makeKey(sex, String.valueOf(age)));
BigDecimal base = row.map(r -> new BigDecimal(r.rate()))   // columns are usually String — convert
                     .orElseThrow(() -> new IllegalStateException("no BaseRates row for " + sex + "/" + age));
```

The `makeKey` argument order is the table's **key columns in declared order** (the contract prints the exact signature). Range tables (rate-by-band) use `getRangeTable(...).getRecord(key, boundValue)` and the `interpolate(...)`/`extrapolate(...)` helpers — see the bedrock `datafetcher.md` and the range-table pattern. Decide deliberately what to do on a **table miss**: a silent `orElse(BigDecimal.ONE)` hides a config/data gap; failing loud is usually correct for rating.

## Numeric discipline (do not copy the demos here)

- Money and rates are `BigDecimal`. Several demo raters compute in `double` (`baseRate.doubleValue() * faceAmount / 1000`) and wrap with `BigDecimal.valueOf(...)` — this leaks binary-float error into prices. **Stay in `BigDecimal`** end to end.
- Every `divide`/`setScale` needs an explicit `RoundingMode`; raw `divide` on a non-terminating decimal throws `ArithmeticException`. The platform convention is `HALF_EVEN`.
- Use `new MoneyService(currency).toMoney(amount)` to round a final amount to the currency scale; currency is `quote.currency()` / `policy.currency()` (an ISO string).
- `RatingItem`'s builder fields for `amount`/`rate` are `Optional`-typed (`.rate(Optional.of(v))` or, where the builder offers a `BigDecimal` overload, `.rate(v)` — confirm in the JAR). The bedrock `coremodel-classes.md` shows the canonical constructor and the deprecated positional constructor to avoid.

## The mental checklist before emitting an item

For each charge you intend to apply: (1) is this charge **granted** on this element in config? (2) is the element's **locator** the right `elementLocator`? (3) does the charge's **handling** say `.rate()` or `.amount()`? (4) if `.rate()`, is my value a **per-duration-unit rate**, not a term total? (5) am I emitting **exactly one** item for this `(locator, chargeType)`? If all five hold, the item is valid.
