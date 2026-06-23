# DataFetcher and Runtime Services (core-datamodel v1.7.61)

Purpose: the complete method surface of `com.socotra.deployment.DataFetcher` plus the table fetchers, `ResourceSelector`, and the runtime service classes, with exact signatures from source.

## Obtaining an instance

```java
import com.socotra.deployment.DataFetcher;

DataFetcher dataFetcher = DataFetcher.getInstance();   // delegates to DataFetcherFactory.get()
```

`DataFetcherFactory.get()` is what generated request `of(...)` factories use; both forms are equivalent. Outside the platform runtime (local dev), `com.socotra.developer.APIDataFetcher` provides a REST-backed implementation.

## Generic-parameter convention (important)

Methods declared `<T> T getX(...)` return the **generated product type**; `T` is inferred from the assignment target. There is no runtime type token — assigning to the wrong type fails at runtime, not compile time.

```java
ZenCoverQuote quote = dataFetcher.getQuote(quoteLocator);          // T = ZenCoverQuote
ZenCoverSegment seg = dataFetcher.getSegmentByTransaction(txLoc);  // T = ZenCoverSegment
PersonalAccount acct = dataFetcher.getAccount(accountLocator);     // T = PersonalAccount
```

Bounded generics (`<T extends com.socotra.coremodel.interfaces.Contact<?>>` etc.) work the same way against generated subtypes. Non-generic methods (`getPolicy`, `getTransaction`, `getTerm`, ...) return stable coremodel records directly.

## Complete DataFetcher surface (all 40 methods, grouped)

All locator parameters are `com.socotra.platform.tools.ULID` unless noted.

Version note: this is the **v1.7.61** surface. The surface shrinks on older datamodels — v1.6.180 (the credit-card-protection example's pinned jar) lacks `getDiaries`, the `getProducer*` methods, `getQuoteGroup`, `getTermCharges`, and `getTermSubsegmentSummaries` (verified via `javap`). `javap` the target project's pinned core-datamodel jar before relying on a method.

### Account / Quote
```java
<T> T getAccount(ULID accountLocator);
<T> T getQuickQuote(ULID quickQuoteLocator);
<T> T getQuote(ULID quoteLocator);
QuoteGroup getQuoteGroup(ULID quoteGroupLocator);
QuotePricing getQuotePricing(ULID quoteLocator);                       // record: quoteLocator, accountLocator, quoteState, productName, startTime, endTime, duration, durationBasis, Collection<Charge> items
<T extends CustomerObject> T getQuoteStaticData(ULID locator);         // T = generated <Product>QuoteStaticData
```

### Policy / Transaction / Segment / Term
```java
Policy getPolicy(ULID policyLocator);
Transaction getTransaction(ULID transactionLocator);
TransactionPricing getTransactionPricing(ULID transactionLocator);     // record: transactionLocator, Collection<Charge> items, Collection<TransactionPricing> aggregated
Collection<AffectedTransaction> getAffectedTransactions(ULID transactionLocator);
@Deprecated <T> Collection<T> getSegments(ULID transactionLocator);    // DEPRECATED — use getSegmentByTransaction
<T> T getSegmentByTransaction(ULID transactionLocator);
<T> T getSegment(ULID segmentLocator);
Term getTerm(ULID termLocator);
Map<ULID, Collection<Charge>> getTermCharges(ULID termLocator);        // keyed by transaction locator
StreamingEntity<SubsegmentSummary> getTermSubsegmentSummaries(ULID termLocator);
Preferences getPreferences(ULID transactionLocator);
<T extends CustomerObject> T getPolicyStaticData(ULID locator);        // T = generated <Product>PolicyStaticData
```

Note: the generated request factories still call `getSegments(...)` and pick `segments.stream().max(Comparator.comparing(Segment::endTime))`; new plugin code should prefer `getSegmentByTransaction`.

### Underwriting
```java
UnderwritingFlags getQuoteUnderwritingFlags(ULID quoteLocator);
UnderwritingFlags getTransactionUnderwritingFlags(ULID transactionLocator);
UnderwritingFlag getUnderwritingFlag(ULID underwritingFlagLocator);
```

### Billing / Payments / Installments / Delinquency
```java
Invoice getInvoice(ULID invoiceLocator);
InvoiceDetails getInvoiceDetails(ULID invoiceLocator);
<T> T getPayment(ULID paymentLocator);                                 // T = generated payment type, e.g. StandardPayment
Installment getInstallment(ULID installmentLocator);
InstallmentLattice getInstallmentLattice(ULID installmentLatticeLocator);
Collection<DelinquencyEvent> getDelinquencyEvents(ULID delinquencyLocator, int offset, int count);
```

### Claims (FNOL) / Contacts
```java
<T extends com.socotra.coremodel.interfaces.Fnol<?>> T getFnol(ULID fnolLocator);
Collection<FnolLoss> getFnolLosses(ULID fnolLocator);
Collection<ULID> getFnolClaims(ULID fnolLocator);
Collection<ContactRoles> getFnolContacts(ULID fnolLocator);
<T extends com.socotra.coremodel.interfaces.Contact<?>> T getContact(ULID locator);
```

### Producer
```java
<T extends com.socotra.coremodel.interfaces.Producer<?>> T getProducer(ULID producerLocator);
<T extends com.socotra.coremodel.interfaces.ProducerCode<?>> T getProducerCode(ULID producerCodeLocator);
<T extends com.socotra.coremodel.interfaces.ProducerCode<?>> T getProducerCode(String code);   // overload by code string
<T extends com.socotra.coremodel.interfaces.ProducerLicense<?>> T getProducerLicense(ULID producerLicenseLocator);
<T extends com.socotra.coremodel.interfaces.ProducerAppointment<?>> T getProducerAppointment(ULID producerAppointmentLocator);
```

### Documents / Diaries / Aux data / Work management
```java
Collection<DocumentInstance> getQuoteDocuments(ULID quoteLocator);
Collection<DocumentInstance> getSegmentDocuments(ULID segmentLocator);
Collection<DocumentInstance> getDocumentsAttachedToTransaction(ULID transactionLocator);
Collection<DiaryEntry> getDiaries(DiaryReferenceType referenceType, ULID referenceLocator, int offset, int count);
AuxData getAuxData(String locator, String key);                        // locator is a String here, not ULID
AuxDataKeysSet getAuxDataKeys(String locator, int offset, int count);
Task getTask(ULID taskLocator);
UserAssociation getUserAssociation(ULID userAssociationLocator);
```

## Table lookups: ResourceSelector + fetchers

`ResourceSelector` resolves config tables versioned by selection time relative to a context object (quote, transaction, segment, policy, term):

```java
public interface ResourceSelector {
  static ResourceSelector get(Object forObject);   // forObject = the quote/transaction/segment driving time selection

  <T extends TableMetadata & CustomerObject> TableRecordFetcher<T> getTable(Class<T> tableType);
  <T extends RangeTableMetadata & CustomerObject> RangeTableRecordFetcher<T> getRangeTable(Class<T> tableType);
  ConstraintsFetcher getConstraints(Class<? extends ConstraintTableMetadata> tableType);
  <T> Optional<T> getSecret(Class<T> secretType);

  static Instant selectionTime(SelectionTimeBasis basis, Object o);
  static Optional<String> jurisdiction(Object object);   // works for Policy, QuoteCore, Transaction, Segment, Term
  static Map.Entry<String, SelectionTimeBasis> extractStaticNameAndBasis(Class<? extends SelectableResource> tableClass);
}
```

`ResourceSelectorFactory` is the abstract provider behind `ResourceSelector.get(...)` (`getInstance().getSelector(forObject)`); you never subclass it in plugin code.

### TableRecordFetcher
```java
public interface TableRecordFetcher<T extends TableMetadata & CustomerObject> {
  Optional<T> getRecord(byte[] key);
}
```

Keys are built with the generated static `makeKey(...)` on the table record (which calls `com.socotra.platform.tools.TableUtils.makeKey(String...)`):

```java
ResourceSelector selector = ResourceSelector.get(quote);
Optional<CountyRiskFactors> row = selector
    .getTable(CountyRiskFactors.class)
    .getRecord(CountyRiskFactors.makeKey(countyCode));
BigDecimal factor = row.map(r -> new BigDecimal(r.riskFactor())).orElse(BigDecimal.ONE);
```

Gotcha: generated table record columns are typically `String`; convert to `BigDecimal` yourself.

### RangeTableRecordFetcher
```java
public interface RangeTableRecordFetcher<T extends RangeTableMetadata & CustomerObject> {
  Optional<T> getRecord(byte[] key, BigDecimal boundValue);
  Optional<T> getLowerAdjacentRecord(byte[] key, BigDecimal boundValue);  // closest range with start <= boundValue
  Optional<T> getUpperAdjacentRecord(byte[] key, BigDecimal boundValue);  // closest range with start >= boundValue

  default Optional<BigDecimal> interpolate(byte[] key, BigDecimal boundValue,
      Function<T, ? extends Number> mapper, Interpolation method);
  default Optional<BigDecimal> extrapolate(byte[] key, BigDecimal boundValue,
      Function<T, ? extends Number> mapper, Interpolation method);
}
```

`Interpolation` (in `com.socotra.platform.tools`) is `enum { linear, stepUp, stepDown }` with `BigDecimal apply(BigDecimal x1, BigDecimal y1, BigDecimal x2, BigDecimal y2, BigDecimal x)`. `interpolate` returns `Optional.empty()` unless both adjacent records exist; `extrapolate` falls back to the nearest two records on one side.

### ConstraintsFetcher
```java
public interface ConstraintsFetcher {
  Collection<String> get(byte[] key);   // allowed values for a constraint table key
}
```

## Services

### MoneyService (final class — construct directly; there is no Money type, money is BigDecimal)
```java
public MoneyService(String currency)                                   // e.g. new MoneyService(policy.currency())
public BigDecimal toMoney(BigDecimal amount)                           // setScale(currency fraction digits, HALF_EVEN)
public BigDecimal getAdjustedRateForWholeUnitAmount(BigDecimal amount, BigDecimal duration)
public BigDecimal getRateForTargetAmount(BigDecimal amount, BigDecimal duration)   // amount / duration, HALF_EVEN
```

### TimeService (final class)
```java
public TimeService(String timezone, DurationBasis durationBasis)
public ZoneId timezone()
public Instant alignForward(Instant instant, ChronoUnit chronoUnit)
public Instant tomorrowMidnight()
public Instant todayMidnight()
public Instant addDuration(Instant instant, int duration)
public Instant addDuration(Instant instant, double duration)
public Instant addDuration(Instant instant, double duration, DurationBasis durationBasis)
public static LocalDateTime roundToSeconds(LocalDateTime localDateTime)
@Deprecated public BigDecimal calculateDuration(Instant startTime, Instant endTime)   // wrong for mid-term segments
public BigDecimal calculateDuration(Instant startTime, Instant endTime, Instant termStartTime)
```
Static constant: `TimeService.UTC` (`ZoneId.of("UTC")`). Always prefer the 3-arg `calculateDuration` — the 2-arg overload is deprecated because it miscomputes durations for segments inside a term.

### EventsService
```java
public interface EventsService {
  static EventsService getInstance();                       // EventsServiceFactory.get()
  void createEvent(EventType eventType, Object eventData);
  default void createEvent(EventType eventType);            // eventData = Map.of()
}
```
`EventType` is `com.socotra.coremodel.interfaces.EventType`; generated custom events (e.g. `TenantCustomEvent`) implement it.

### AuxDataService
```java
public interface AuxDataService {
  static AuxDataService getInstance();                      // AuxDataServiceFactory.get()
  void setAuxData(String locator, AuxDataSetCreateRequest request);
  void deleteAuxData(String locator, String key);
}
```
`AuxDataSetCreateRequest(Optional<String> auxDataSettingsName, Collection<AuxDataSet> auxData)` — builder available. Reads go through `DataFetcher.getAuxData / getAuxDataKeys`.

### SearchService
```java
public interface SearchService {
  static SearchService getInstance();                       // SearchServiceFactory.get()
  SearchConfigurationResponse getSearchConfiguration(String deployedVersion);
}
```

### StreamingEntity (returned by getTermSubsegmentSummaries)
```java
public interface StreamingEntity<T> extends AutoCloseable {
  Iterator<T> iterator();
  Stream<T> stream();
}
```
It is `AutoCloseable` — use try-with-resources.

## Deprecation summary
- `DataFetcher.getSegments(ULID)` — deprecated; use `getSegmentByTransaction(ULID)` / `getSegment(ULID)`.
- `TimeService.calculateDuration(Instant, Instant)` — deprecated; pass `termStartTime` as third arg.
