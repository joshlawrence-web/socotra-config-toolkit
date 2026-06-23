package com.socotra.deployment.customer;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.function.Function;
import com.socotra.coremodel.*;
import com.socotra.coremodel.interfaces.EventType;
import com.socotra.deployment.*;
import com.socotra.deployment.workmanagement.*;
import com.socotra.deployment.producermanagement.*;

public final class CustomerConfig extends DeploymentConfig {

  public CustomerConfig(DeploymentFactory factory) {
    this.factory = factory;

    initProducts();
    initPlans();
    initAccounts();
    initTables();
    initRangeTables();
    initConstraintTables();
    initSecrets();
    initPaymentsAndDisbursements();
    initContacts();
    initElements();
    initResourceDeclarations();
    initConstraints();
    initRegionsAndJurisdictions();
    initAuxDataSettings();
    initWorkManagement();
    initProducerManagement();
    initSearchConfiguration();
    initAutomations();

    this.customEvents = com.socotra.deployment.customer.CustomEvent.map();
    this.customEventsByType = this.customEvents.values().stream().collect(Collectors.toMap(com.socotra.coremodel.CustomEvent::eventType, Function.identity()));
    this.tenantCustomEvents = com.socotra.deployment.customer.TenantCustomEvent.map();
    this.tenantCustomEventsByType = this.tenantCustomEvents.values().stream().collect(Collectors.toMap(com.socotra.coremodel.TenantCustomEvent::eventType, Function.identity()));

    this.defaultRegion = "";
    this.defaultCurrency = "EUR";
    this.defaultTimezone = "Europe/Paris";
    this.defaultInstallmentPlanName = "Monthly12";
    this.defaultInvoicingPlanName = "";
    this.defaultRetryPlanName = "";
    this.defaultPaymentNumberingPlanName = "";
    this.defaultDisbursementNumberingPlanName = "";
    this.defaultOperationsWorkbenchUIConfig = "";
    this.defaultBillingPlanName = "";
    this.defaultDelinquencyPlanName = "Standard";
    this.defaultAutoRenewalPlanName = "Standard_60_30_1";
    this.defaultExcessCreditPlanName = "Standard";
    this.defaultShortfallTolerancePlanName = "basicPlan";
    this.defaultBackdatedInstallmentsBilling = BackdatedInstallmentsBilling.deferDueDate;
    this.defaultInvoiceDocument = "";
    this.isSerialInvoiceNumberingEnabled = false;
    this.isEntityAnonymizationEnabled = false;
    this.isCustomerDataEncryptionEnabled = false;
    this.knownPlugins = Set.of(PluginType.automationHttp,PluginType.automationWebhook,PluginType.autopay,PluginType.cancellation,PluginType.configMigration,PluginType.delinquencyEvent,PluginType.deserialization,PluginType.documentConsolidationSelection,PluginType.documentConsolidationSnapshot,PluginType.documentDataSnapshot,PluginType.documentSelection,PluginType.installments,PluginType.paymentPostProcessing,PluginType.preCommit,PluginType.rating,PluginType.renewal,PluginType.underwriting,PluginType.validation,PluginType.workplanExecution,PluginType.workplanSelection);



    this.webhookEnabledAutomationPluginPath = List.of(
    );

    buildObjectRepository();
  }

  private void initAuxDataSettings() {
    this.auxDataSettings = Map.ofEntries(
        Map.entry("SampleAuxDataSetting", 30),
        Map.entry("MaxClaims", 30)
    );
    this.defaultAuxDataSettings = "SampleAuxDataSetting";
  }

  private void initProducts() {
    this.products = Map.ofEntries(
        Map.entry(trimSuffix("ZenCoverProduct", "Product").toLowerCase(), new ZenCoverProduct())
    );
  }

  private void initPlans() {
    this.installmentPlans = InstallmentPlans.PLANS.entrySet().stream().collect(
        Collectors.toMap(e -> trimSuffix(e.getKey(), "InstallmentPlan").toLowerCase(), Map.Entry::getValue));
    this.invoicingPlans = InvoicingPlans.PLANS.entrySet().stream().collect(
        Collectors.toMap(e -> trimSuffix(e.getKey(), "InvoicingPlan").toLowerCase(), Map.Entry::getValue));
    this.billingPlans = Map.ofEntries(
    );
    this.delinquencyPlans = DelinquencyPlans.PLANS.entrySet().stream().collect(
        Collectors.toMap(e -> trimSuffix(e.getKey(), "DelinquencyPlan").toLowerCase(), Map.Entry::getValue));
    this.autoRenewalPlans = AutoRenewalPlans.PLANS.entrySet().stream().collect(
        Collectors.toMap(e -> trimSuffix(e.getKey(), "AutoRenewalPlan").toLowerCase(), Map.Entry::getValue));
    this.excessCreditPlans = ExcessCreditPlans.PLANS.entrySet().stream().collect(
        Collectors.toMap(e -> trimSuffix(e.getKey(), "ExcessCreditPlan").toLowerCase(), Map.Entry::getValue));
    this.shortfallTolerancePlans = ShortfallTolerancePlans.PLANS.entrySet().stream().collect(
        Collectors.toMap(e -> trimSuffix(e.getKey(), "ShortfallTolerancePlan").toLowerCase(), Map.Entry::getValue));
    this.retryPlans = RetryPlans.PLANS.entrySet().stream().collect(
        Collectors.toMap(e -> trimSuffix(e.getKey(), "RetryPlan").toLowerCase(), Map.Entry::getValue));
    this.numberingPlans = NumberingPlans.PLANS.entrySet().stream().collect(
        Collectors.toMap(e -> trimSuffix(e.getKey(), "NumberingPlan").toLowerCase(), Map.Entry::getValue));
    this.externalNumberingPlans = ExternalNumberingPlans.PLANS.entrySet().stream().collect(
        Collectors.toMap(e -> trimSuffix(e.getKey(), "ExternalNumberingPlan").toLowerCase(), Map.Entry::getValue));

    this.typeToNumberingPlan = new HashMap();
    this.typeToNumberingPlan.put("ZenCoverQuote", this.numberingPlans.get("zcpolicynumberingplan"));
    this.typeToNumberingPlan.put("ZenCoverPolicy", this.numberingPlans.get("zcpolicynumberingplan"));
    this.typeToNumberingPlan = Collections.unmodifiableMap(this.typeToNumberingPlan);
    this.typeToExternalNumberingPlan = new HashMap();
    this.typeToExternalNumberingPlan = Collections.unmodifiableMap(this.typeToExternalNumberingPlan);
  }

  private void initAccounts() {
    this.accounts = Map.ofEntries(
        Map.entry(PersonalAccount.TYPE.toLowerCase(), PersonalAccount.class)
    );
  }

  private void initContacts() {
    this.contacts = Map.ofEntries(
    );
    this.contactRoles = Set.of(
    );
  }

  private void initPaymentsAndDisbursements() {
    this.payments = Map.ofEntries(
        Map.entry(StandardPayment.TYPE.toLowerCase(), StandardPayment.class)
    );
    this.disbursements = Map.ofEntries(
    );
  }

  private void initTables() {
    this.tables = new HashMap<>();
    this.tables.put(CountyCodes.getStaticName().toLowerCase(), CountyCodes.class);
    this.tables.put(CountyTaxRates.getStaticName().toLowerCase(), CountyTaxRates.class);
    this.tables.put(CountyRiskFactors.getStaticName().toLowerCase(), CountyRiskFactors.class);
    this.tables.put(StateTaxRates.getStaticName().toLowerCase(), StateTaxRates.class);
    this.tables.put(SerialNumberList.getStaticName().toLowerCase(), SerialNumberList.class);
    this.tables = Collections.unmodifiableMap(this.tables);
}

  private void initRangeTables() {
  }

  private void initConstraintTables() {
    this.constraintTables = new HashMap<>();
    this.constraintTables.put(Counties.getStaticName().toLowerCase(), Counties.class);
    this.constraintTables = Collections.unmodifiableMap(this.constraintTables);
  }

  private void initSecrets() {
  }

  private void initElements() {
    this.elements = new HashMap<>();
    this.elements.put(ZenCoverSegment.ZenCoverSegmentData.TYPE.toLowerCase(), ZenCoverSegment.ZenCoverSegmentData.class);
    this.elements.put(BreakdownQuote.TYPE.toLowerCase(), BreakdownQuote.class);
    this.elements.put(AccidentalDamageQuote.AccidentalDamageQuoteData.TYPE.toLowerCase(), AccidentalDamageQuote.AccidentalDamageQuoteData.class);
    this.elements.put(ItemQuote.TYPE.toLowerCase(), ItemQuote.class);
    this.elements.put(ApplianceLimit.TYPE.toLowerCase(), ApplianceLimit.class);
    this.elements.put(AccidentalDamageQuote.TYPE.toLowerCase(), AccidentalDamageQuote.class);
    this.elements.put(ZenCoverSegment.TYPE.toLowerCase(), ZenCoverSegment.class);
    this.elements.put(TheftQuote.TYPE.toLowerCase(), TheftQuote.class);
    this.elements.put(AccidentalDamageQuickQuote.AccidentalDamageQuickQuoteData.TYPE.toLowerCase(), AccidentalDamageQuickQuote.AccidentalDamageQuickQuoteData.class);
    this.elements.put(ZenCoverQuote.TYPE.toLowerCase(), ZenCoverQuote.class);
    this.elements.put(TheftPolicy.TYPE.toLowerCase(), TheftPolicy.class);
    this.elements.put(ItemPolicy.ItemPolicyData.TYPE.toLowerCase(), ItemPolicy.ItemPolicyData.class);
    this.elements.put(ItemQuote.ItemQuoteData.TYPE.toLowerCase(), ItemQuote.ItemQuoteData.class);
    this.elements.put(AccidentalDamagePolicy.AccidentalDamagePolicyData.TYPE.toLowerCase(), AccidentalDamagePolicy.AccidentalDamagePolicyData.class);
    this.elements.put(TheftQuickQuote.TYPE.toLowerCase(), TheftQuickQuote.class);
    this.elements.put(ZenCoverPolicyStaticData.TYPE.toLowerCase(), ZenCoverPolicyStaticData.class);
    this.elements.put(BreakdownQuote.BreakdownQuoteData.TYPE.toLowerCase(), BreakdownQuote.BreakdownQuoteData.class);
    this.elements.put(ZenCoverQuote.ZenCoverQuoteData.TYPE.toLowerCase(), ZenCoverQuote.ZenCoverQuoteData.class);
    this.elements.put(ItemQuickQuote.TYPE.toLowerCase(), ItemQuickQuote.class);
    this.elements.put(ZeroDeductible.TYPE.toLowerCase(), ZeroDeductible.class);
    this.elements.put(ItemPolicy.TYPE.toLowerCase(), ItemPolicy.class);
    this.elements.put(BreakdownQuickQuote.BreakdownQuickQuoteData.TYPE.toLowerCase(), BreakdownQuickQuote.BreakdownQuickQuoteData.class);
    this.elements.put(ApplianceDeductible.TYPE.toLowerCase(), ApplianceDeductible.class);
    this.elements.put(BreakdownPolicy.TYPE.toLowerCase(), BreakdownPolicy.class);
    this.elements.put(BreakdownQuickQuote.TYPE.toLowerCase(), BreakdownQuickQuote.class);
    this.elements.put(ZenCoverQuoteStaticData.TYPE.toLowerCase(), ZenCoverQuoteStaticData.class);
    this.elements.put(ItemQuickQuote.ItemQuickQuoteData.TYPE.toLowerCase(), ItemQuickQuote.ItemQuickQuoteData.class);
    this.elements.put(AccessoryDeductible.TYPE.toLowerCase(), AccessoryDeductible.class);
    this.elements.put(AccidentalDamagePolicy.TYPE.toLowerCase(), AccidentalDamagePolicy.class);
    this.elements.put(ZenCoverQuickQuote.TYPE.toLowerCase(), ZenCoverQuickQuote.class);
    this.elements.put(BreakdownPolicy.BreakdownPolicyData.TYPE.toLowerCase(), BreakdownPolicy.BreakdownPolicyData.class);
    this.elements.put(ZenCoverQuickQuoteStaticData.TYPE.toLowerCase(), ZenCoverQuickQuoteStaticData.class);
    this.elements.put(ZenCoverQuickQuote.ZenCoverQuickQuoteData.TYPE.toLowerCase(), ZenCoverQuickQuote.ZenCoverQuickQuoteData.class);
    this.elements.put(AccidentalDamageQuickQuote.TYPE.toLowerCase(), AccidentalDamageQuickQuote.class);
    this.elements = Collections.unmodifiableMap(this.elements);
  }

  private void initResourceDeclarations() {
    this.resourceDeclarations.putAll(DocumentConfigs.toResourceDeclaration());
    this.resourceDeclarations.put(CountyCodes.getStaticName().toLowerCase(), ResourceDeclaration.of(CountyCodes.getStaticName(), ResourceType.table, CountyCodes.getSelectionTimeBasis()));
    this.resourceDeclarations.put(CountyTaxRates.getStaticName().toLowerCase(), ResourceDeclaration.of(CountyTaxRates.getStaticName(), ResourceType.table, CountyTaxRates.getSelectionTimeBasis()));
    this.resourceDeclarations.put(CountyRiskFactors.getStaticName().toLowerCase(), ResourceDeclaration.of(CountyRiskFactors.getStaticName(), ResourceType.table, CountyRiskFactors.getSelectionTimeBasis()));
    this.resourceDeclarations.put(StateTaxRates.getStaticName().toLowerCase(), ResourceDeclaration.of(StateTaxRates.getStaticName(), ResourceType.table, StateTaxRates.getSelectionTimeBasis()));
    this.resourceDeclarations.put(SerialNumberList.getStaticName().toLowerCase(), ResourceDeclaration.of(SerialNumberList.getStaticName(), ResourceType.table, SerialNumberList.getSelectionTimeBasis()));
    this.resourceDeclarations.put(Counties.getStaticName().toLowerCase(), ResourceDeclaration.of(Counties.getStaticName(), ResourceType.constraintTable, Counties.getSelectionTimeBasis()));
  }

  private void initConstraints() {
  }

  private void initRegionsAndJurisdictions() {
    this.regions = Set.of();
    this.regionNumberingStrings = Map.of();
    this.jurisdictions = Set.of();
  }

  private void initWorkManagement() {
    this.workManagement = WorkManagementConfig.builder()
      .qualifications(
        Map.ofEntries(
        )
      )
      .userAssociationRoles(
        Map.ofEntries(
        )
      )
      .tasks(
        Map.ofEntries(
            Map.entry(
              "riskReferral",
              TasksConfig
                .builder()
                .category("underwriting")
                .defaultDeadlineDays(Optional.of(BigDecimal.valueOf(3)))
                .blocksUnderwriting(true)
                .numberingPlan( Optional.empty())
                .numberingString( Optional.empty())
                .build()
            )
        )
      )
      .build();
  }

  private void initProducerManagement() {
  }

  private void initSearchConfiguration() {
    Collection searchObjs = new ArrayList();
    Collection<FieldSearchConfiguration> searchFieldsPersonalAccount = List.of(
        FieldSearchConfiguration.builder()
          .name("firstName")
          .type("string")
          .restricted(Optional.of(false))
          .build(),
        FieldSearchConfiguration.builder()
          .name("lastName")
          .type("string")
          .restricted(Optional.of(false))
          .build(),
        FieldSearchConfiguration.builder()
          .name("email")
          .type("string")
          .restricted(Optional.of(false))
          .build(),
        FieldSearchConfiguration.builder()
          .name("primaryPhone")
          .type("string")
          .restricted(Optional.of(false))
          .build()
    );

    Collection<String> contentsPersonalAccount = List.of(
    );
    searchObjs.add(EntitySearchConfiguration.builder()
      .fields(searchFieldsPersonalAccount)
      .entityName("PersonalAccount")
      .entityType("AccountRef")
      .contents(contentsPersonalAccount)
      .build());

    Collection<FieldSearchConfiguration> searchFieldsCoverExclusions = List.of(
        FieldSearchConfiguration.builder()
          .name("exclusionCode")
          .type("string")
          .restricted(Optional.of(false))
          .build()
    );

    Collection<String> contentsCoverExclusions = List.of(
    );
    searchObjs.add(EntitySearchConfiguration.builder()
      .fields(searchFieldsCoverExclusions)
      .entityName("CoverExclusions")
      .entityType("DataTypeRef")
      .contents(contentsCoverExclusions)
      .build());

    Collection<FieldSearchConfiguration> searchFieldsCoverData = List.of(
        FieldSearchConfiguration.builder()
          .name("perilStartDate")
          .type("date")
          .restricted(Optional.of(false))
          .build(),
        FieldSearchConfiguration.builder()
          .name("perilEndDate")
          .type("date")
          .restricted(Optional.of(false))
          .build(),
        FieldSearchConfiguration.builder()
          .name("premium")
          .type("decimal")
          .restricted(Optional.of(false))
          .build(),
        FieldSearchConfiguration.builder()
          .name("taxRate")
          .type("decimal")
          .restricted(Optional.of(false))
          .build(),
        FieldSearchConfiguration.builder()
          .name("taxAmount")
          .type("decimal")
          .restricted(Optional.of(false))
          .build(),
        FieldSearchConfiguration.builder()
          .name("labourCovered")
          .type("string")
          .restricted(Optional.of(false))
          .build(),
        FieldSearchConfiguration.builder()
          .name("partsCovered")
          .type("string")
          .restricted(Optional.of(false))
          .build(),
        FieldSearchConfiguration.builder()
          .name("manufacturerLabourGuaranteeEndDate")
          .type("date")
          .restricted(Optional.of(false))
          .build(),
        FieldSearchConfiguration.builder()
          .name("manufacturerPartsGuaranteeEndDate")
          .type("date")
          .restricted(Optional.of(false))
          .build()
    );

    Collection<String> contentsCoverData = List.of(
    );
    searchObjs.add(EntitySearchConfiguration.builder()
      .fields(searchFieldsCoverData)
      .entityName("CoverData")
      .entityType("DataTypeRef")
      .contents(contentsCoverData)
      .build());

    Collection<FieldSearchConfiguration> searchFieldsZenCover = List.of(
        FieldSearchConfiguration.builder()
          .name("coolingOffPeriod")
          .type("int")
          .restricted(Optional.of(false))
          .build(),
        FieldSearchConfiguration.builder()
          .name("newBusinessWaitPeriod")
          .type("int")
          .restricted(Optional.of(false))
          .build(),
        FieldSearchConfiguration.builder()
          .name("contractTermEndDate")
          .type("datetime")
          .restricted(Optional.of(false))
          .build(),
        FieldSearchConfiguration.builder()
          .name("expectedRenewalDate")
          .type("date")
          .restricted(Optional.of(false))
          .build(),
        FieldSearchConfiguration.builder()
          .name("discountAmount")
          .type("decimal")
          .restricted(Optional.of(false))
          .build(),
        FieldSearchConfiguration.builder()
          .name("discountType")
          .type("string")
          .restricted(Optional.of(false))
          .build(),
        FieldSearchConfiguration.builder()
          .name("discountProfileCode")
          .type("string")
          .restricted(Optional.of(false))
          .build(),
        FieldSearchConfiguration.builder()
          .name("discountTerm")
          .type("string")
          .restricted(Optional.of(false))
          .build(),
        FieldSearchConfiguration.builder()
          .name("settlementPeriod")
          .type("int")
          .restricted(Optional.of(false))
          .build(),
        FieldSearchConfiguration.builder()
          .name("settlementPeriodType")
          .type("string")
          .restricted(Optional.of(false))
          .build(),
        FieldSearchConfiguration.builder()
          .name("settlementPeriodOffsetInDays")
          .type("int")
          .restricted(Optional.of(false))
          .build(),
        FieldSearchConfiguration.builder()
          .name("gracePeriod")
          .type("int")
          .restricted(Optional.of(false))
          .build(),
        FieldSearchConfiguration.builder()
          .name("gracePeriodType")
          .type("string")
          .restricted(Optional.of(false))
          .build()
    );

    Collection<String> contentsZenCover = List.of(
          "Item+"
    );
    searchObjs.add(EntitySearchConfiguration.builder()
      .fields(searchFieldsZenCover)
      .entityName("ZenCover")
      .entityType("ProductRef")
      .contents(contentsZenCover)
      .build());

    Collection<FieldSearchConfiguration> searchFieldsItem = List.of(
        FieldSearchConfiguration.builder()
          .name("itemTypeCode")
          .type("string")
          .restricted(Optional.of(false))
          .build(),
        FieldSearchConfiguration.builder()
          .name("purchaseDate")
          .type("date")
          .restricted(Optional.of(false))
          .build(),
        FieldSearchConfiguration.builder()
          .name("purchasePrice")
          .type("decimal")
          .restricted(Optional.of(false))
          .build(),
        FieldSearchConfiguration.builder()
          .name("manufacturerLabourGuaranteePeriod")
          .type("int")
          .restricted(Optional.of(false))
          .build(),
        FieldSearchConfiguration.builder()
          .name("itemInWarrantyAtTakeup")
          .type("boolean")
          .restricted(Optional.of(false))
          .build(),
        FieldSearchConfiguration.builder()
          .name("itemInWorkingOrderAtTakeup")
          .type("boolean")
          .restricted(Optional.of(false))
          .build(),
        FieldSearchConfiguration.builder()
          .name("serialNumber")
          .type("int")
          .restricted(Optional.of(false))
          .build()
    );

    Collection<String> contentsItem = List.of(
          "AccidentalDamage?", 
          "Breakdown!", 
          "Theft?"
    );
    searchObjs.add(EntitySearchConfiguration.builder()
      .fields(searchFieldsItem)
      .entityName("Item")
      .entityType("ElementRef")
      .contents(contentsItem)
      .build());

    Collection<FieldSearchConfiguration> searchFieldsAccidentalDamage = List.of(
        FieldSearchConfiguration.builder()
          .name("labourCovered")
          .type("string")
          .restricted(Optional.of(false))
          .build(),
        FieldSearchConfiguration.builder()
          .name("partsCovered")
          .type("string")
          .restricted(Optional.of(false))
          .build()
    );

    Collection<String> contentsAccidentalDamage = List.of(
    );
    searchObjs.add(EntitySearchConfiguration.builder()
      .fields(searchFieldsAccidentalDamage)
      .entityName("AccidentalDamage")
      .entityType("ElementRef")
      .contents(contentsAccidentalDamage)
      .build());

    Collection<FieldSearchConfiguration> searchFieldsBreakdown = List.of(
        FieldSearchConfiguration.builder()
          .name("labourCovered")
          .type("string")
          .restricted(Optional.of(false))
          .build(),
        FieldSearchConfiguration.builder()
          .name("partsCovered")
          .type("string")
          .restricted(Optional.of(false))
          .build(),
        FieldSearchConfiguration.builder()
          .name("manufacturerLabourGuaranteeEndDate")
          .type("date")
          .restricted(Optional.of(false))
          .build(),
        FieldSearchConfiguration.builder()
          .name("manufacturerPartsGuaranteeEndDate")
          .type("date")
          .restricted(Optional.of(false))
          .build()
    );

    Collection<String> contentsBreakdown = List.of(
    );
    searchObjs.add(EntitySearchConfiguration.builder()
      .fields(searchFieldsBreakdown)
      .entityName("Breakdown")
      .entityType("ElementRef")
      .contents(contentsBreakdown)
      .build());

    Collection<FieldSearchConfiguration> searchFieldsStandardPayment = List.of(
        FieldSearchConfiguration.builder()
          .name("paidDate")
          .type("date")
          .restricted(Optional.of(false))
          .build(),
        FieldSearchConfiguration.builder()
          .name("paymentChannelCode")
          .type("string")
          .restricted(Optional.of(false))
          .build(),
        FieldSearchConfiguration.builder()
          .name("errorCode")
          .type("string")
          .restricted(Optional.of(false))
          .build()
    );

    Collection<String> contentsStandardPayment = List.of(
    );
    searchObjs.add(EntitySearchConfiguration.builder()
      .fields(searchFieldsStandardPayment)
      .entityName("StandardPayment")
      .entityType("PaymentRef")
      .contents(contentsStandardPayment)
      .build());

    this.searchConfiguration = SearchConfiguration.builder()
      .defaultSearchable(true)
      .objects(Collections.unmodifiableCollection(searchObjs))
      .dataAccessControlEnabled(Optional.of(false))
      .dataMaskingEnabled(Optional.of(false))
      .enableCustomerDataEncryption(Optional.of(false))
      .build();
  }

  private void initAutomations() {
    this.automations = Map.ofEntries(
        Map.entry("CancellationInvoices".toLowerCase(), AutomationPluginDetails.builder()
        .pluginName("CancellationInvoices")
        .enableWebhooks(false)
        .secret("")
        .actions(Map.ofEntries(
            Map.entry("loadInvoiceAuxData".toLowerCase(), AutomationActionDetails.builder()
            .methodName("loadInvoiceAuxData")
            .timeout(30)
            .takesRequest(true)
            .returnsResponse(true)
            .build()
            )
        )).build())
    );
  }

  @Override
  public Optional<DocumentConfig> getDocumentConfig(String name) {
    return Optional.ofNullable(DocumentConfigs.getDocumentConfig(name));
  }

  @Override
  public Optional<TemplateSnippetConfig> getTemplateSnippetConfig(String name) {
    return Optional.ofNullable(DocumentConfigs.getTemplateSnippetConfig(name));
  }

  @Override
  public Optional<ConsolidatedDocumentConfig> getConsolidatedDocumentConfig(String name) {
    return Optional.ofNullable(DocumentConfigs.getConsolidatedDocumentConfig(name));
  }

  @Override
  public boolean hasCustomFont(String fontName) {
    return DocumentConfigs.hasCustomFont(fontName);
  }

  @Override
  public Optional<MoratoriumConfig> getMoratorium(String name) {
    return Optional.ofNullable(MoratoriumConfigs.getMoratorium(name));
  }

  @Override
  public Map<String, MoratoriumConfig> getMoratoriums() {
    return MoratoriumConfigs.getMoratoriums();
  }

  private static String trimSuffix(String input, String suffix) {
    if (input.endsWith(suffix)) {
      return input.substring(0, input.length() - suffix.length()).trim();
    } else {
      return input;
    }
  }
}