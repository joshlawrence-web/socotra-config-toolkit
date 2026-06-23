# Socotra plugin surface — BasicCreditCardProtection, PremiumCreditCardProtection

Source JARs: `customer-config.jar` + `core-datamodel-v1.6.180.jar`
core-datamodel version: **1.6.180**
Detected products (2): **BasicCreditCardProtection**, **PremiumCreditCardProtection**
Plugin interfaces: **11** · declared methods: **64**

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

#### `AutopayPluginResponse`

`com.socotra.coremodel.AutopayPluginResponse`

| accessor | returns |
|---|---|
| `paymentRequest()` | `java.util.Optional<PaymentCreateRequest>` |
| `nextRequestTime()` | `java.util.Optional<java.time.Instant>` |

## DelinquencyEventPlugin

`com.socotra.deployment.customer.DelinquencyEventPlugin`

### Methods (2)

```java
default java.util.Map<String, DelinquencyEventUpdateRequest> triggerDelinquencyEvent(DelinquencyEventPlugin.BasicCreditCardProtectionRequest);
default java.util.Map<String, DelinquencyEventUpdateRequest> triggerDelinquencyEvent(DelinquencyEventPlugin.PremiumCreditCardProtectionRequest);
```

### Request / response types

#### `DelinquencyEventPlugin.BasicCreditCardProtectionRequest`

`com.socotra.deployment.customer.DelinquencyEventPlugin$BasicCreditCardProtectionRequest`

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

#### `DelinquencyEventPlugin.PremiumCreditCardProtectionRequest`

`com.socotra.deployment.customer.DelinquencyEventPlugin$PremiumCreditCardProtectionRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `delinquency()` | `Delinquency` |
| `delinquencyEvent()` | `DelinquencyEvent` |

## DocumentConsolidationSnapshotPlugin

`com.socotra.deployment.customer.DocumentConsolidationSnapshotPlugin`

### Methods (5)

```java
default DocumentDataSnapshot consolidate(DocumentConsolidationSnapshotPlugin.BasicCreditCardProtectionQuoteRequest);
default DocumentDataSnapshot consolidate(DocumentConsolidationSnapshotPlugin.BasicCreditCardProtectionRequest);
default DocumentDataSnapshot consolidate(DocumentConsolidationSnapshotPlugin.PremiumCreditCardProtectionQuoteRequest);
default DocumentDataSnapshot consolidate(DocumentConsolidationSnapshotPlugin.PremiumCreditCardProtectionRequest);
default DocumentDataSnapshot consolidate(DocumentConsolidationSnapshotPlugin.InvoiceDetailsRequest);
```

### Request / response types

#### `DocumentConsolidationSnapshotPlugin.BasicCreditCardProtectionQuoteRequest`

`com.socotra.deployment.customer.DocumentConsolidationSnapshotPlugin$BasicCreditCardProtectionQuoteRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `quote()` | `BasicCreditCardProtectionQuote` |
| `config()` | `DocumentConfig` |
| `consolidationInfo()` | `DocumentConsolidationInfo` |

#### `DocumentDataSnapshot`

`com.socotra.coremodel.DocumentDataSnapshot`

| accessor | returns |
|---|---|
| `metadata()` | `java.util.Optional<Object>` |
| `renderingData()` | `Object` |

#### `DocumentConsolidationSnapshotPlugin.BasicCreditCardProtectionRequest`

`com.socotra.deployment.customer.DocumentConsolidationSnapshotPlugin$BasicCreditCardProtectionRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `policy()` | `Policy` |
| `transaction()` | `Transaction` |
| `segment()` | `java.util.Optional<BasicCreditCardProtectionSegment>` |
| `config()` | `DocumentConfig` |
| `consolidationInfo()` | `DocumentConsolidationInfo` |

#### `DocumentConsolidationSnapshotPlugin.PremiumCreditCardProtectionQuoteRequest`

`com.socotra.deployment.customer.DocumentConsolidationSnapshotPlugin$PremiumCreditCardProtectionQuoteRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `quote()` | `PremiumCreditCardProtectionQuote` |
| `config()` | `DocumentConfig` |
| `consolidationInfo()` | `DocumentConsolidationInfo` |

#### `DocumentConsolidationSnapshotPlugin.PremiumCreditCardProtectionRequest`

`com.socotra.deployment.customer.DocumentConsolidationSnapshotPlugin$PremiumCreditCardProtectionRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `policy()` | `Policy` |
| `transaction()` | `Transaction` |
| `segment()` | `java.util.Optional<PremiumCreditCardProtectionSegment>` |
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

### Methods (5)

```java
default DocumentDataSnapshot dataSnapshot(DocumentDataSnapshotPlugin.BasicCreditCardProtectionQuoteRequest);
default DocumentDataSnapshot dataSnapshot(DocumentDataSnapshotPlugin.BasicCreditCardProtectionRequest);
default DocumentDataSnapshot dataSnapshot(DocumentDataSnapshotPlugin.PremiumCreditCardProtectionQuoteRequest);
default DocumentDataSnapshot dataSnapshot(DocumentDataSnapshotPlugin.PremiumCreditCardProtectionRequest);
default DocumentDataSnapshot dataSnapshot(DocumentDataSnapshotPlugin.InvoiceDetailsRequest);
```

### Request / response types

#### `DocumentDataSnapshotPlugin.BasicCreditCardProtectionQuoteRequest`

`com.socotra.deployment.customer.DocumentDataSnapshotPlugin$BasicCreditCardProtectionQuoteRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `quote()` | `BasicCreditCardProtectionQuote` |
| `config()` | `DocumentConfig` |

#### `DocumentDataSnapshot`

`com.socotra.coremodel.DocumentDataSnapshot`

| accessor | returns |
|---|---|
| `metadata()` | `java.util.Optional<Object>` |
| `renderingData()` | `Object` |

#### `DocumentDataSnapshotPlugin.BasicCreditCardProtectionRequest`

`com.socotra.deployment.customer.DocumentDataSnapshotPlugin$BasicCreditCardProtectionRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `policy()` | `Policy` |
| `transaction()` | `Transaction` |
| `segment()` | `java.util.Optional<BasicCreditCardProtectionSegment>` |
| `config()` | `DocumentConfig` |

#### `DocumentDataSnapshotPlugin.PremiumCreditCardProtectionQuoteRequest`

`com.socotra.deployment.customer.DocumentDataSnapshotPlugin$PremiumCreditCardProtectionQuoteRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `quote()` | `PremiumCreditCardProtectionQuote` |
| `config()` | `DocumentConfig` |

#### `DocumentDataSnapshotPlugin.PremiumCreditCardProtectionRequest`

`com.socotra.deployment.customer.DocumentDataSnapshotPlugin$PremiumCreditCardProtectionRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `policy()` | `Policy` |
| `transaction()` | `Transaction` |
| `segment()` | `java.util.Optional<PremiumCreditCardProtectionSegment>` |
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

### Methods (4)

```java
default java.util.Map<String, DocumentSelectionAction> selectDocuments(DocumentSelectionPlugin.BasicCreditCardProtectionQuoteRequest);
default java.util.Map<String, DocumentSelectionAction> selectDocuments(DocumentSelectionPlugin.BasicCreditCardProtectionRequest);
default java.util.Map<String, DocumentSelectionAction> selectDocuments(DocumentSelectionPlugin.PremiumCreditCardProtectionQuoteRequest);
default java.util.Map<String, DocumentSelectionAction> selectDocuments(DocumentSelectionPlugin.PremiumCreditCardProtectionRequest);
```

### Request / response types

#### `DocumentSelectionPlugin.BasicCreditCardProtectionQuoteRequest`

`com.socotra.deployment.customer.DocumentSelectionPlugin$BasicCreditCardProtectionQuoteRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `quote()` | `BasicCreditCardProtectionQuote` |
| `documentConfigs()` | `java.util.Collection<DocumentConfig>` |
| `trigger()` | `DocumentTrigger` |

#### `DocumentSelectionAction`

`com.socotra.coremodel.DocumentSelectionAction`

| accessor | returns |
|---|---|
| `values()` | `DocumentSelectionAction[]` |

#### `DocumentSelectionPlugin.BasicCreditCardProtectionRequest`

`com.socotra.deployment.customer.DocumentSelectionPlugin$BasicCreditCardProtectionRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `policy()` | `Policy` |
| `transaction()` | `Transaction` |
| `segment()` | `java.util.Optional<BasicCreditCardProtectionSegment>` |
| `documentConfigs()` | `java.util.Collection<DocumentConfig>` |
| `trigger()` | `DocumentTrigger` |

#### `DocumentSelectionPlugin.PremiumCreditCardProtectionQuoteRequest`

`com.socotra.deployment.customer.DocumentSelectionPlugin$PremiumCreditCardProtectionQuoteRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `quote()` | `PremiumCreditCardProtectionQuote` |
| `documentConfigs()` | `java.util.Collection<DocumentConfig>` |
| `trigger()` | `DocumentTrigger` |

#### `DocumentSelectionPlugin.PremiumCreditCardProtectionRequest`

`com.socotra.deployment.customer.DocumentSelectionPlugin$PremiumCreditCardProtectionRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `policy()` | `Policy` |
| `transaction()` | `Transaction` |
| `segment()` | `java.util.Optional<PremiumCreditCardProtectionSegment>` |
| `documentConfigs()` | `java.util.Collection<DocumentConfig>` |
| `trigger()` | `DocumentTrigger` |

## InstallmentsPlugin

`com.socotra.deployment.customer.InstallmentsPlugin`

### Methods (2)

```java
default InstallmentsPluginResponse updateInstallments(InstallmentsPlugin.BasicCreditCardProtectionRequest);
default InstallmentsPluginResponse updateInstallments(InstallmentsPlugin.PremiumCreditCardProtectionRequest);
```

### Request / response types

#### `InstallmentsPlugin.BasicCreditCardProtectionRequest`

`com.socotra.deployment.customer.InstallmentsPlugin$BasicCreditCardProtectionRequest`

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

#### `InstallmentsPlugin.PremiumCreditCardProtectionRequest`

`com.socotra.deployment.customer.InstallmentsPlugin$PremiumCreditCardProtectionRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `context()` | `InstallmentsPluginContext` |
| `installments()` | `java.util.Collection<Installment>` |
| `installmentLattice()` | `InstallmentLattice` |

## PreCommitPlugin

`com.socotra.deployment.customer.PreCommitPlugin`

### Methods (11)

```java
default BankCustomerAccount preCommit(PreCommitPlugin.BankCustomerAccountRequest);
default BasicCreditCardProtectionQuote preCommit(PreCommitPlugin.BasicCreditCardProtectionQuoteRequest);
default BasicCreditCardProtectionQuickQuote preCommit(PreCommitPlugin.BasicCreditCardProtectionQuickQuoteRequest);
default PreCommitTransactionResponse preCommit(PreCommitPlugin.BasicCreditCardProtectionTransactionRequest);
default BasicCreditCardProtectionSegment preCommit(PreCommitPlugin.BasicCreditCardProtectionRequest);
default PremiumCreditCardProtectionQuote preCommit(PreCommitPlugin.PremiumCreditCardProtectionQuoteRequest);
default PremiumCreditCardProtectionQuickQuote preCommit(PreCommitPlugin.PremiumCreditCardProtectionQuickQuoteRequest);
default PreCommitTransactionResponse preCommit(PreCommitPlugin.PremiumCreditCardProtectionTransactionRequest);
default PremiumCreditCardProtectionSegment preCommit(PreCommitPlugin.PremiumCreditCardProtectionRequest);
default PreCommitDelinquencyResponse preCommit(PreCommitPlugin.DelinquencyRequest);
default PreCommitDelinquencyEventsResponse preCommit(PreCommitPlugin.DelinquencyEventsRequest);
```

### Request / response types

#### `PreCommitPlugin.BankCustomerAccountRequest`

`com.socotra.deployment.customer.PreCommitPlugin$BankCustomerAccountRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `account()` | `BankCustomerAccount` |
| `trigger()` | `PreCommitTrigger` |

#### `BankCustomerAccount`

`com.socotra.deployment.customer.BankCustomerAccount`

| accessor | returns |
|---|---|
| `numberingTrigger()` | `NumberingTrigger` |
| `type()` | `String` |
| `anonymizeData()` | `BankCustomerAccount` |
| `contactSlots()` | `java.util.Map<String, ContactSlot>` |
| `locator()` | `ULID` |
| `data()` | `BankCustomerAccount.BankCustomerAccountData` |
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

#### `PreCommitPlugin.BasicCreditCardProtectionQuoteRequest`

`com.socotra.deployment.customer.PreCommitPlugin$BasicCreditCardProtectionQuoteRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `quote()` | `BasicCreditCardProtectionQuote` |
| `trigger()` | `PreCommitTrigger` |

#### `BasicCreditCardProtectionQuote`

`com.socotra.deployment.customer.BasicCreditCardProtectionQuote`

| accessor | returns |
|---|---|
| `contactSlots()` | `java.util.Map<String, ContactSlot>` |
| `numberingTrigger()` | `NumberingTrigger` |
| `type()` | `String` |
| `anonymizeData()` | `BasicCreditCardProtectionQuote` |
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
| `billingTrigger()` | `java.util.Optional<BillingTrigger>` |
| `region()` | `java.util.Optional<String>` |
| `fraudProtection()` | `FraudProtectionQuote` |
| `cardReplacement()` | `CardReplacementQuote` |
| `unauthorizedUse()` | `UnauthorizedUseQuote` |
| `fraudLimit()` | `FraudLimit` |
| `cardReplacementBenefit()` | `CardReplacementBenefit` |
| `basicDeductible()` | `BasicDeductible` |
| `data()` | `BasicCreditCardProtectionQuote.BasicCreditCardProtectionQuoteData` |
| `element()` | `Element` |
| `billingLevel()` | `BillingLevel` |
| `quoteNumber()` | `java.util.Optional<String>` |
| `contacts()` | `java.util.Collection<ContactRoles>` |
| `invoiceFeeAmount()` | `java.util.Optional<java.math.BigDecimal>` |

#### `PreCommitPlugin.BasicCreditCardProtectionQuickQuoteRequest`

`com.socotra.deployment.customer.PreCommitPlugin$BasicCreditCardProtectionQuickQuoteRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `quote()` | `BasicCreditCardProtectionQuickQuote` |
| `trigger()` | `PreCommitTrigger` |

#### `BasicCreditCardProtectionQuickQuote`

`com.socotra.deployment.customer.BasicCreditCardProtectionQuickQuote`

| accessor | returns |
|---|---|
| `contactSlots()` | `java.util.Map<String, ContactSlot>` |
| `type()` | `String` |
| `anonymizeData()` | `BasicCreditCardProtectionQuickQuote` |
| `locator()` | `ULID` |
| `state()` | `QuickQuoteState` |
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
| `fraudProtection()` | `FraudProtectionQuickQuote` |
| `cardReplacement()` | `CardReplacementQuickQuote` |
| `unauthorizedUse()` | `UnauthorizedUseQuickQuote` |
| `fraudLimit()` | `FraudLimit` |
| `cardReplacementBenefit()` | `CardReplacementBenefit` |
| `basicDeductible()` | `BasicDeductible` |
| `data()` | `BasicCreditCardProtectionQuickQuote.BasicCreditCardProtectionQuickQuoteData` |
| `element()` | `Element` |
| `contacts()` | `java.util.Collection<ContactRoles>` |

#### `PreCommitPlugin.BasicCreditCardProtectionTransactionRequest`

`com.socotra.deployment.customer.PreCommitPlugin$BasicCreditCardProtectionTransactionRequest`

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

#### `PreCommitPlugin.BasicCreditCardProtectionRequest`

`com.socotra.deployment.customer.PreCommitPlugin$BasicCreditCardProtectionRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `policy()` | `Policy` |
| `transaction()` | `Transaction` |
| `segment()` | `BasicCreditCardProtectionSegment` |
| `trigger()` | `PreCommitTrigger` |

#### `BasicCreditCardProtectionSegment`

`com.socotra.deployment.customer.BasicCreditCardProtectionSegment`

| accessor | returns |
|---|---|
| `type()` | `String` |
| `anonymizeData()` | `BasicCreditCardProtectionSegment` |
| `originalEffectiveTime()` | `java.time.Instant` |
| `locator()` | `ULID` |
| `transactionLocator()` | `ULID` |
| `basedOn()` | `java.util.Optional<ULID>` |
| `segmentType()` | `SegmentType` |
| `startTime()` | `java.time.Instant` |
| `endTime()` | `java.time.Instant` |
| `duration()` | `java.math.BigDecimal` |
| `fraudProtection()` | `FraudProtectionPolicy` |
| `cardReplacement()` | `CardReplacementPolicy` |
| `unauthorizedUse()` | `UnauthorizedUsePolicy` |
| `fraudLimit()` | `FraudLimit` |
| `cardReplacementBenefit()` | `CardReplacementBenefit` |
| `basicDeductible()` | `BasicDeductible` |
| `data()` | `BasicCreditCardProtectionSegment.BasicCreditCardProtectionSegmentData` |
| `element()` | `Element` |

#### `PreCommitPlugin.PremiumCreditCardProtectionQuoteRequest`

`com.socotra.deployment.customer.PreCommitPlugin$PremiumCreditCardProtectionQuoteRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `quote()` | `PremiumCreditCardProtectionQuote` |
| `trigger()` | `PreCommitTrigger` |

#### `PremiumCreditCardProtectionQuote`

`com.socotra.deployment.customer.PremiumCreditCardProtectionQuote`

| accessor | returns |
|---|---|
| `contactSlots()` | `java.util.Map<String, ContactSlot>` |
| `numberingTrigger()` | `NumberingTrigger` |
| `type()` | `String` |
| `anonymizeData()` | `PremiumCreditCardProtectionQuote` |
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
| `billingTrigger()` | `java.util.Optional<BillingTrigger>` |
| `region()` | `java.util.Optional<String>` |
| `fraudProtection()` | `FraudProtectionQuote` |
| `identityTheftProtection()` | `IdentityTheftProtectionQuote` |
| `cardReplacement()` | `CardReplacementQuote` |
| `unauthorizedUse()` | `UnauthorizedUseQuote` |
| `purchaseProtection()` | `PurchaseProtectionQuote` |
| `travelEmergencyAssistance()` | `TravelEmergencyAssistanceQuote` |
| `fraudLimit()` | `FraudLimit` |
| `identityTheftLimit()` | `IdentityTheftLimit` |
| `purchaseProtectionLimit()` | `PurchaseProtectionLimit` |
| `travelAssistanceLimit()` | `TravelAssistanceLimit` |
| `cardReplacementBenefit()` | `CardReplacementBenefit` |
| `premiumDeductible()` | `PremiumDeductible` |
| `data()` | `PremiumCreditCardProtectionQuote.PremiumCreditCardProtectionQuoteData` |
| `element()` | `Element` |
| `billingLevel()` | `BillingLevel` |
| `quoteNumber()` | `java.util.Optional<String>` |
| `contacts()` | `java.util.Collection<ContactRoles>` |
| `invoiceFeeAmount()` | `java.util.Optional<java.math.BigDecimal>` |

#### `PreCommitPlugin.PremiumCreditCardProtectionQuickQuoteRequest`

`com.socotra.deployment.customer.PreCommitPlugin$PremiumCreditCardProtectionQuickQuoteRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `quote()` | `PremiumCreditCardProtectionQuickQuote` |
| `trigger()` | `PreCommitTrigger` |

#### `PremiumCreditCardProtectionQuickQuote`

`com.socotra.deployment.customer.PremiumCreditCardProtectionQuickQuote`

| accessor | returns |
|---|---|
| `contactSlots()` | `java.util.Map<String, ContactSlot>` |
| `type()` | `String` |
| `anonymizeData()` | `PremiumCreditCardProtectionQuickQuote` |
| `locator()` | `ULID` |
| `state()` | `QuickQuoteState` |
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
| `fraudProtection()` | `FraudProtectionQuickQuote` |
| `identityTheftProtection()` | `IdentityTheftProtectionQuickQuote` |
| `cardReplacement()` | `CardReplacementQuickQuote` |
| `unauthorizedUse()` | `UnauthorizedUseQuickQuote` |
| `purchaseProtection()` | `PurchaseProtectionQuickQuote` |
| `travelEmergencyAssistance()` | `TravelEmergencyAssistanceQuickQuote` |
| `fraudLimit()` | `FraudLimit` |
| `identityTheftLimit()` | `IdentityTheftLimit` |
| `purchaseProtectionLimit()` | `PurchaseProtectionLimit` |
| `travelAssistanceLimit()` | `TravelAssistanceLimit` |
| `cardReplacementBenefit()` | `CardReplacementBenefit` |
| `premiumDeductible()` | `PremiumDeductible` |
| `data()` | `PremiumCreditCardProtectionQuickQuote.PremiumCreditCardProtectionQuickQuoteData` |
| `element()` | `Element` |
| `contacts()` | `java.util.Collection<ContactRoles>` |

#### `PreCommitPlugin.PremiumCreditCardProtectionTransactionRequest`

`com.socotra.deployment.customer.PreCommitPlugin$PremiumCreditCardProtectionTransactionRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `policy()` | `Policy` |
| `transaction()` | `Transaction` |
| `changeInstructions()` | `java.util.Collection<ChangeInstructionHolder>` |
| `trigger()` | `PreCommitTrigger` |

#### `PreCommitPlugin.PremiumCreditCardProtectionRequest`

`com.socotra.deployment.customer.PreCommitPlugin$PremiumCreditCardProtectionRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `policy()` | `Policy` |
| `transaction()` | `Transaction` |
| `segment()` | `PremiumCreditCardProtectionSegment` |
| `trigger()` | `PreCommitTrigger` |

#### `PremiumCreditCardProtectionSegment`

`com.socotra.deployment.customer.PremiumCreditCardProtectionSegment`

| accessor | returns |
|---|---|
| `type()` | `String` |
| `anonymizeData()` | `PremiumCreditCardProtectionSegment` |
| `originalEffectiveTime()` | `java.time.Instant` |
| `locator()` | `ULID` |
| `transactionLocator()` | `ULID` |
| `basedOn()` | `java.util.Optional<ULID>` |
| `segmentType()` | `SegmentType` |
| `startTime()` | `java.time.Instant` |
| `endTime()` | `java.time.Instant` |
| `duration()` | `java.math.BigDecimal` |
| `fraudProtection()` | `FraudProtectionPolicy` |
| `identityTheftProtection()` | `IdentityTheftProtectionPolicy` |
| `cardReplacement()` | `CardReplacementPolicy` |
| `unauthorizedUse()` | `UnauthorizedUsePolicy` |
| `purchaseProtection()` | `PurchaseProtectionPolicy` |
| `travelEmergencyAssistance()` | `TravelEmergencyAssistancePolicy` |
| `fraudLimit()` | `FraudLimit` |
| `identityTheftLimit()` | `IdentityTheftLimit` |
| `purchaseProtectionLimit()` | `PurchaseProtectionLimit` |
| `travelAssistanceLimit()` | `TravelAssistanceLimit` |
| `cardReplacementBenefit()` | `CardReplacementBenefit` |
| `premiumDeductible()` | `PremiumDeductible` |
| `data()` | `PremiumCreditCardProtectionSegment.PremiumCreditCardProtectionSegmentData` |
| `element()` | `Element` |

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

### Methods (12)

```java
default RatingSet rate(RatePlugin.BasicCreditCardProtectionQuoteRequest);
default RatingSet statelessRate(RatePlugin.BasicCreditCardProtectionQuoteRequest);
default RatingSet rate(RatePlugin.BasicCreditCardProtectionQuickQuoteRequest);
default RatingSet statelessRate(RatePlugin.BasicCreditCardProtectionQuickQuoteRequest);
default RatingSet rate(RatePlugin.BasicCreditCardProtectionRequest);
default RatingSet statelessRate(RatePlugin.BasicCreditCardProtectionRequest);
default RatingSet rate(RatePlugin.PremiumCreditCardProtectionQuoteRequest);
default RatingSet statelessRate(RatePlugin.PremiumCreditCardProtectionQuoteRequest);
default RatingSet rate(RatePlugin.PremiumCreditCardProtectionQuickQuoteRequest);
default RatingSet statelessRate(RatePlugin.PremiumCreditCardProtectionQuickQuoteRequest);
default RatingSet rate(RatePlugin.PremiumCreditCardProtectionRequest);
default RatingSet statelessRate(RatePlugin.PremiumCreditCardProtectionRequest);
```

### Request / response types

#### `RatePlugin.BasicCreditCardProtectionQuoteRequest`

`com.socotra.deployment.customer.RatePlugin$BasicCreditCardProtectionQuoteRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `quote()` | `BasicCreditCardProtectionQuote` |
| `duration()` | `java.math.BigDecimal` |
| `durationBasis()` | `DurationBasis` |

#### `RatingSet`

`com.socotra.coremodel.RatingSet`

| accessor | returns |
|---|---|
| `ok()` | `Boolean` |
| `ratingItems()` | `java.util.Collection<RatingItem>` |

#### `RatePlugin.BasicCreditCardProtectionQuickQuoteRequest`

`com.socotra.deployment.customer.RatePlugin$BasicCreditCardProtectionQuickQuoteRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `quote()` | `BasicCreditCardProtectionQuickQuote` |
| `duration()` | `java.math.BigDecimal` |
| `durationBasis()` | `DurationBasis` |

#### `RatePlugin.BasicCreditCardProtectionRequest`

`com.socotra.deployment.customer.RatePlugin$BasicCreditCardProtectionRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `policy()` | `Policy` |
| `transaction()` | `Transaction` |
| `segment()` | `java.util.Optional<BasicCreditCardProtectionSegment>` |
| `duration()` | `java.math.BigDecimal` |
| `durationBasis()` | `DurationBasis` |

#### `RatePlugin.PremiumCreditCardProtectionQuoteRequest`

`com.socotra.deployment.customer.RatePlugin$PremiumCreditCardProtectionQuoteRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `quote()` | `PremiumCreditCardProtectionQuote` |
| `duration()` | `java.math.BigDecimal` |
| `durationBasis()` | `DurationBasis` |

#### `RatePlugin.PremiumCreditCardProtectionQuickQuoteRequest`

`com.socotra.deployment.customer.RatePlugin$PremiumCreditCardProtectionQuickQuoteRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `quote()` | `PremiumCreditCardProtectionQuickQuote` |
| `duration()` | `java.math.BigDecimal` |
| `durationBasis()` | `DurationBasis` |

#### `RatePlugin.PremiumCreditCardProtectionRequest`

`com.socotra.deployment.customer.RatePlugin$PremiumCreditCardProtectionRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `policy()` | `Policy` |
| `transaction()` | `Transaction` |
| `segment()` | `java.util.Optional<PremiumCreditCardProtectionSegment>` |
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

### Methods (8)

```java
default UnderwritingModification underwrite(UnderwritingPlugin.BasicCreditCardProtectionQuoteRequest);
default UnderwritingModification underwrite(UnderwritingPlugin.BasicCreditCardProtectionRequest);
default UnderwritingModification statelessUnderwrite(UnderwritingPlugin.BasicCreditCardProtectionQuoteRequest);
default UnderwritingModification statelessUnderwrite(UnderwritingPlugin.BasicCreditCardProtectionRequest);
default UnderwritingModification underwrite(UnderwritingPlugin.PremiumCreditCardProtectionQuoteRequest);
default UnderwritingModification underwrite(UnderwritingPlugin.PremiumCreditCardProtectionRequest);
default UnderwritingModification statelessUnderwrite(UnderwritingPlugin.PremiumCreditCardProtectionQuoteRequest);
default UnderwritingModification statelessUnderwrite(UnderwritingPlugin.PremiumCreditCardProtectionRequest);
```

### Request / response types

#### `UnderwritingPlugin.BasicCreditCardProtectionQuoteRequest`

`com.socotra.deployment.customer.UnderwritingPlugin$BasicCreditCardProtectionQuoteRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `quote()` | `BasicCreditCardProtectionQuote` |
| `flags()` | `java.util.Collection<UnderwritingFlag>` |

#### `UnderwritingModification`

`com.socotra.coremodel.UnderwritingModification`

| accessor | returns |
|---|---|
| `flagsToCreate()` | `java.util.Collection<UnderwritingFlagCore>` |
| `flagsToClear()` | `java.util.Collection<ULID>` |

#### `UnderwritingPlugin.BasicCreditCardProtectionRequest`

`com.socotra.deployment.customer.UnderwritingPlugin$BasicCreditCardProtectionRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `policy()` | `Policy` |
| `transaction()` | `Transaction` |
| `segment()` | `java.util.Optional<BasicCreditCardProtectionSegment>` |
| `flags()` | `java.util.Collection<UnderwritingFlag>` |

#### `UnderwritingPlugin.PremiumCreditCardProtectionQuoteRequest`

`com.socotra.deployment.customer.UnderwritingPlugin$PremiumCreditCardProtectionQuoteRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `quote()` | `PremiumCreditCardProtectionQuote` |
| `flags()` | `java.util.Collection<UnderwritingFlag>` |

#### `UnderwritingPlugin.PremiumCreditCardProtectionRequest`

`com.socotra.deployment.customer.UnderwritingPlugin$PremiumCreditCardProtectionRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `policy()` | `Policy` |
| `transaction()` | `Transaction` |
| `segment()` | `java.util.Optional<PremiumCreditCardProtectionSegment>` |
| `flags()` | `java.util.Collection<UnderwritingFlag>` |

## ValidationPlugin

`com.socotra.deployment.customer.ValidationPlugin`

### Methods (13)

```java
default ValidationItem validate(ValidationPlugin.BankCustomerAccountRequest);
default ValidationItem validate(ValidationPlugin.BasicCreditCardProtectionQuoteRequest);
default ValidationItem statelessValidate(ValidationPlugin.BasicCreditCardProtectionQuoteRequest);
default ValidationItem validate(ValidationPlugin.BasicCreditCardProtectionQuickQuoteRequest);
default ValidationItem statelessValidate(ValidationPlugin.BasicCreditCardProtectionQuickQuoteRequest);
default ValidationItem validate(ValidationPlugin.BasicCreditCardProtectionRequest);
default ValidationItem statelessValidate(ValidationPlugin.BasicCreditCardProtectionRequest);
default ValidationItem validate(ValidationPlugin.PremiumCreditCardProtectionQuoteRequest);
default ValidationItem statelessValidate(ValidationPlugin.PremiumCreditCardProtectionQuoteRequest);
default ValidationItem validate(ValidationPlugin.PremiumCreditCardProtectionQuickQuoteRequest);
default ValidationItem statelessValidate(ValidationPlugin.PremiumCreditCardProtectionQuickQuoteRequest);
default ValidationItem validate(ValidationPlugin.PremiumCreditCardProtectionRequest);
default ValidationItem statelessValidate(ValidationPlugin.PremiumCreditCardProtectionRequest);
```

### Request / response types

#### `ValidationPlugin.BankCustomerAccountRequest`

`com.socotra.deployment.customer.ValidationPlugin$BankCustomerAccountRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `account()` | `BankCustomerAccount` |

#### `ValidationItem`

`com.socotra.coremodel.ValidationItem`

| accessor | returns |
|---|---|
| `elementType()` | `String` |
| `locator()` | `java.util.Optional<ULID>` |
| `errors()` | `java.util.Collection<String>` |

#### `ValidationPlugin.BasicCreditCardProtectionQuoteRequest`

`com.socotra.deployment.customer.ValidationPlugin$BasicCreditCardProtectionQuoteRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `quote()` | `BasicCreditCardProtectionQuote` |

#### `ValidationPlugin.BasicCreditCardProtectionQuickQuoteRequest`

`com.socotra.deployment.customer.ValidationPlugin$BasicCreditCardProtectionQuickQuoteRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `quote()` | `BasicCreditCardProtectionQuickQuote` |

#### `ValidationPlugin.BasicCreditCardProtectionRequest`

`com.socotra.deployment.customer.ValidationPlugin$BasicCreditCardProtectionRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `policy()` | `Policy` |
| `transaction()` | `Transaction` |
| `segment()` | `java.util.Optional<BasicCreditCardProtectionSegment>` |

#### `ValidationPlugin.PremiumCreditCardProtectionQuoteRequest`

`com.socotra.deployment.customer.ValidationPlugin$PremiumCreditCardProtectionQuoteRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `quote()` | `PremiumCreditCardProtectionQuote` |

#### `ValidationPlugin.PremiumCreditCardProtectionQuickQuoteRequest`

`com.socotra.deployment.customer.ValidationPlugin$PremiumCreditCardProtectionQuickQuoteRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `quote()` | `PremiumCreditCardProtectionQuickQuote` |

#### `ValidationPlugin.PremiumCreditCardProtectionRequest`

`com.socotra.deployment.customer.ValidationPlugin$PremiumCreditCardProtectionRequest`

| accessor | returns |
|---|---|
| `structures()` | `java.util.Collection<?>` |
| `policy()` | `Policy` |
| `transaction()` | `Transaction` |
| `segment()` | `java.util.Optional<PremiumCreditCardProtectionSegment>` |

