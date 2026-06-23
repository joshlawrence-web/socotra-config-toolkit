# Socotra plugin surface — ZenCover

Source JARs: `customer-config.jar` + `core-datamodel-v1.7.61.jar`
core-datamodel version: **1.7.61**
Detected product (1): **ZenCover**
Plugin interfaces: **18** · declared methods: **48**

Every signature below is read from the compiled JARs via `javap` — it is the
exact method surface a plugin implementation for this config may override.
The generated SPI shape (interface set, request-record overloads, presence of
methods like `statelessRate`) varies with the core-datamodel version and the
products defined; trust only what is listed here.

## AutopayPlugin

`com.socotra.deployment.customer.AutopayPlugin`

### Methods (1)

```java
default AutopayPluginResponse autopay(AutopayPlugin.AutopayRequest);
```

### Request / response types

#### `AutopayPlugin.AutopayRequest`

`com.socotra.deployment.customer.AutopayPlugin$AutopayRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `invoice()` | `Invoice` |
| `invoicingHoldActive()` | `Boolean` |

#### `AutopayPluginResponse`

`com.socotra.coremodel.AutopayPluginResponse`

| accessor | returns |
|---|---|
| `paymentRequest()` | `java.util.Optional<PaymentCreateRequest>` |
| `nextRequestTime()` | `java.util.Optional<java.time.Instant>` |

## CancellationInvoicesAutomationPlugin

`com.socotra.deployment.customer.CancellationInvoicesAutomationPlugin`

### Methods (1)

```java
abstract CancellationInvoicesAutomationPlugin.CancellationInvoicesLoadInvoiceAuxDataResponse loadInvoiceAuxData(CancellationInvoicesAutomationPlugin.CancellationInvoicesLoadInvoiceAuxDataRequest);
```

### Request / response types

#### `CancellationInvoicesAutomationPlugin.CancellationInvoicesLoadInvoiceAuxDataRequest`

`com.socotra.deployment.customer.CancellationInvoicesAutomationPlugin$CancellationInvoicesLoadInvoiceAuxDataRequest`

| accessor | returns |
|---|---|
| `cancellationComments()` | `String` |
| `cancellationDate()` | `String` |
| `cancellationMode()` | `String` |
| `policyLocator()` | `String` |

#### `CancellationInvoicesAutomationPlugin.CancellationInvoicesLoadInvoiceAuxDataResponse`

`com.socotra.deployment.customer.CancellationInvoicesAutomationPlugin$CancellationInvoicesLoadInvoiceAuxDataResponse`

| accessor | returns |
|---|---|
| `result()` | `Boolean` |
| `transactionLocator()` | `String` |
| `transactionState()` | `String` |

## CancellationPlugin

`com.socotra.deployment.customer.CancellationPlugin`

### Methods (1)

```java
default CancellationPluginResponse cancel(CancellationPlugin.ZenCoverRequest);
```

### Request / response types

#### `CancellationPlugin.ZenCoverRequest`

`com.socotra.deployment.customer.CancellationPlugin$ZenCoverRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `policy()` | `Policy` |
| `transaction()` | `Transaction` |
| `segment()` | `ZenCoverSegment` |
| `charges()` | `java.util.Collection<Charge>` |

#### `CancellationPluginResponse`

`com.socotra.coremodel.CancellationPluginResponse`

| accessor | returns |
|---|---|
| `retentionCharges()` | `RatingSet` |

## ConfigMigrationPlugin

`com.socotra.deployment.customer.ConfigMigrationPlugin`

### Methods (3)

```java
default PersonalAccount migrate(ConfigMigrationPlugin.PersonalAccountRequest, ConfigMigrationTransformer);
default ZenCoverQuote migrate(ConfigMigrationPlugin.ZenCoverQuoteRequest, ConfigMigrationTransformer);
default ZenCoverSegment migrate(ConfigMigrationPlugin.ZenCoverRequest, ConfigMigrationTransformer);
```

### Request / response types

#### `ConfigMigrationPlugin.PersonalAccountRequest`

`com.socotra.deployment.customer.ConfigMigrationPlugin$PersonalAccountRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `account()` | `Account` |

#### `PersonalAccount`

`com.socotra.deployment.customer.PersonalAccount`

| accessor | returns |
|---|---|
| `numberingTrigger()` | `NumberingTrigger` |
| `type()` | `String` |
| `anonymizeData()` | `PersonalAccount` |
| `contactSlots()` | `java.util.Map<String, ContactSlot>` |
| `locator()` | `ULID` |
| `data()` | `PersonalAccount.PersonalAccountData` |
| `delinquencyPlanName()` | `java.util.Optional<String>` |
| `autoRenewalPlanName()` | `java.util.Optional<String>` |
| `excessCreditPlanName()` | `java.util.Optional<String>` |
| `shortfallTolerancePlanName()` | `java.util.Optional<String>` |
| `preferences()` | `java.util.Optional<Preferences>` |
| `billingLevel()` | `BillingLevel` |
| `region()` | `java.util.Optional<String>` |
| `invoiceDocument()` | `java.util.Optional<String>` |
| `accountNumber()` | `java.util.Optional<String>` |
| `contacts()` | `java.util.Collection<ContactRoles>` |
| `paymentExecutionRetryPlanName()` | `java.util.Optional<String>` |

#### `ConfigMigrationPlugin.ZenCoverQuoteRequest`

`com.socotra.deployment.customer.ConfigMigrationPlugin$ZenCoverQuoteRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `quote()` | `Quote` |

#### `ZenCoverQuote`

`com.socotra.deployment.customer.ZenCoverQuote`

| accessor | returns |
|---|---|
| `contactSlots()` | `java.util.Map<String, ContactSlot>` |
| `numberingTrigger()` | `NumberingTrigger` |
| `type()` | `String` |
| `anonymizeData()` | `ZenCoverQuote` |
| `locator()` | `ULID` |
| `groupLocator()` | `ULID` |
| `quoteState()` | `QuoteState` |
| `productName()` | `String` |
| `accountLocator()` | `ULID` |
| `createdAt()` | `java.util.Optional<java.time.Instant>` |
| `createdBy()` | `java.util.Optional<java.util.UUID>` |
| `startTime()` | `java.util.Optional<java.time.Instant>` |
| `endTime()` | `java.util.Optional<java.time.Instant>` |
| `timezone()` | `java.util.Optional<String>` |
| `currency()` | `java.util.Optional<String>` |
| `underwritingStatus()` | `java.util.Optional<String>` |
| `expirationTime()` | `java.util.Optional<java.time.Instant>` |
| `preferences()` | `java.util.Optional<Preferences>` |
| `policyLocator()` | `java.util.Optional<ULID>` |
| `durationBasis()` | `java.util.Optional<DurationBasis>` |
| `delinquencyPlanName()` | `java.util.Optional<String>` |
| `autoRenewalPlanName()` | `java.util.Optional<String>` |
| `region()` | `java.util.Optional<String>` |
| `jurisdiction()` | `java.util.Optional<String>` |
| `producerCode()` | `java.util.Optional<String>` |
| `items()` | `java.util.Collection<ItemQuote>` |
| `data()` | `ZenCoverQuote.ZenCoverQuoteData` |
| `element()` | `Element` |
| `billingLevel()` | `BillingLevel` |
| `quoteNumber()` | `java.util.Optional<String>` |
| `contacts()` | `java.util.Collection<ContactRoles>` |
| `invoiceFeeAmount()` | `java.util.Optional<java.math.BigDecimal>` |
| `reservedPolicyNumber()` | `java.util.Optional<String>` |

#### `ConfigMigrationPlugin.ZenCoverRequest`

`com.socotra.deployment.customer.ConfigMigrationPlugin$ZenCoverRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `policy()` | `Policy` |
| `segment()` | `Segment` |

#### `ZenCoverSegment`

`com.socotra.deployment.customer.ZenCoverSegment`

| accessor | returns |
|---|---|
| `type()` | `String` |
| `anonymizeData()` | `ZenCoverSegment` |
| `originalEffectiveTime()` | `java.time.Instant` |
| `locator()` | `ULID` |
| `transactionLocator()` | `ULID` |
| `basedOn()` | `java.util.Optional<ULID>` |
| `segmentType()` | `SegmentType` |
| `startTime()` | `java.time.Instant` |
| `endTime()` | `java.time.Instant` |
| `duration()` | `java.math.BigDecimal` |
| `items()` | `java.util.Collection<ItemPolicy>` |
| `data()` | `ZenCoverSegment.ZenCoverSegmentData` |
| `producerInfo()` | `java.util.Optional<ProducerInfo>` |
| `element()` | `Element` |

## DelinquencyEventPlugin

`com.socotra.deployment.customer.DelinquencyEventPlugin`

### Methods (1)

```java
default java.util.Map<String, DelinquencyEventUpdateRequest> triggerDelinquencyEvent(DelinquencyEventPlugin.ZenCoverRequest);
```

### Request / response types

#### `DelinquencyEventPlugin.ZenCoverRequest`

`com.socotra.deployment.customer.DelinquencyEventPlugin$ZenCoverRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `delinquency()` | `Delinquency` |
| `delinquencyEvent()` | `DelinquencyEvent` |

#### `DelinquencyEventUpdateRequest`

`com.socotra.coremodel.DelinquencyEventUpdateRequest`

| accessor | returns |
|---|---|
| `delinquencyEventState()` | `java.util.Optional<DelinquencyEventState>` |
| `triggerTime()` | `java.util.Optional<java.time.Instant>` |

## DocumentConsolidationSelectionPlugin

`com.socotra.deployment.customer.DocumentConsolidationSelectionPlugin`

### Methods (2)

```java
default java.util.List<ULID> selectDocuments(DocumentConsolidationSelectionPlugin.ZenCoverQuoteRequest);
default java.util.List<ULID> selectDocuments(DocumentConsolidationSelectionPlugin.ZenCoverRequest);
```

### Request / response types

#### `DocumentConsolidationSelectionPlugin.ZenCoverQuoteRequest`

`com.socotra.deployment.customer.DocumentConsolidationSelectionPlugin$ZenCoverQuoteRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `quote()` | `ZenCoverQuote` |
| `config()` | `ConsolidatedDocumentConfig` |
| `documents()` | `java.util.Collection<DocumentSummary>` |
| `productName()` | `String` |

#### `ULID`

`com.socotra.platform.tools.ULID`

| accessor | returns |
|---|---|
| `toLowerString()` | `String` |
| `getMostSignificantBits()` | `long` |
| `getLeastSignificantBits()` | `long` |
| `generate()` | `ULID` |
| `getTime()` | `long` |
| `increment()` | `ULID` |

#### `DocumentConsolidationSelectionPlugin.ZenCoverRequest`

`com.socotra.deployment.customer.DocumentConsolidationSelectionPlugin$ZenCoverRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `policy()` | `Policy` |
| `transaction()` | `Transaction` |
| `segment()` | `java.util.Optional<ZenCoverSegment>` |
| `config()` | `ConsolidatedDocumentConfig` |
| `documents()` | `java.util.Collection<DocumentSummary>` |

## DocumentConsolidationSnapshotPlugin

`com.socotra.deployment.customer.DocumentConsolidationSnapshotPlugin`

### Methods (3)

```java
default DocumentDataSnapshot consolidate(DocumentConsolidationSnapshotPlugin.ZenCoverQuoteRequest);
default DocumentDataSnapshot consolidate(DocumentConsolidationSnapshotPlugin.ZenCoverRequest);
default DocumentDataSnapshot consolidate(DocumentConsolidationSnapshotPlugin.InvoiceDetailsRequest);
```

### Request / response types

#### `DocumentConsolidationSnapshotPlugin.ZenCoverQuoteRequest`

`com.socotra.deployment.customer.DocumentConsolidationSnapshotPlugin$ZenCoverQuoteRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `quote()` | `ZenCoverQuote` |
| `config()` | `DocumentConfig` |
| `consolidationInfo()` | `DocumentConsolidationInfo` |

#### `DocumentDataSnapshot`

`com.socotra.coremodel.DocumentDataSnapshot`

| accessor | returns |
|---|---|
| `metadata()` | `java.util.Optional<Object>` |
| `renderingData()` | `Object` |

#### `DocumentConsolidationSnapshotPlugin.ZenCoverRequest`

`com.socotra.deployment.customer.DocumentConsolidationSnapshotPlugin$ZenCoverRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `policy()` | `Policy` |
| `transaction()` | `Transaction` |
| `segment()` | `java.util.Optional<ZenCoverSegment>` |
| `config()` | `DocumentConfig` |
| `consolidationInfo()` | `DocumentConsolidationInfo` |

#### `DocumentConsolidationSnapshotPlugin.InvoiceDetailsRequest`

`com.socotra.deployment.customer.DocumentConsolidationSnapshotPlugin$InvoiceDetailsRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `invoiceDetails()` | `InvoiceDetails` |
| `config()` | `DocumentConfig` |
| `consolidationInfo()` | `DocumentConsolidationInfo` |

## DocumentDataSnapshotPlugin

`com.socotra.deployment.customer.DocumentDataSnapshotPlugin`

### Methods (3)

```java
default DocumentDataSnapshot dataSnapshot(DocumentDataSnapshotPlugin.ZenCoverQuoteRequest);
default DocumentDataSnapshot dataSnapshot(DocumentDataSnapshotPlugin.ZenCoverRequest);
default DocumentDataSnapshot dataSnapshot(DocumentDataSnapshotPlugin.InvoiceDetailsRequest);
```

### Request / response types

#### `DocumentDataSnapshotPlugin.ZenCoverQuoteRequest`

`com.socotra.deployment.customer.DocumentDataSnapshotPlugin$ZenCoverQuoteRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `quote()` | `ZenCoverQuote` |
| `config()` | `DocumentConfig` |

#### `DocumentDataSnapshot`

`com.socotra.coremodel.DocumentDataSnapshot`

| accessor | returns |
|---|---|
| `metadata()` | `java.util.Optional<Object>` |
| `renderingData()` | `Object` |

#### `DocumentDataSnapshotPlugin.ZenCoverRequest`

`com.socotra.deployment.customer.DocumentDataSnapshotPlugin$ZenCoverRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `policy()` | `Policy` |
| `transaction()` | `Transaction` |
| `segment()` | `java.util.Optional<ZenCoverSegment>` |
| `config()` | `DocumentConfig` |

#### `DocumentDataSnapshotPlugin.InvoiceDetailsRequest`

`com.socotra.deployment.customer.DocumentDataSnapshotPlugin$InvoiceDetailsRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `invoiceDetails()` | `InvoiceDetails` |
| `config()` | `DocumentConfig` |

## DocumentSelectionPlugin

`com.socotra.deployment.customer.DocumentSelectionPlugin`

### Methods (2)

```java
default java.util.Map<String, DocumentSelectionAction> selectDocuments(DocumentSelectionPlugin.ZenCoverQuoteRequest);
default java.util.Map<String, DocumentSelectionAction> selectDocuments(DocumentSelectionPlugin.ZenCoverRequest);
```

### Request / response types

#### `DocumentSelectionPlugin.ZenCoverQuoteRequest`

`com.socotra.deployment.customer.DocumentSelectionPlugin$ZenCoverQuoteRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `quote()` | `ZenCoverQuote` |
| `documentConfigs()` | `java.util.Collection<DocumentConfig>` |
| `trigger()` | `DocumentTrigger` |

#### `DocumentSelectionAction`

`com.socotra.coremodel.DocumentSelectionAction`

| accessor | returns |
|---|---|
| `values()` | `DocumentSelectionAction[]` |

#### `DocumentSelectionPlugin.ZenCoverRequest`

`com.socotra.deployment.customer.DocumentSelectionPlugin$ZenCoverRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `policy()` | `Policy` |
| `transaction()` | `Transaction` |
| `segment()` | `java.util.Optional<ZenCoverSegment>` |
| `documentConfigs()` | `java.util.Collection<DocumentConfig>` |
| `trigger()` | `DocumentTrigger` |

## InstallmentsPlugin

`com.socotra.deployment.customer.InstallmentsPlugin`

### Methods (1)

```java
default InstallmentsPluginResponse updateInstallments(InstallmentsPlugin.ZenCoverRequest);
```

### Request / response types

#### `InstallmentsPlugin.ZenCoverRequest`

`com.socotra.deployment.customer.InstallmentsPlugin$ZenCoverRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `context()` | `InstallmentsPluginContext` |
| `installments()` | `java.util.Collection<Installment>` |
| `installmentLattice()` | `InstallmentLattice` |

#### `InstallmentsPluginResponse`

`com.socotra.coremodel.InstallmentsPluginResponse`

| accessor | returns |
|---|---|
| `installmentUpdates()` | `java.util.Map<String, InstallmentUpdate>` |

## PaymentPostProcessingPlugin

`com.socotra.deployment.customer.PaymentPostProcessingPlugin`

### Methods (1)

```java
default PaymentPostProcessingResponse postProcess(PaymentPostProcessingPlugin.PaymentPostProcessingRequest);
```

### Request / response types

#### `PaymentPostProcessingPlugin.PaymentPostProcessingRequest`

`com.socotra.deployment.customer.PaymentPostProcessingPlugin$PaymentPostProcessingRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `context()` | `PaymentPostProcessingContext` |

#### `PaymentPostProcessingResponse`

`com.socotra.coremodel.PaymentPostProcessingResponse`

| accessor | returns |
|---|---|
| `nextRequestTime()` | `java.util.Optional<java.time.Instant>` |
| `note()` | `java.util.Optional<String>` |
| `amount()` | `java.util.Optional<java.math.BigDecimal>` |
| `paymentState()` | `java.util.Optional<PaymentState>` |

## PreCommitPlugin

`com.socotra.deployment.customer.PreCommitPlugin`

### Methods (8)

```java
default PersonalAccount preCommit(PreCommitPlugin.PersonalAccountRequest);
default ZenCoverQuote preCommit(PreCommitPlugin.ZenCoverQuoteRequest);
default ZenCoverQuickQuote preCommit(PreCommitPlugin.ZenCoverQuickQuoteRequest);
default PreCommitTransactionResponse preCommit(PreCommitPlugin.ZenCoverTransactionRequest);
default ZenCoverSegment preCommit(PreCommitPlugin.ZenCoverRequest);
default StandardPayment preCommit(PreCommitPlugin.StandardPaymentRequest);
default PreCommitDelinquencyResponse preCommit(PreCommitPlugin.DelinquencyRequest);
default PreCommitDelinquencyEventsResponse preCommit(PreCommitPlugin.DelinquencyEventsRequest);
```

### Request / response types

#### `PreCommitPlugin.PersonalAccountRequest`

`com.socotra.deployment.customer.PreCommitPlugin$PersonalAccountRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `account()` | `PersonalAccount` |
| `trigger()` | `PreCommitTrigger` |

#### `PersonalAccount`

`com.socotra.deployment.customer.PersonalAccount`

| accessor | returns |
|---|---|
| `numberingTrigger()` | `NumberingTrigger` |
| `type()` | `String` |
| `anonymizeData()` | `PersonalAccount` |
| `contactSlots()` | `java.util.Map<String, ContactSlot>` |
| `locator()` | `ULID` |
| `data()` | `PersonalAccount.PersonalAccountData` |
| `delinquencyPlanName()` | `java.util.Optional<String>` |
| `autoRenewalPlanName()` | `java.util.Optional<String>` |
| `excessCreditPlanName()` | `java.util.Optional<String>` |
| `shortfallTolerancePlanName()` | `java.util.Optional<String>` |
| `preferences()` | `java.util.Optional<Preferences>` |
| `billingLevel()` | `BillingLevel` |
| `region()` | `java.util.Optional<String>` |
| `invoiceDocument()` | `java.util.Optional<String>` |
| `accountNumber()` | `java.util.Optional<String>` |
| `contacts()` | `java.util.Collection<ContactRoles>` |
| `paymentExecutionRetryPlanName()` | `java.util.Optional<String>` |

#### `PreCommitPlugin.ZenCoverQuoteRequest`

`com.socotra.deployment.customer.PreCommitPlugin$ZenCoverQuoteRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `quote()` | `ZenCoverQuote` |
| `trigger()` | `PreCommitTrigger` |

#### `ZenCoverQuote`

`com.socotra.deployment.customer.ZenCoverQuote`

| accessor | returns |
|---|---|
| `contactSlots()` | `java.util.Map<String, ContactSlot>` |
| `numberingTrigger()` | `NumberingTrigger` |
| `type()` | `String` |
| `anonymizeData()` | `ZenCoverQuote` |
| `locator()` | `ULID` |
| `groupLocator()` | `ULID` |
| `quoteState()` | `QuoteState` |
| `productName()` | `String` |
| `accountLocator()` | `ULID` |
| `createdAt()` | `java.util.Optional<java.time.Instant>` |
| `createdBy()` | `java.util.Optional<java.util.UUID>` |
| `startTime()` | `java.util.Optional<java.time.Instant>` |
| `endTime()` | `java.util.Optional<java.time.Instant>` |
| `timezone()` | `java.util.Optional<String>` |
| `currency()` | `java.util.Optional<String>` |
| `underwritingStatus()` | `java.util.Optional<String>` |
| `expirationTime()` | `java.util.Optional<java.time.Instant>` |
| `preferences()` | `java.util.Optional<Preferences>` |
| `policyLocator()` | `java.util.Optional<ULID>` |
| `durationBasis()` | `java.util.Optional<DurationBasis>` |
| `delinquencyPlanName()` | `java.util.Optional<String>` |
| `autoRenewalPlanName()` | `java.util.Optional<String>` |
| `region()` | `java.util.Optional<String>` |
| `jurisdiction()` | `java.util.Optional<String>` |
| `producerCode()` | `java.util.Optional<String>` |
| `items()` | `java.util.Collection<ItemQuote>` |
| `data()` | `ZenCoverQuote.ZenCoverQuoteData` |
| `element()` | `Element` |
| `billingLevel()` | `BillingLevel` |
| `quoteNumber()` | `java.util.Optional<String>` |
| `contacts()` | `java.util.Collection<ContactRoles>` |
| `invoiceFeeAmount()` | `java.util.Optional<java.math.BigDecimal>` |
| `reservedPolicyNumber()` | `java.util.Optional<String>` |

#### `PreCommitPlugin.ZenCoverQuickQuoteRequest`

`com.socotra.deployment.customer.PreCommitPlugin$ZenCoverQuickQuoteRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `quote()` | `ZenCoverQuickQuote` |
| `trigger()` | `PreCommitTrigger` |

#### `ZenCoverQuickQuote`

`com.socotra.deployment.customer.ZenCoverQuickQuote`

| accessor | returns |
|---|---|
| `contactSlots()` | `java.util.Map<String, ContactSlot>` |
| `type()` | `String` |
| `anonymizeData()` | `ZenCoverQuickQuote` |
| `locator()` | `ULID` |
| `quickQuoteState()` | `QuickQuoteState` |
| `productName()` | `String` |
| `accountLocator()` | `java.util.Optional<ULID>` |
| `startTime()` | `java.util.Optional<java.time.Instant>` |
| `endTime()` | `java.util.Optional<java.time.Instant>` |
| `timezone()` | `java.util.Optional<String>` |
| `currency()` | `java.util.Optional<String>` |
| `expirationTime()` | `java.util.Optional<java.time.Instant>` |
| `createdAt()` | `java.util.Optional<java.time.Instant>` |
| `createdBy()` | `java.util.Optional<java.util.UUID>` |
| `durationBasis()` | `java.util.Optional<DurationBasis>` |
| `jurisdiction()` | `java.util.Optional<String>` |
| `items()` | `java.util.Collection<ItemQuickQuote>` |
| `data()` | `ZenCoverQuickQuote.ZenCoverQuickQuoteData` |
| `element()` | `Element` |
| `contacts()` | `java.util.Collection<ContactRoles>` |

#### `PreCommitPlugin.ZenCoverTransactionRequest`

`com.socotra.deployment.customer.PreCommitPlugin$ZenCoverTransactionRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `policy()` | `Policy` |
| `transaction()` | `Transaction` |
| `changeInstructions()` | `java.util.Collection<ChangeInstructionHolder>` |
| `trigger()` | `PreCommitTrigger` |

#### `PreCommitTransactionResponse`

`com.socotra.coremodel.PreCommitTransactionResponse`

| accessor | returns |
|---|---|
| `changeInstructions()` | `java.util.Collection<ChangeInstructionHolder>` |

#### `PreCommitPlugin.ZenCoverRequest`

`com.socotra.deployment.customer.PreCommitPlugin$ZenCoverRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `policy()` | `Policy` |
| `transaction()` | `Transaction` |
| `segment()` | `ZenCoverSegment` |
| `trigger()` | `PreCommitTrigger` |

#### `ZenCoverSegment`

`com.socotra.deployment.customer.ZenCoverSegment`

| accessor | returns |
|---|---|
| `type()` | `String` |
| `anonymizeData()` | `ZenCoverSegment` |
| `originalEffectiveTime()` | `java.time.Instant` |
| `locator()` | `ULID` |
| `transactionLocator()` | `ULID` |
| `basedOn()` | `java.util.Optional<ULID>` |
| `segmentType()` | `SegmentType` |
| `startTime()` | `java.time.Instant` |
| `endTime()` | `java.time.Instant` |
| `duration()` | `java.math.BigDecimal` |
| `items()` | `java.util.Collection<ItemPolicy>` |
| `data()` | `ZenCoverSegment.ZenCoverSegmentData` |
| `producerInfo()` | `java.util.Optional<ProducerInfo>` |
| `element()` | `Element` |

#### `PreCommitPlugin.StandardPaymentRequest`

`com.socotra.deployment.customer.PreCommitPlugin$StandardPaymentRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `payment()` | `StandardPayment` |
| `trigger()` | `PreCommitTrigger` |

#### `StandardPayment`

`com.socotra.deployment.customer.StandardPayment`

| accessor | returns |
|---|---|
| `type()` | `String` |
| `anonymizeData()` | `StandardPayment` |
| `state()` | `PaymentState` |
| `locator()` | `ULID` |
| `paymentState()` | `PaymentState` |
| `amount()` | `java.math.BigDecimal` |
| `currency()` | `String` |
| `targets()` | `java.util.List<CreditItem>` |
| `accountLocator()` | `java.util.Optional<ULID>` |
| `financialInstrumentLocator()` | `java.util.Optional<ULID>` |
| `transactionMethod()` | `java.util.Optional<ExternalCashTransactionMethod>` |
| `transactionNumber()` | `java.util.Optional<String>` |
| `paymentMode()` | `java.util.Optional<PaymentMode>` |
| `aggregatePaymentLocator()` | `java.util.Optional<ULID>` |
| `subpayments()` | `java.util.Collection<Subpayment>` |
| `data()` | `StandardPayment.StandardPaymentData` |

#### `PreCommitPlugin.DelinquencyRequest`

`com.socotra.deployment.customer.PreCommitPlugin$DelinquencyRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `delinquency()` | `Delinquency` |

#### `PreCommitDelinquencyResponse`

`com.socotra.coremodel.PreCommitDelinquencyResponse`

| accessor | returns |
|---|---|
| `settings()` | `DelinquencySettings` |
| `graceEndAt()` | `java.util.Optional<java.time.Instant>` |
| `lapseTransactionEffectiveDate()` | `java.util.Optional<java.time.Instant>` |

#### `PreCommitPlugin.DelinquencyEventsRequest`

`com.socotra.deployment.customer.PreCommitPlugin$DelinquencyEventsRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `delinquencyEvents()` | `java.util.Collection<DelinquencyEvent>` |

#### `PreCommitDelinquencyEventsResponse`

`com.socotra.coremodel.PreCommitDelinquencyEventsResponse`

| accessor | returns |
|---|---|
| `updateRequests()` | `java.util.Map<String, DelinquencyEventUpdateRequest>` |
| `removalLocators()` | `java.util.Collection<ULID>` |

## RatePlugin

`com.socotra.deployment.customer.RatePlugin`

### Methods (6)

```java
default RatingSet rate(RatePlugin.ZenCoverQuoteRequest);
default RatingSet statelessRate(RatePlugin.ZenCoverQuoteRequest);
default RatingSet rate(RatePlugin.ZenCoverQuickQuoteRequest);
default RatingSet statelessRate(RatePlugin.ZenCoverQuickQuoteRequest);
default RatingSet rate(RatePlugin.ZenCoverRequest);
default RatingSet statelessRate(RatePlugin.ZenCoverRequest);
```

### Request / response types

#### `RatePlugin.ZenCoverQuoteRequest`

`com.socotra.deployment.customer.RatePlugin$ZenCoverQuoteRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `quote()` | `ZenCoverQuote` |
| `duration()` | `java.math.BigDecimal` |
| `durationBasis()` | `DurationBasis` |

#### `RatingSet`

`com.socotra.coremodel.RatingSet`

| accessor | returns |
|---|---|
| `ok()` | `Boolean` |
| `ratingItems()` | `java.util.Collection<RatingItem>` |

#### `RatePlugin.ZenCoverQuickQuoteRequest`

`com.socotra.deployment.customer.RatePlugin$ZenCoverQuickQuoteRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `quote()` | `ZenCoverQuickQuote` |
| `duration()` | `java.math.BigDecimal` |
| `durationBasis()` | `DurationBasis` |

#### `RatePlugin.ZenCoverRequest`

`com.socotra.deployment.customer.RatePlugin$ZenCoverRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `policy()` | `Policy` |
| `transaction()` | `Transaction` |
| `segment()` | `java.util.Optional<ZenCoverSegment>` |
| `duration()` | `java.math.BigDecimal` |
| `durationBasis()` | `DurationBasis` |

## RenewalPlugin

`com.socotra.deployment.customer.RenewalPlugin`

### Methods (1)

```java
default AutoRenewalResponse renew(RenewalPlugin.AutoRenewalRequest);
```

### Request / response types

#### `RenewalPlugin.AutoRenewalRequest`

`com.socotra.deployment.customer.RenewalPlugin$AutoRenewalRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `event()` | `AutoRenewalEvent` |
| `autoRenewal()` | `AutoRenewal` |

#### `AutoRenewalResponse`

`com.socotra.coremodel.AutoRenewalResponse`

| accessor | returns |
|---|---|
| `autoRenewalState()` | `java.util.Optional<AutoRenewalState>` |
| `renewalTransactionType()` | `java.util.Optional<String>` |
| `newTermDuration()` | `java.util.Optional<Integer>` |
| `renewalTransactionCreateTime()` | `java.util.Optional<java.time.Instant>` |
| `renewalTransactionAcceptTime()` | `java.util.Optional<java.time.Instant>` |
| `renewalTransactionIssueTime()` | `java.util.Optional<java.time.Instant>` |

## UnderwritingPlugin

`com.socotra.deployment.customer.UnderwritingPlugin`

### Methods (4)

```java
default UnderwritingModification underwrite(UnderwritingPlugin.ZenCoverQuoteRequest);
default UnderwritingModification underwrite(UnderwritingPlugin.ZenCoverRequest);
default UnderwritingModification statelessUnderwrite(UnderwritingPlugin.ZenCoverQuoteRequest);
default UnderwritingModification statelessUnderwrite(UnderwritingPlugin.ZenCoverRequest);
```

### Request / response types

#### `UnderwritingPlugin.ZenCoverQuoteRequest`

`com.socotra.deployment.customer.UnderwritingPlugin$ZenCoverQuoteRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `quote()` | `ZenCoverQuote` |
| `flags()` | `java.util.Collection<UnderwritingFlag>` |

#### `UnderwritingModification`

`com.socotra.coremodel.UnderwritingModification`

| accessor | returns |
|---|---|
| `flagsToCreate()` | `java.util.Collection<UnderwritingFlagCore>` |
| `flagsToClear()` | `java.util.Collection<ULID>` |

#### `UnderwritingPlugin.ZenCoverRequest`

`com.socotra.deployment.customer.UnderwritingPlugin$ZenCoverRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `policy()` | `Policy` |
| `transaction()` | `Transaction` |
| `segment()` | `java.util.Optional<ZenCoverSegment>` |
| `flags()` | `java.util.Collection<UnderwritingFlag>` |

## ValidationPlugin

`com.socotra.deployment.customer.ValidationPlugin`

### Methods (8)

```java
default ValidationItem validate(ValidationPlugin.PersonalAccountRequest);
default ValidationItem validate(ValidationPlugin.ZenCoverQuoteRequest);
default ValidationItem statelessValidate(ValidationPlugin.ZenCoverQuoteRequest);
default ValidationItem validate(ValidationPlugin.ZenCoverQuickQuoteRequest);
default ValidationItem statelessValidate(ValidationPlugin.ZenCoverQuickQuoteRequest);
default ValidationItem validate(ValidationPlugin.ZenCoverRequest);
default ValidationItem statelessValidate(ValidationPlugin.ZenCoverRequest);
default ValidationItem validate(ValidationPlugin.StandardPaymentRequest);
```

### Request / response types

#### `ValidationPlugin.PersonalAccountRequest`

`com.socotra.deployment.customer.ValidationPlugin$PersonalAccountRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `account()` | `PersonalAccount` |

#### `ValidationItem`

`com.socotra.coremodel.ValidationItem`

| accessor | returns |
|---|---|
| `elementType()` | `String` |
| `locator()` | `java.util.Optional<ULID>` |
| `errors()` | `java.util.Collection<String>` |

#### `ValidationPlugin.ZenCoverQuoteRequest`

`com.socotra.deployment.customer.ValidationPlugin$ZenCoverQuoteRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `quote()` | `ZenCoverQuote` |

#### `ValidationPlugin.ZenCoverQuickQuoteRequest`

`com.socotra.deployment.customer.ValidationPlugin$ZenCoverQuickQuoteRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `quote()` | `ZenCoverQuickQuote` |

#### `ValidationPlugin.ZenCoverRequest`

`com.socotra.deployment.customer.ValidationPlugin$ZenCoverRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `policy()` | `Policy` |
| `transaction()` | `Transaction` |
| `segment()` | `java.util.Optional<ZenCoverSegment>` |

#### `ValidationPlugin.StandardPaymentRequest`

`com.socotra.deployment.customer.ValidationPlugin$StandardPaymentRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `payment()` | `StandardPayment` |

## WorkplanExecutionPlugin

`com.socotra.deployment.customer.WorkplanExecutionPlugin`

### Methods (1)

```java
default WorkplanExecutionResponse decorateWorkplanExecution(WorkplanExecutionPlugin.WorkplanExecutionRequest);
```

### Request / response types

#### `WorkplanExecutionPlugin.WorkplanExecutionRequest`

`com.socotra.deployment.customer.WorkplanExecutionPlugin$WorkplanExecutionRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `execution()` | `WorkplanExecution` |

#### `WorkplanExecutionResponse`

`com.socotra.coremodel.WorkplanExecutionResponse`

| accessor | returns |
|---|---|
| `tasks()` | `java.util.Collection<TaskCreateRequest>` |
| `associations()` | `java.util.Collection<UserAssociationCreateRequest>` |

## WorkplanSelectionPlugin

`com.socotra.deployment.customer.WorkplanSelectionPlugin`

### Methods (1)

```java
default WorkplanSelectionResponse selectWorkplans(WorkplanSelectionPlugin.WorkplanSelectionRequest);
```

### Request / response types

#### `WorkplanSelectionPlugin.WorkplanSelectionRequest`

`com.socotra.deployment.customer.WorkplanSelectionPlugin$WorkplanSelectionRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `workplansSelection()` | `WorkplanSelection` |

#### `WorkplanSelectionResponse`

`com.socotra.coremodel.WorkplanSelectionResponse`

| accessor | returns |
|---|---|
| `workplansToExecute()` | `java.util.Collection<WorkplanSelectionItem>` |

