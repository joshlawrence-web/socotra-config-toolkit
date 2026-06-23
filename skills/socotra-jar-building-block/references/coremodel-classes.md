# Core Model Classes Plugin Authors Actually Touch

Purpose: signature-accurate reference for the `com.socotra.coremodel` / `com.socotra.platform.tools` types used in plugin code, plus the conventions for finding everything else.

## Conventions (apply everywhere)

- Most coremodel types are Java **records**: accessors are component-named methods (`charge.amount()`, not `getAmount()`).
- Optional fields are `Optional<T>`; canonical constructors normalize `null -> Optional.empty()` and `null` collections -> `List.of()`, and `Objects.requireNonNull` the required components — building an incomplete record **throws at construction time**.
- Every record has `static XBuilder builder()` and instance `toBuilder()`; collection fields get `addX(item)` / `addXs(Collection)` builder methods; immutable-update idiom is `obj.toBuilder().field(v).build()`.
- **There is no Money class.** All monetary values are `java.math.BigDecimal`. Use `MoneyService(String currency).toMoney(amount)` to round to currency scale (HALF_EVEN); currency is a `String` ISO code on `Policy.currency()` / `Quote.currency()` (resolved via `java.util.Currency`).
- Finding the rest of the 635 source files: `grep -rn "public record <Name>(" sources/` or `grep -rn "public enum <Name>" sources/`. State enums are `<Entity>State`; request/response pairs are `<X>Request`/`<X>Response`; product-agnostic views live in `coremodel/interfaces/`.

## Rating

### RatingSet (record)
```java
public record RatingSet(Boolean ok, Collection<RatingItem> ratingItems)
```
Constructor rejects two items with the same `elementLocator` + `chargeType().name()`. Builder: `RatingSet.builder().ok(true).addRatingItem(item).build()` (also `.ratingItems(collection)`, `.addRatingItems(Collection)`; the varargs `addRatingItems(RatingItem...)` is `@Deprecated`).

### RatingItem (record)
```java
public record RatingItem(
    ULID elementLocator,
    ChargeType chargeType,                 // com.socotra.coremodel.ChargeType (your generated enum implements it)
    Optional<BigDecimal> amount,
    Optional<BigDecimal> rate,
    Optional<BigDecimal> referenceRate,
    Optional<String> tag)
```
Constructor rules (enforced, will throw):
- `chargeType.handling() == flat` or `retention` → `amount` required.
- `chargeType.handling() == normal` → `rate` required.
- At most **one** of `amount`/`rate` may be present.
- Empty `referenceRate` defaults to `rate`.
A `@Deprecated` positional constructor `(ULID, ChargeType, BigDecimal rate, BigDecimal referenceRate, Optional<String> tag)` exists — avoid it; use the builder:
```java
RatingItem.builder()
    .elementLocator(item.theft().locator())
    .chargeType(ChargeType.premium)        // generated com.socotra.deployment.customer.ChargeType
    .rate(Optional.of(annualRate))         // builder fields are Optional-typed for amount/rate
    .build();
```

### ChargeType (interface) — implemented by the generated `customer.ChargeType` enum
```java
public interface ChargeType {
  ChargeCategory category();
  String name();
  default ChargeInvoicing invoicing() { return ChargeInvoicing.scheduled; }
  default ChargeHandling handling() { return ChargeHandling.normal; }
  default Optional<Boolean> transactionBundlingEnabled() { return Optional.of(Boolean.FALSE); }
}
```

### Charge (record) — persisted, returned by pricing/term queries
```java
public record Charge(
    ULID locator, ULID elementLocator, String chargeType, ChargeCategory chargeCategory,
    BigDecimal amount, BigDecimal rate, BigDecimal referenceRate,
    Optional<String> tag, Optional<BigDecimal> rateDifference,
    ULID elementStaticLocator, Optional<ULID> reversalOfLocator,
    ChargeHandling handling, ChargeInvoicing invoicing)
```
Note `chargeType` here is a `String` (the name), unlike `RatingItem.chargeType()`.

### Chargeable (interface) — implemented by generated product/exposure/peril types
```java
public interface Chargeable {
  BigDecimal MAX_RATE = new BigDecimal("999999999.9999999999");
  BigDecimal MAX_AMOUNT = new BigDecimal("9999999999999999.999");
  ULID locator();
  Collection<? extends ChargeType> charges();
  void consumeChargeableElements(Consumer<Chargeable> consumer);
  default ValidationResult validateRatingSet(RatingSet ratingSet);  // checks items map to known elements/charges
}
```

## ULID (com.socotra.platform.tools)

`public class ULID implements Comparable<ULID>` — the universal locator type (16 bytes, 26-char Crockford string).
```java
public ULID(long msb, long lsb)
public static ULID generate()
public static ULID from(String value)        // parse canonical string
public static ULID from(byte[] bytes)
public static boolean isValid(String value)
public String toString()                     // 26-char uppercase
public String toLowerString()
public long getMostSignificantBits() / getLeastSignificantBits()
public long getTime()
public ULID increment()
```
Has Jackson serializer/deserializer built in — safe in rendering data and JSON payloads.

## Policy / Transaction / Term (coremodel records — product-independent)

```java
public record Policy(
    ULID locator, ULID accountLocator, Collection<ULID> branchHeadTransactionLocators,
    ULID issuedTransactionLocator, String productName, String timezone, String currency,
    DurationBasis durationBasis, Instant createdAt, UUID createdBy,
    Optional<String> delinquencyPlanName, Optional<String> autoRenewalPlanName,
    Instant startTime, Instant endTime, ULID latestTermLocator, BillingLevel billingLevel,
    Optional<byte[]> securityId, Optional<String> region, Optional<String> policyNumber,
    ULID latestSegmentLocator, Collection<ContactRoles> contacts, Collection<PolicyStatus> statuses,
    Optional<BigDecimal> invoiceFeeAmount, Optional<Instant> anonymizedAt, Optional<Boolean> checkHold,
    Optional<Instant> coverageEndTime, Map<String,String> moratoriumElections,
    Optional<String> jurisdiction,
    @Deprecated Optional<String> producerCode, @Deprecated Optional<String> producerCodeOfRecord,
    Optional<Boolean> migrateOnRenewal) implements com.socotra.coremodel.interfaces.Policy
```

```java
public record Transaction(
    ULID locator, TransactionCategory transactionCategory, TransactionState transactionState,
    Optional<String> underwritingStatus, ULID policyLocator,
    Optional<ULID> baseTransactionLocator, Optional<ULID> aggregateTransactionLocator,
    Instant createdAt, UUID createdBy, Optional<ValidationResult> validationResult,
    Collection<Object> changeInstructions, Instant effectiveTime,
    Collection<Transaction> aggregatedTransactions, ULID termLocator,
    Optional<Preferences> preferences, String transactionType,
    Optional<Instant> issuedTime, Optional<Instant> acceptedTime,
    Optional<ULID> reapplicationOfLocator, Optional<DataMaskingLevel> maskingLevel,
    Optional<Instant> anonymizedAt, ULID staticLocator) implements SensitiveObject
```
`transactionType` is the configured type **name** (String); `transactionCategory` is the enum.

```java
public record Term(
    ULID locator, ULID staticLocator, ULID policyLocator, Integer number,
    Optional<ULID> previousTermLocator, Optional<ULID> supersedesTermLocator,
    Instant startTime, Instant endTime, Optional<ULID> autoRenewalLocator,
    Optional<String> termNumber)
```

## Product-agnostic interfaces (coremodel/interfaces/) vs generated records

Generated product records implement these. Use the interfaces when writing product-independent helpers.

```java
public interface Quote extends QuoteCore {     // com.socotra.coremodel.interfaces
  QuoteState quoteState();
  ULID accountLocator();
  Optional<String> underwritingStatus();
  default Optional<ULID> policyLocator();
  default ULID groupLocator();
  default Optional<String> region() / quoteNumber() / reservedPolicyNumber();
  default Optional<BigDecimal> duration() / invoiceFeeAmount();
  default Optional<Instant> acceptedTime() / issuedTime();
  default Optional<ULID> quickQuoteLocator();
  default Optional<ValidationResult> validationResult();
  <T extends CustomerObject> T toCustomerObject(DeploymentFactory factory);
}
// QuoteCore adds startTime()/endTime()/timezone()/currency()/jurisdiction() etc. (all Optional)

public interface Segment {                     // com.socotra.coremodel.interfaces
  ULID locator();
  ULID transactionLocator();
  SegmentType segmentType();
  Instant startTime();
  Instant endTime();
  Element element();
  BigDecimal duration();
  Optional<ULID> basedOn();
  <T extends CustomerObject> T toCustomerObject(DeploymentFactory factory);
}
```

Generated example shapes (product "ZenCover" — names vary per product):
```java
public record ZenCoverQuote(
    ULID locator, ULID groupLocator, QuoteState quoteState, String productName, ULID accountLocator,
    Optional<Instant> createdAt, Optional<UUID> createdBy, Optional<Instant> startTime, Optional<Instant> endTime,
    Optional<String> timezone, Optional<String> currency, Optional<String> underwritingStatus,
    Optional<Instant> expirationTime, Optional<Preferences> preferences, Optional<ULID> policyLocator,
    Optional<DurationBasis> durationBasis, Optional<String> delinquencyPlanName, Optional<String> autoRenewalPlanName,
    Optional<String> region, Optional<String> jurisdiction, Optional<String> producerCode,
    Collection<ItemQuote> items,               // <- config-defined exposures
    ZenCoverQuoteData data,                    // <- config-defined fields: quote.data().discountAmount() etc.
    Element element, BillingLevel billingLevel, Optional<String> quoteNumber,
    Collection<ContactRoles> contacts, Optional<BigDecimal> invoiceFeeAmount,
    Optional<String> reservedPolicyNumber)
    implements ZenCover, Validatable<ZenCoverQuote>, com.socotra.coremodel.interfaces.Quote, ...

public record ZenCoverSegment(
    ULID locator, ULID transactionLocator, Optional<ULID> basedOn, SegmentType segmentType,
    Instant startTime, Instant endTime, BigDecimal duration,
    Collection<ItemPolicy> items, ZenCoverSegmentData data,
    Optional<ProducerInfo> producerInfo, Element element)
    implements ZenCover, ..., com.socotra.coremodel.interfaces.Segment, ...
```
Exposure interface vs per-context records: `Item` (interface, extends `Chargeable`) with `ItemQuote` / `ItemPolicy` / `ItemQuickQuote` record variants; peril child accessors (`item.theft()`, `item.breakdown()`) are **nullable**, not Optional. Field interfaces nest as `Item.ItemData` with plain accessors (`purchaseDate()`, `purchasePrice()`); quote/policy record variants carry concrete `ItemQuoteData` etc. with builders.

The element layering itself is config-defined, so do not assume an exposure collection exists. ZenCover nests coverages under a repeating `Item` exposure (`Collection<ItemQuote> items` on the quote); the credit-card-protection example has **no exposure layer** — its required (`!`) coverages appear as non-Optional direct components of the quote record (`FraudProtectionQuote fraudProtection, CardReplacementQuote cardReplacement, ...`). Demo configs range from zero to ~11 exposure types per config.

### Element (record) — generic tree node behind every generated entity
```java
public record Element(UUID tenantLocator, String type, ULID locator, ULID rootLocator,
                      ULID parentLocator, Collection<Element> elements, ...)
```

## Underwriting

```java
public record UnderwritingFlag(
    ULID locator, UnderwritingLevel level, Optional<String> note, Optional<ULID> elementLocator,
    UnderwritingReferenceType referenceType, ULID referenceLocator,
    Optional<Instant> clearedAt, Optional<String> tag)

public record UnderwritingFlags(Optional<ULID> reserved, Collection<UnderwritingFlag> flags,
                                Collection<UnderwritingFlag> clearedFlags)

public record UnderwritingFlagCore(          // what plugins CREATE
    UnderwritingLevel level, Optional<String> note, Optional<ULID> elementLocator,
    Optional<String> tag, Optional<TaskCreateRequest> taskCreation)

public record UnderwritingModification(Collection<UnderwritingFlagCore> flagsToCreate,
                                       Collection<ULID> flagsToClear)

public enum UnderwritingLevel { none("none"), info("none"), block("blocked"),
                                decline("declined"), reject("rejected"), approve("approved");
                                public String getStatusName(); }
```
Idiom:
```java
UnderwritingFlagCore.builder()
    .level(UnderwritingLevel.block)
    .elementLocator(Optional.of(item.locator()))
    .tag(Optional.of(ruleId))                  // tag = dedupe key across re-runs
    .note(Optional.of("reason"))
    .build();
return UnderwritingModification.builder().flagsToCreate(flags).build();
```

## Validation

```java
public record ValidationItem(String elementType, Optional<ULID> locator, Collection<String> errors)
// builder: .elementType(String).locator(ULID).addError(String).addErrors(Collection<String>)/.errors(...)
//          boolean hasErrors() on the builder

public record ValidationResult(Collection<ValidationItem> validationItems)
    implements com.socotra.coremodel.interfaces.ValidationResult
// constructor merges items sharing the same locator
```
Empty `ValidationItem.builder().build()` = success.

## Installments

```java
public record Installment(
    ULID locator, ULID installmentLatticeLocator, ULID accountLocator, String currency, String timezone,
    Integer installmentFrameIndex, Optional<ULID> quoteLocator, Optional<ULID> policyLocator,
    Optional<ULID> transactionLocator, Instant installmentStartTime, Instant installmentEndTime,
    Instant coverageStartTime, Instant coverageEndTime, BigDecimal installmentDuration,
    BigDecimal coverageDuration, Instant generateTime, Instant dueTime, Optional<ULID> invoiceLocator,
    Instant createdAt, UUID createdBy, Instant updatedAt, UUID updatedBy,
    Collection<InstallmentItem> installmentItems, Optional<ULID> reversalOfInstallmentLocator,
    Optional<ULID> termLocator, Optional<ULID> migratedFromInstallmentLocator,
    Optional<Instant> autopayTime, Optional<Boolean> enhancedByPlugin)

public record InstallmentLattice(
    ULID locator, Optional<ULID> settingsLocator, Instant createdAt, UUID createdBy, ULID accountLocator,
    Instant termStartTime, Instant termEndTime, Optional<ULID> termLocator, Optional<ULID> quoteLocator,
    Optional<ULID> policyLocator, String currency, String timezone, Optional<ULID> basedOnLocator,
    Instant effectiveTime, Optional<ULID> creatorTransactionLocator, LatticeCategory latticeCategory,
    Collection<ULID> reversedLatticeLocators, Collection<ULID> reReversedLatticeLocators,
    Collection<InstallmentLatticeFrame> frames) implements com.socotra.coremodel.interfaces.InstallmentLattice

public record InstallmentsPluginResponse(Map<String, InstallmentUpdate> installmentUpdates)
```

## Enums plugin code commonly needs

```java
public enum QuoteState { draft, validated, earlyUnderwritten, priced, underwritten, accepted,
                         issued, underwrittenBlocked, declined, rejected, refused, discarded }

public enum TransactionState { draft, initialized, validated, earlyUnderwritten, priced, underwritten,
                               accepted, issued, underwrittenBlocked, declined, rejected, refused,
                               discarded, invalidated, reversed }

public enum TransactionCategory { issuance, change, renewal, cancellation, reinstatement, reversal, aggregate }

public enum ChargeHandling { normal, flat, retention }
public enum ChargeInvoicing { scheduled, next, immediate }
public enum ChargeCategory { none, premium, tax, fee, credit, invoiceFee, cededPremium, nonFinancial, surcharge }
                            // each carries boolean isPayable (none/cededPremium/nonFinancial are false)

public enum DurationBasis { none, years, months, monthsE360, weeks, days, hours }  // toInt()/fromInt(int)

public enum PreCommitTrigger { create, update, validate, manual }
public enum DocumentTrigger { validated, priced, accepted, underwritten, issued, generated } // ordinal-ordered, isBefore()
public enum DocumentSelectionAction { generate, noChange, generateIfAbsent, remove, defaultAction }
```

`TransactionType` enum note: the coremodel has a `TransactionType` **interface** (`category()`, `costBearing()`); the concrete enum (`issuance`, `change`, custom types like `addItem`...) is **generated** per configuration in `com.socotra.deployment.customer` (one enum shared by all products in the config). Same pattern for `ChargeType`. `Transaction.transactionType()` returns the String name — compare with `TransactionType.addItem.name()`.

## Tables (for ResourceSelector lookups)

```java
public interface TableMetadata extends SelectableResource { byte[] makeKey(); }
public interface RangeTableMetadata extends TableMetadata {
  BigDecimal rangeStart(); BigDecimal rangeEnd(); String rangeStartName(); String rangeEndName();
}
// ConstraintTableMetadata for constraint tables (e.g. generated Counties record)
```
Generated table records add `static byte[] makeKey(String keyCol...)`, `static String getStaticName()`, `static SelectionTimeBasis getSelectionTimeBasis()`. Key helper: `TableUtils.makeKey(String... values)`. Number helper: `NumberUtils.trimScale(BigDecimal[, int maxScale[, RoundingMode]])` (default rounding HALF_EVEN).

## BigDecimal handling gotchas

- Platform rounding convention is `RoundingMode.HALF_EVEN` (MoneyService, NumberUtils, TimeService all use it).
- `RatingItem.rate` is a per-duration-unit rate; the platform multiplies by segment duration. Use `MoneyService.getRateForTargetAmount(amount, duration)` to convert a target total to a rate.
- Always `setScale`/`divide` with an explicit `RoundingMode` — raw `divide` on non-terminating decimals throws `ArithmeticException`.
