# Generated Plugin Interfaces (com.socotra.deployment.customer)

Purpose: every plugin SPI interface generated from product config, with exact method overloads, request record shapes, triggers, and return semantics. The worked example throughout is the **single-product** config "ZenCover" (`generated-examples/zencover/`, built against core-datamodel v1.7.61). For a verified **multi-product** contrast, see `generated-examples/credit-card-protection/` (two products in one customer package, built against core-datamodel v1.6.180).

## What varies per configuration

**Stable across all configs**: interface names (`RatePlugin`, `ValidationPlugin`, ...), method names (`rate`, `validate`, `preCommit`, ...), the `@Plugin(type = PluginType.x)` annotation, the nested `<Name>PluginStub`, `VERSION` constant, and the `PluginRequest` contract (`Collection<?> structures()`).

Everything else is shaped by the specific config and the platform version it was built against. Verify against the target project's own generated sources (the `scripts/` introspection tooling exists for this) rather than assuming the ZenCover shapes. Verified variation axes:

1. **Request record names follow product names.** `<ProductName>QuoteRequest`, `<ProductName>QuickQuoteRequest`, `<ProductName>Request` (segment/transaction-level), `<ProductName>TransactionRequest`; the typed payload classes (`ZenCoverQuote`, `ZenCoverSegment`, `ZenCoverQuickQuote`) follow product names too. For a product named `Acme`, expect `AcmeQuoteRequest`, `AcmeQuote`, etc.

2. **A config may define multiple products — each plugin interface then carries one full overload set (and request-record set) per product.** Verified in `generated-examples/credit-card-protection/`: products `BasicCreditCardProtection` and `PremiumCreditCardProtection` share one customer package, and `RatePlugin` has 6 `rate(...)` overloads (quote / quick-quote / segment trio × 2 products), each with a `statelessRate` twin; `ValidationPlugin`, `UnderwritingPlugin`, `PreCommitPlugin`, `InstallmentsPlugin`, `DelinquencyEventPlugin`, and the document plugins are likewise duplicated per product. Real configs go further: among the 28 EC demo configs, products-per-config ranges 1–6 (renters: 6, term-life: 5, mobility-package-fr: 4), often including an `"abstract": true` base product. Compact multi-product contrast:

```java
// credit-card-protection RatePlugin — one trio per product:
default RatingSet rate(BasicCreditCardProtectionQuoteRequest request)
default RatingSet rate(BasicCreditCardProtectionQuickQuoteRequest request)
default RatingSet rate(BasicCreditCardProtectionRequest request)          // segment/transaction-level
default RatingSet rate(PremiumCreditCardProtectionQuoteRequest request)
default RatingSet rate(PremiumCreditCardProtectionQuickQuoteRequest request)
default RatingSet rate(PremiumCreditCardProtectionRequest request)
// + a statelessRate twin delegating to each of the six

record BasicCreditCardProtectionRequest(Policy policy, Transaction transaction,
       Optional<BasicCreditCardProtectionSegment> segment,
       BigDecimal duration, DurationBasis durationBasis) implements PluginRequest
```

3. **Overload presence follows config features.**
   - *Account types*: one account-level overload per configured account type, named after it — `PersonalAccountRequest` in ZenCover, `BankCustomerAccountRequest` in credit-card-protection. Demo configs define 1–4 account types (names are free-form: `Organization`, `CommercialAccount`, `Traveler`, ...), sometimes including abstract bases.
   - *Payment methods*: ZenCover configures a `StandardPayment` payment method, so `ValidationPlugin`/`PreCommitPlugin` carry `StandardPaymentRequest` overloads; credit-card-protection configures no payment methods and has **no** payment overloads at all. Demo configs range 0–3 payment types.
   - *Automations*: `@AutomationPlugin` interfaces exist only for configured automations (ZenCover: `CancellationInvoicesAutomationPlugin`; credit-card-protection: none).
   - *Quick quotes are system behaviour, not a config feature*: quick-quote overloads and `{Product}QuickQuote` types are generated for every concrete product regardless of config (confirmed in both example configs). Segment-level overloads likewise appeared for every concrete product in both examples.

4. **The generated SPI shape tracks the core-datamodel version the config was built against.** The `PluginType` registry grows across versions: v1.6.180 has only 11 constants (`rating, underwriting, validation, documentSelection, documentDataSnapshot, preCommit, renewal, delinquencyEvent, documentConsolidationSnapshot, installments, autopay` — verified via `javap` on the demo config's bundled jar), so packages generated against it cannot contain `CancellationPlugin`, `ConfigMigrationPlugin`, `PaymentPostProcessingPlugin`, `DocumentConsolidationSelectionPlugin`, or the Workplan plugins. Product-agnostic record shapes also drift — e.g. `AutopayPlugin.AutopayRequest` is `(Invoice invoice)` in the 1.6.180 example vs `(Invoice invoice, Boolean invoicingHoldActive)` in 1.7.61. Note the `stateless*` twin methods (`statelessRate`/`statelessValidate`/`statelessUnderwrite`) are present in **both** versions examined — do not assume they are 1.7.x-only.

5. **The interface SET itself varies per config build.** ZenCover (1.7.61) generates 18 plugin interfaces; credit-card-protection (1.6.180) generates 11 — absent: `CancellationPlugin`, `ConfigMigrationPlugin`, `PaymentPostProcessingPlugin`, `DocumentConsolidationSelectionPlugin`, `WorkplanSelectionPlugin`, `WorkplanExecutionPlugin`, and the automation plugin. Causes are a mix of axis 3 (config features) and axis 4 (platform version); enumerate the actual set in the target's generated package before writing an implementation.

Overload-as-dispatch: one plugin handles multiple lifecycle contexts via overloads on the request type. All methods are `default` (no-op-ish bodies), so implementations override only what they need. `stateless*` variants default to delegating to the stateful method.

The sections below document the full v1.7.61 surface using the ZenCover names; substitute your product/account/payment names and prune to the interfaces your build actually generates.

## RatePlugin — `@Plugin(type = PluginType.rating)`, VERSION 3

Triggered when a quote/quick-quote is priced or a transaction (issuance, change, renewal, ...) needs pricing. Returns `RatingSet` (`ok` flag + `Collection<RatingItem>`; one item per element locator + charge type, duplicates throw).

```java
default RatingSet rate(ZenCoverQuoteRequest request)
default RatingSet statelessRate(ZenCoverQuoteRequest request)        // delegates to rate(request)
default RatingSet rate(ZenCoverQuickQuoteRequest request)
default RatingSet statelessRate(ZenCoverQuickQuoteRequest request)
default RatingSet rate(ZenCoverRequest request)                      // segment/transaction-level
default RatingSet statelessRate(ZenCoverRequest request)
```

Request records:
```java
record ZenCoverQuoteRequest(ZenCoverQuote quote, BigDecimal duration, DurationBasis durationBasis) implements PluginRequest
record ZenCoverQuickQuoteRequest(ZenCoverQuickQuote quote, BigDecimal duration, DurationBasis durationBasis) implements PluginRequest
record ZenCoverRequest(Policy policy, Transaction transaction, Optional<ZenCoverSegment> segment,
                       BigDecimal duration, DurationBasis durationBasis) implements PluginRequest
```

Accessor pattern (records, so accessors are field-named methods):
```java
ZenCoverQuote quote = request.quote();
for (ItemQuote item : quote.items()) {                       // config-defined exposures
    BigDecimal price = item.data().purchasePrice();          // config-defined fields live on .data()
    if (item.theft() != null) { ULID loc = item.theft().locator(); }  // perils are nullable child records
}
// segment-level:
request.segment().ifPresent(seg -> seg.items().forEach(...)); // segment() is Optional<ZenCoverSegment>
```

## ValidationPlugin — `@Plugin(type = PluginType.validation)`, VERSION 3

Triggered on validate of accounts, quotes, quick quotes, transactions, payments. Returns `ValidationItem` (empty builder = pass; `addError(...)` entries block progression).

```java
default ValidationItem validate(ValidationPlugin.PersonalAccountRequest request)
default ValidationItem validate(ValidationPlugin.ZenCoverQuoteRequest request)
default ValidationItem statelessValidate(ValidationPlugin.ZenCoverQuoteRequest request)
default ValidationItem validate(ValidationPlugin.ZenCoverQuickQuoteRequest request)
default ValidationItem statelessValidate(ValidationPlugin.ZenCoverQuickQuoteRequest request)
default ValidationItem validate(ValidationPlugin.ZenCoverRequest request)
default ValidationItem statelessValidate(ValidationPlugin.ZenCoverRequest request)
default ValidationItem validate(ValidationPlugin.StandardPaymentRequest request)
```

```java
record PersonalAccountRequest(PersonalAccount account)
record ZenCoverQuoteRequest(ZenCoverQuote quote)
record ZenCoverQuickQuoteRequest(ZenCoverQuickQuote quote)
record ZenCoverRequest(Policy policy, Transaction transaction, Optional<ZenCoverSegment> segment)
record StandardPaymentRequest(StandardPayment payment)
```

Return idiom:
```java
return ValidationItem.builder()
    .elementType("ZenCoverQuote").locator(quote.locator())
    .errors(errors)            // or .addError("...") per error
    .build();                  // ValidationItem.builder().build() == no errors
```

## UnderwritingPlugin — `@Plugin(type = PluginType.underwriting)`, VERSION 2

Triggered at underwriting steps (quote and transaction level). Returns `UnderwritingModification(Collection<UnderwritingFlagCore> flagsToCreate, Collection<ULID> flagsToClear)`.

```java
default UnderwritingModification underwrite(UnderwritingPlugin.ZenCoverQuoteRequest request)
default UnderwritingModification underwrite(UnderwritingPlugin.ZenCoverRequest request)
default UnderwritingModification statelessUnderwrite(UnderwritingPlugin.ZenCoverQuoteRequest request)
default UnderwritingModification statelessUnderwrite(UnderwritingPlugin.ZenCoverRequest request)
```

```java
record ZenCoverQuoteRequest(ZenCoverQuote quote, Collection<UnderwritingFlag> flags)
record ZenCoverRequest(Policy policy, Transaction transaction, Optional<ZenCoverSegment> segment,
                       Collection<UnderwritingFlag> flags)
```

Gotcha: in both generated examples (v1.7.61 and v1.6.180), `of(...)` passes `List.of()` for `flags` (a generator TODO). Real implementations fetch flags themselves: `DataFetcher.getInstance().getQuoteUnderwritingFlags(quote.locator())` and dedupe by `flag.tag()`.

## PreCommitPlugin — `@Plugin(type = PluginType.preCommit)`, VERSION 1

Triggered just before commit of an entity (trigger values: `PreCommitTrigger.{create, update, validate, manual}`). Returns the (possibly mutated, via `toBuilder()`) entity — this is the hook for programmatic data fixes.

```java
default PersonalAccount preCommit(PreCommitPlugin.PersonalAccountRequest request)            // returns request.account()
default ZenCoverQuote preCommit(PreCommitPlugin.ZenCoverQuoteRequest request)                // returns request.quote()
default ZenCoverQuickQuote preCommit(PreCommitPlugin.ZenCoverQuickQuoteRequest request)
default PreCommitTransactionResponse preCommit(PreCommitPlugin.ZenCoverTransactionRequest request)
default ZenCoverSegment preCommit(PreCommitPlugin.ZenCoverRequest request)                   // returns request.segment()
default StandardPayment preCommit(PreCommitPlugin.StandardPaymentRequest request)
default PreCommitDelinquencyResponse preCommit(PreCommitPlugin.DelinquencyRequest request)
@Deprecated default PreCommitDelinquencyEventsResponse preCommit(PreCommitPlugin.DelinquencyEventsRequest request)
```

```java
record PersonalAccountRequest(PersonalAccount account, PreCommitTrigger trigger)
record ZenCoverQuoteRequest(ZenCoverQuote quote, PreCommitTrigger trigger)
record ZenCoverQuickQuoteRequest(ZenCoverQuickQuote quote, PreCommitTrigger trigger)
record ZenCoverTransactionRequest(Policy policy, Transaction transaction,
                                  Collection<ChangeInstructionHolder> changeInstructions, PreCommitTrigger trigger)
record ZenCoverRequest(Policy policy, Transaction transaction, ZenCoverSegment segment, PreCommitTrigger trigger)
       // NOTE: segment is NOT Optional here, unlike Rate/Validation/Underwriting requests
record StandardPaymentRequest(StandardPayment payment, PreCommitTrigger trigger)
record DelinquencyRequest(Delinquency delinquency)
record DelinquencyEventsRequest(Collection<DelinquencyEvent> delinquencyEvents)              // deprecated path
```

`PreCommitTransactionResponse(Collection<ChangeInstructionHolder> changeInstructions)`; `PreCommitDelinquencyResponse(DelinquencySettings settings, Optional<Instant> graceEndAt, Optional<Instant> lapseTransactionEffectiveDate)`.

Mutation idiom:
```java
ZenCoverQuote q = request.quote();
return q.toBuilder().data(q.data().toBuilder().gracePeriod(14).build()).build();
```

## InstallmentsPlugin — `@Plugin(type = PluginType.installments)`, VERSION 1

Triggered when an installment schedule is (re)generated. Returns `InstallmentsPluginResponse(Map<String, InstallmentUpdate> installmentUpdates)` keyed by installment locator string.

```java
default InstallmentsPluginResponse updateInstallments(InstallmentsPlugin.ZenCoverRequest request)

record ZenCoverRequest(InstallmentsPluginContext context, Collection<Installment> installments,
                       InstallmentLattice installmentLattice)
// InstallmentsPluginContext(ULID accountLocator, Optional<ULID> policyLocator,
//                           Optional<ULID> quoteLocator, Optional<ULID> transactionLocator)
```

## CancellationPlugin — `@Plugin(type = PluginType.cancellation)`, VERSION 1

Triggered when a cancellation transaction is priced. Returns `CancellationPluginResponse(RatingSet retentionCharges)` — rating items with `ChargeHandling.retention` charge types (amount required). Not generated by older datamodels (absent from the v1.6.180 example — see "What varies per configuration").

```java
default CancellationPluginResponse cancel(ZenCoverRequest request)

record ZenCoverRequest(Policy policy, Transaction transaction, ZenCoverSegment segment,
                       Collection<Charge> charges)        // charges = existing term charges; segment non-Optional
```

## RenewalPlugin — `@Plugin(type = PluginType.renewal)`, VERSION 1

Triggered on auto-renewal plan events. Return `AutoRenewalResponse` (all fields `Optional`: `autoRenewalState`, `renewalTransactionType` (String), `newTermDuration` (Integer), `renewalTransactionCreateTime/AcceptTime/IssueTime` (Instant)).

```java
default AutoRenewalResponse renew(AutoRenewalRequest request)

record AutoRenewalRequest(AutoRenewalEvent event, AutoRenewal autoRenewal) implements PluginRequest
```

## DelinquencyEventPlugin — `@Plugin(type = PluginType.delinquencyEvent)`, VERSION 1

Triggered per configured delinquency event. Returns `Map<String, DelinquencyEventUpdateRequest>`.

```java
default Map<String, DelinquencyEventUpdateRequest> triggerDelinquencyEvent(DelinquencyEventPlugin.ZenCoverRequest request)

record ZenCoverRequest(Delinquency delinquency, DelinquencyEvent delinquencyEvent)
```

## Document plugins

### DocumentSelectionPlugin — `@Plugin(type = PluginType.documentSelection)`, VERSION 1
Which configured documents to generate at a `DocumentTrigger` (`validated, priced, accepted, underwritten, issued, generated`). Returns `Map<String, DocumentSelectionAction>` keyed by `DocumentConfig.name()`; actions: `generate, noChange, generateIfAbsent, remove, defaultAction`.

```java
default Map<String, DocumentSelectionAction> selectDocuments(DocumentSelectionPlugin.ZenCoverQuoteRequest request)
default Map<String, DocumentSelectionAction> selectDocuments(DocumentSelectionPlugin.ZenCoverRequest request)

record ZenCoverQuoteRequest(ZenCoverQuote quote, Collection<DocumentConfig> documentConfigs, DocumentTrigger trigger)
record ZenCoverRequest(Policy policy, Transaction transaction, Optional<ZenCoverSegment> segment,
                       Collection<DocumentConfig> documentConfigs, DocumentTrigger trigger)
```

### DocumentDataSnapshotPlugin — `@Plugin(type = PluginType.documentDataSnapshot)`, VERSION 1
Builds the rendering payload for one document. Returns `DocumentDataSnapshot(Optional<Object> metadata, Object renderingData)` — `renderingData` is any Jackson-serializable object handed to the template.

```java
default DocumentDataSnapshot dataSnapshot(DocumentDataSnapshotPlugin.ZenCoverQuoteRequest request)
default DocumentDataSnapshot dataSnapshot(DocumentDataSnapshotPlugin.ZenCoverRequest request)
default DocumentDataSnapshot dataSnapshot(DocumentDataSnapshotPlugin.InvoiceDetailsRequest request)

record ZenCoverQuoteRequest(ZenCoverQuote quote, DocumentConfig config)
record ZenCoverRequest(Policy policy, Transaction transaction, Optional<ZenCoverSegment> segment, DocumentConfig config)
record InvoiceDetailsRequest(InvoiceDetails invoiceDetails, DocumentConfig config)
```

### DocumentConsolidationSelectionPlugin — `@Plugin(type = PluginType.documentConsolidationSelection)`, VERSION 1
Picks which generated documents go into a consolidated document; returns ordered `List<ULID>` of document locators.

```java
default List<ULID> selectDocuments(DocumentConsolidationSelectionPlugin.ZenCoverQuoteRequest request)
default List<ULID> selectDocuments(DocumentConsolidationSelectionPlugin.ZenCoverRequest request)

record ZenCoverQuoteRequest(ZenCoverQuote quote, ConsolidatedDocumentConfig config,
                            Collection<DocumentSummary> documents, String productName)
record ZenCoverRequest(Policy policy, Transaction transaction, Optional<ZenCoverSegment> segment,
                       ConsolidatedDocumentConfig config, Collection<DocumentSummary> documents)
```

### DocumentConsolidationSnapshotPlugin — `@Plugin(type = PluginType.documentConsolidationSnapshot)`, VERSION 1
Snapshot for the consolidated document. Same `DocumentDataSnapshot` return; requests mirror DocumentDataSnapshotPlugin's three overloads plus a `DocumentConsolidationInfo consolidationInfo` component (records also provide convenience constructors omitting `config`).

```java
default DocumentDataSnapshot consolidate(...ZenCoverQuoteRequest | ...ZenCoverRequest | ...InvoiceDetailsRequest)
record ZenCoverQuoteRequest(ZenCoverQuote quote, DocumentConfig config, DocumentConsolidationInfo consolidationInfo)
record ZenCoverRequest(Policy policy, Transaction transaction, Optional<ZenCoverSegment> segment,
                       DocumentConfig config, DocumentConsolidationInfo consolidationInfo)
record InvoiceDetailsRequest(InvoiceDetails invoiceDetails, DocumentConfig config, DocumentConsolidationInfo consolidationInfo)
```

## ConfigMigrationPlugin — `@Plugin(type = PluginType.configMigration)` (no VERSION constant)

Migrates persisted entities to a new config version. Unique signature shape: takes a `ConfigMigrationTransformer` (from `com.socotra.deployment.plugins`) as a second parameter, and request records hold the **interface** types (`com.socotra.coremodel.interfaces.Quote/Segment`, `Account`), not generated records. Not generated by older datamodels (absent from the v1.6.180 example).

```java
default PersonalAccount migrate(ConfigMigrationPlugin.PersonalAccountRequest request, ConfigMigrationTransformer transformer)
default ZenCoverQuote   migrate(ConfigMigrationPlugin.ZenCoverQuoteRequest request, ConfigMigrationTransformer transformer)
default ZenCoverSegment migrate(ConfigMigrationPlugin.ZenCoverRequest request, ConfigMigrationTransformer transformer)

record PersonalAccountRequest(Account account)
record ZenCoverQuoteRequest(Quote quote)
record ZenCoverRequest(Policy policy, Segment segment)
```

## Billing-side plugins

### AutopayPlugin — `@Plugin(type = PluginType.autopay)`, VERSION 1
```java
default AutopayPluginResponse autopay(AutopayRequest request)
record AutopayRequest(Invoice invoice, Boolean invoicingHoldActive)
```

### PaymentPostProcessingPlugin — `@Plugin(type = PluginType.paymentPostProcessing)`, VERSION 1
```java
default PaymentPostProcessingResponse postProcess(PaymentPostProcessingRequest request)
record PaymentPostProcessingRequest(PaymentPostProcessingContext context)
```

## Work management plugins

### WorkplanSelectionPlugin — `@Plugin(type = PluginType.workplanSelection)`
```java
default WorkplanSelectionResponse selectWorkplans(WorkplanSelectionRequest request)
record WorkplanSelectionRequest(WorkplanSelection workplansSelection)
// default: WorkplanSelectionResponse.builder().workplansToExecute(request.workplansSelection().workplans()).build()
```

### WorkplanExecutionPlugin — `@Plugin(type = PluginType.workplanExecution)`
```java
default WorkplanExecutionResponse decorateWorkplanExecution(WorkplanExecutionRequest request)
record WorkplanExecutionRequest(WorkplanExecution execution)
// default copies request.execution().tasks() and .associations() into the response builder
```

## Automation plugins — `@AutomationPlugin(type = "<Name>")`

Generated per configured automation (HTTP action or webhook). Not overload-dispatched: the interface declares **abstract** methods (no default) with fully generated request/response records that implement `Validatable` and carry builders + generated `validate(DeploymentConfig, ValidatableContext)`. Example (ZenCover):

```java
@AutomationPlugin(type = "CancellationInvoices")
public interface CancellationInvoicesAutomationPlugin {
  CancellationInvoicesLoadInvoiceAuxDataResponse loadInvoiceAuxData(CancellationInvoicesLoadInvoiceAuxDataRequest request);
}
// record CancellationInvoicesLoadInvoiceAuxDataRequest(String cancellationComments, String cancellationDate,
//                                                      String cancellationMode, String policyLocator)
// record CancellationInvoicesLoadInvoiceAuxDataResponse(Boolean result, String transactionLocator, String transactionState)
```

The implementation class (`CancellationInvoicesAutomationPluginImpl implements CancellationInvoicesAutomationPlugin`) must implement the abstract method(s).

## Implementation checklist

1. Class in package `com.socotra.deployment.customer`, `implements <GeneratedInterface>`.
2. `@Override` only the overloads you need; signatures must match the generated ones exactly (request types are nested records — refer to them unqualified inside the package, e.g. `ZenCoverQuoteRequest` or `ValidationPlugin.ZenCoverQuoteRequest`).
3. Never edit generated files; never annotate your impl class with `@Plugin` (the interface carries it).
4. Logging via slf4j (`LoggerFactory.getLogger(...)`) is the observed convention.
