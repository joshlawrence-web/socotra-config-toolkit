# Rating pattern catalog

Patterns keyed to **config shape**. Match the shape of the active config (from the rating contract + JAR scan) to a pattern, then adapt — these are skeletons of *correct structure*, not drop-in code. All assume `implements RatePlugin` in `package com.socotra.deployment.customer` (a sub-package like `…customer.acme` is allowed; the impl class is never annotated `@Plugin`).

> The EC demo raters are cited for **mechanics only**. They are *not* production-grade — the [Anti-patterns](#anti-patterns-seen-in-demo-raters) section lists what to fix when borrowing from them.

Imports common to the snippets:
```java
import com.socotra.coremodel.RatingItem;
import com.socotra.coremodel.RatingSet;
import com.socotra.deployment.ResourceSelector;
import com.socotra.deployment.customer.*;        // generated: ChargeType, <Product>*, table records
import com.socotra.platform.tools.ULID;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
```

---

## 1. Flat / constant charge (no tables)

**Config shape:** small set of charges, premium determined by a config field (a plan/tier), no rate tables. **When:** simplest products; prototypes.

```java
@Override
public RatingSet rate(AcmeQuoteRequest request) {
    AcmeQuote quote = request.quote();
    List<RatingItem> items = new ArrayList<>();
    BigDecimal premium = switch (quote.data().plan()) {        // a config enum/string field
        case "Gold"   -> new BigDecimal("59.88");
        case "Silver" -> new BigDecimal("5.00");
        default       -> throw new IllegalStateException("unknown plan: " + quote.data().plan());
    };
    items.add(RatingItem.builder()
        .elementLocator(quote.locator())
        .chargeType(ChargeType.premium)        // confirm constant exists in the JAR
        .rate(premium)                         // 'premium' has handling=normal -> .rate()
        .build());
    return RatingSet.builder().ok(true).ratingItems(items).build();
}
```

**Pitfalls:** if `plan` is a free-text string, compare case-insensitively and handle null; `default` should fail loudly, not silently return an empty set (a zero-charge quote is rarely intended).

---

## 2. Table-driven base rate × factors

**Config shape:** `tables/` with a base-rate table keyed by rating attributes, plus factor tables. **When:** the common real case. **Contract gives you** each table's `makeKey(...)` signature and key order.

```java
@Override
public RatingSet rate(TermLifeQuoteRequest request) {
    TermLifeQuote quote = request.quote();
    var data = quote.insured().data();
    ResourceSelector sel = ResourceSelector.get(quote);

    // Each table record type is distinct, so look up per-table (column accessors are String).
    BigDecimal baseRate = new BigDecimal(
        sel.getTable(BaseRates.class)
           .getRecord(BaseRates.makeKey(data.insuredSex(), String.valueOf(data.insuredIssueAge())))
           .orElseThrow(() -> new IllegalStateException("no BaseRates row"))
           .rate());
    BigDecimal modality = new BigDecimal(
        sel.getTable(Modality.class)
           .getRecord(Modality.makeKey(quote.data().mode()))
           .orElseThrow(() -> new IllegalStateException("no Modality row"))
           .value());

    // per-1000-of-face premium, then modal factor — all BigDecimal, explicit rounding
    BigDecimal faceThousands = new BigDecimal(data.faceAmount())
            .divide(new BigDecimal("1000"), 10, java.math.RoundingMode.HALF_EVEN);
    BigDecimal premium = baseRate.multiply(faceThousands).multiply(modality);

    List<RatingItem> items = new ArrayList<>();
    items.add(RatingItem.builder()
        .elementLocator(quote.locator())
        .chargeType(ChargeType.premium)
        .rate(premium)
        .build());
    return RatingSet.builder().ok(true).ratingItems(items).build();
}
```

**Pitfalls:** key columns are often `int`/`decimal` in config but `makeKey` takes `String` — stringify (`String.valueOf(age)`). Generated value columns return `String` — convert. Decide the table-miss behaviour deliberately (here: throw; a silent default hides config gaps). The real term-life demo does this in `double` — keep it `BigDecimal`.

---

## 3. Iterating repeating exposures

**Config shape:** product `contents` has a repeating element (`Location+`, `Vehicle+`); each instance is priced. **When:** commercial/auto/property. **Contract marks** these `quote.<name>s()[i]` (collection) and lists charges per exposure.

```java
@Override
public RatingSet rate(CommercialPropertyQuoteRequest request) {
    CommercialPropertyQuote quote = request.quote();
    ResourceSelector sel = ResourceSelector.get(quote);
    List<RatingItem> items = new ArrayList<>();

    for (LocationQuote loc : quote.locations()) {            // confirm accessor via the socotra-jar-building-block skill's JAR introspection
        BigDecimal locPremium = priceLocation(loc, sel);
        items.add(RatingItem.builder()
            .elementLocator(loc.locator())                  // charge lands on the exposure, not the product
            .chargeType(ChargeType.premium)
            .rate(locPremium)
            .build());
        // coverages on the exposure: null-check each optional peril/coverage child
        if (loc.flood() != null) {
            items.add(RatingItem.builder()
                .elementLocator(loc.flood().locator())
                .chargeType(ChargeType.premium)
                .rate(priceFlood(loc.flood(), sel))
                .build());
        }
    }
    return RatingSet.builder().ok(true).ratingItems(items).build();
}
```

**Pitfalls:** child coverage/peril accessors are **nullable** (not `Optional`) — null-check, don't `Optional`-chain them. Only charge a `(locator, chargeType)` the element declares (check the contract; the demo property config left some exposures with no `charges` array → contract ⚠️). Emit one item per `(locator, chargeType)`.

---

## 4. Multi-product overload sets

**Config shape:** several concrete products in one customer package (contract lists >1 concrete product; JAR shows one `rate(...)` trio per product). **When:** product families (e.g. France + Spain). Factor shared logic into a helper keyed by locator + the product's data; each overload adapts its typed request.

```java
@Override public RatingSet rate(AcmeFranceQuoteRequest r) { return priceQuote(r.quote().locator(), r.quote().data()); }
@Override public RatingSet rate(AcmeSpainQuoteRequest r)  { return priceQuote(r.quote().locator(), r.quote().data()); }

@Override public RatingSet rate(AcmeFranceRequest r) { return segment(r); }
@Override public RatingSet rate(AcmeSpainRequest  r) { return segment(r); }

private RatingSet segment(/* the shared request shape — both expose segment()/duration() */ ... r) {
    return r.segment()
        .map(seg -> priceSegment(seg.locator(), seg.data(), r.duration()))
        .orElseGet(() -> RatePlugin.super.rate(r));
}
```

**Pitfalls:** the per-product request types are **distinct classes** — you cannot write one method over both; share via a private helper that takes the extracted primitives (locator, data view, duration). Don't forget the per-product **segment-level** overloads (pattern 6).

---

## 5. Range / banded tables (rate-by-band, interpolation)

**Config shape:** a table with `rangeStart`/`rangeEnd` columns (contract marks it a *range table*). **When:** age bands, value bands, mileage bands.

```java
ResourceSelector sel = ResourceSelector.get(quote);
var fetcher = sel.getRangeTable(AgeBandRates.class);
byte[] key = AgeBandRates.makeKey(territory);               // non-range key columns
BigDecimal age = new BigDecimal(insured.data().issueAge());

// exact band:
BigDecimal factor = fetcher.getRecord(key, age)
        .map(r -> new BigDecimal(r.factor()))
        .orElseThrow(() -> new IllegalStateException("no age band for " + age));

// or interpolate between adjacent bands:
BigDecimal interp = fetcher.interpolate(key, age, r -> new BigDecimal(r.factor()), Interpolation.linear)
        .orElseThrow();   // empty unless both adjacent records exist
```

**Pitfalls:** `interpolate` returns empty unless **both** adjacent rows exist (use `extrapolate` for edges). `Interpolation` is `linear`/`stepUp`/`stepDown` — pick per the actuarial intent. See the bedrock `datafetcher.md` for the full `RangeTableRecordFetcher` surface.

---

## 6. Segment / transaction-level rating (the overload demos skip)

**Config shape:** any product that supports endorsements/renewals/cancellations (i.e. essentially all). **When:** a transaction after the initial quote is priced — issuance, change, renewal flow through `rate(<Product>Request)`, **not** the quote overload. Skipping it means changes/renewals don't reprice.

```java
@Override
public RatingSet rate(AcmeRequest request) {                // segment-level
    return request.segment()                                // Optional<AcmeSegment>
        .map(seg -> {
            ResourceSelector sel = ResourceSelector.get(request.transaction());   // context = transaction here
            return priceFrom(seg.locator(), seg.data(), sel, request.duration());
        })
        .orElseGet(() -> RatePlugin.super.rate(request));   // nothing to price -> platform default
}
```

**Pitfalls:** `request.segment()` is `Optional` — never `.get()` it unchecked (a recurring demo bug). The `ResourceSelector` context for segment-level rating is typically the **transaction** (`request.transaction()`), so table versioning aligns to the transaction's effective time. Share the actual pricing math with the quote overload.

---

## 7. Retention / cancellation charges

**Config shape:** config defines a `CancellationPlugin` (contract/JAR will show it; older datamodels omit it) and `retention`-handling charges. **When:** pricing the retained premium on cancellation. This is `CancellationPlugin`, not `RatePlugin`, but it returns rating items:

```java
// CancellationPlugin: returns CancellationPluginResponse(RatingSet retentionCharges)
RatingItem.builder()
    .elementLocator(segment.locator())
    .chargeType(ChargeType.retentionCharge)       // a charge with handling=retention
    .amount(retainedAmount)                       // retention -> .amount(), NOT .rate()
    .build();
```

**Pitfalls:** retention charges use `.amount()` (handling `retention`). See the bedrock `plugin-interfaces.md` `CancellationPlugin` section for the request/response records.

---

## Anti-patterns seen in demo raters

Borrow demo *mechanics*, not these habits:

- **`double` arithmetic for money.** `baseRate.doubleValue() * face / 1000` then `BigDecimal.valueOf(...)` leaks float error into prices. Stay in `BigDecimal` with explicit `HALF_EVEN` rounding.
- **Mutable instance/static fields on the plugin** (`private List<RatingItem> ratingItems;`, `private static double factor;` — seen in `ComPropRater`). A plugin instance may be reused across invocations; shared mutable state is a correctness and thread-safety bug. Build all state in **locals** per call.
- **Unchecked `Optional.get()`** on table lookups and `request.segment()`. A missing table row or absent segment then NPEs/throws opaquely. `map(...).orElseThrow(clear message)`.
- **Quote-only implementations.** Implementing only `rate(<Product>QuoteRequest)` leaves transactions/renewals unpriced (pattern 6).
- **Silent table-miss defaults** (`.orElse(BigDecimal.ONE)`) that mask config/data gaps. Fail loud in rating unless a default is genuinely intended.
- **Charging unconfigured elements** — emitting an item for a charge the element's config `charges` array doesn't grant. Rejected by `validateRatingSet`. Check the contract.
