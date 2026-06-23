package com.socotra.deployment.customer;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.function.Function;
import com.socotra.coremodel.*;
import com.socotra.coremodel.interfaces.EventType;
import com.socotra.deployment.*;
import com.socotra.deployment.workmanagement.*;

public final class CustomerConfig extends DeploymentConfig {

  public CustomerConfig(DeploymentFactory factory) {
    this.factory = factory;
    this.products = Map.ofEntries(
        Map.entry(trimSuffix("PremiumCreditCardProtectionProduct", "Product").toLowerCase(), new PremiumCreditCardProtectionProduct()),
        Map.entry(trimSuffix("BasicCreditCardProtectionProduct", "Product").toLowerCase(), new BasicCreditCardProtectionProduct())
    );
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
    this.numberingPlans = NumberingPlans.PLANS.entrySet().stream().collect(
        Collectors.toMap(e -> trimSuffix(e.getKey(), "NumberingPlan").toLowerCase(), Map.Entry::getValue));
    this.typeToNumberingPlan = new HashMap();
    this.accounts = Map.ofEntries(
        Map.entry(BankCustomerAccount.TYPE.toLowerCase(), BankCustomerAccount.class)
    );
    this.tables = Map.ofEntries(
    );
    this.constraintTables = Map.ofEntries(
    );
    this.secrets = Map.ofEntries(
    );
    this.payments = Map.ofEntries(
    );
    this.disbursements = Map.ofEntries(
    );
    this.contacts = Map.ofEntries(
    );
    this.contactRoles = Set.of(
    );
    Map<String, Class<? extends CustomerObject>> m = new HashMap<>();
    m.put(PurchaseProtectionQuote.TYPE.toLowerCase(), PurchaseProtectionQuote.class);
    m.put(BasicCreditCardProtectionQuickQuoteStaticData.TYPE.toLowerCase(), BasicCreditCardProtectionQuickQuoteStaticData.class);
    m.put(PremiumCreditCardProtectionQuickQuote.PremiumCreditCardProtectionQuickQuoteData.TYPE.toLowerCase(), PremiumCreditCardProtectionQuickQuote.PremiumCreditCardProtectionQuickQuoteData.class);
    m.put(PremiumCreditCardProtectionQuickQuoteStaticData.TYPE.toLowerCase(), PremiumCreditCardProtectionQuickQuoteStaticData.class);
    m.put(BasicCreditCardProtectionSegment.BasicCreditCardProtectionSegmentData.TYPE.toLowerCase(), BasicCreditCardProtectionSegment.BasicCreditCardProtectionSegmentData.class);
    m.put(PremiumCreditCardProtectionPolicyStaticData.TYPE.toLowerCase(), PremiumCreditCardProtectionPolicyStaticData.class);
    m.put(FraudLimit.TYPE.toLowerCase(), FraudLimit.class);
    m.put(CardReplacementBenefit.TYPE.toLowerCase(), CardReplacementBenefit.class);
    m.put(BasicCreditCardProtectionQuote.TYPE.toLowerCase(), BasicCreditCardProtectionQuote.class);
    m.put(PremiumCreditCardProtectionQuoteStaticData.TYPE.toLowerCase(), PremiumCreditCardProtectionQuoteStaticData.class);
    m.put(BasicCreditCardProtectionPolicyStaticData.TYPE.toLowerCase(), BasicCreditCardProtectionPolicyStaticData.class);
    m.put(CardReplacementQuote.TYPE.toLowerCase(), CardReplacementQuote.class);
    m.put(IdentityTheftProtectionPolicy.TYPE.toLowerCase(), IdentityTheftProtectionPolicy.class);
    m.put(PremiumCreditCardProtectionQuickQuote.TYPE.toLowerCase(), PremiumCreditCardProtectionQuickQuote.class);
    m.put(IdentityTheftProtectionQuote.TYPE.toLowerCase(), IdentityTheftProtectionQuote.class);
    m.put(UnauthorizedUseQuickQuote.TYPE.toLowerCase(), UnauthorizedUseQuickQuote.class);
    m.put(PremiumCreditCardProtectionSegment.TYPE.toLowerCase(), PremiumCreditCardProtectionSegment.class);
    m.put(PremiumCreditCardProtectionQuote.PremiumCreditCardProtectionQuoteData.TYPE.toLowerCase(), PremiumCreditCardProtectionQuote.PremiumCreditCardProtectionQuoteData.class);
    m.put(IdentityTheftLimit.TYPE.toLowerCase(), IdentityTheftLimit.class);
    m.put(IdentityTheftProtectionQuickQuote.TYPE.toLowerCase(), IdentityTheftProtectionQuickQuote.class);
    m.put(FraudProtectionPolicy.TYPE.toLowerCase(), FraudProtectionPolicy.class);
    m.put(BasicCreditCardProtectionQuote.BasicCreditCardProtectionQuoteData.TYPE.toLowerCase(), BasicCreditCardProtectionQuote.BasicCreditCardProtectionQuoteData.class);
    m.put(PremiumDeductible.TYPE.toLowerCase(), PremiumDeductible.class);
    m.put(PurchaseProtectionPolicy.TYPE.toLowerCase(), PurchaseProtectionPolicy.class);
    m.put(TravelEmergencyAssistanceQuote.TYPE.toLowerCase(), TravelEmergencyAssistanceQuote.class);
    m.put(FraudProtectionQuote.TYPE.toLowerCase(), FraudProtectionQuote.class);
    m.put(CardReplacementQuickQuote.TYPE.toLowerCase(), CardReplacementQuickQuote.class);
    m.put(BasicCreditCardProtectionQuickQuote.TYPE.toLowerCase(), BasicCreditCardProtectionQuickQuote.class);
    m.put(TravelEmergencyAssistancePolicy.TYPE.toLowerCase(), TravelEmergencyAssistancePolicy.class);
    m.put(TravelAssistanceLimit.TYPE.toLowerCase(), TravelAssistanceLimit.class);
    m.put(PremiumCreditCardProtectionSegment.PremiumCreditCardProtectionSegmentData.TYPE.toLowerCase(), PremiumCreditCardProtectionSegment.PremiumCreditCardProtectionSegmentData.class);
    m.put(UnauthorizedUsePolicy.TYPE.toLowerCase(), UnauthorizedUsePolicy.class);
    m.put(CardReplacementPolicy.TYPE.toLowerCase(), CardReplacementPolicy.class);
    m.put(TravelEmergencyAssistanceQuickQuote.TYPE.toLowerCase(), TravelEmergencyAssistanceQuickQuote.class);
    m.put(BasicCreditCardProtectionQuickQuote.BasicCreditCardProtectionQuickQuoteData.TYPE.toLowerCase(), BasicCreditCardProtectionQuickQuote.BasicCreditCardProtectionQuickQuoteData.class);
    m.put(UnauthorizedUseQuote.TYPE.toLowerCase(), UnauthorizedUseQuote.class);
    m.put(PurchaseProtectionLimit.TYPE.toLowerCase(), PurchaseProtectionLimit.class);
    m.put(BasicCreditCardProtectionSegment.TYPE.toLowerCase(), BasicCreditCardProtectionSegment.class);
    m.put(FraudProtectionQuickQuote.TYPE.toLowerCase(), FraudProtectionQuickQuote.class);
    m.put(BasicCreditCardProtectionQuoteStaticData.TYPE.toLowerCase(), BasicCreditCardProtectionQuoteStaticData.class);
    m.put(BasicDeductible.TYPE.toLowerCase(), BasicDeductible.class);
    m.put(PurchaseProtectionQuickQuote.TYPE.toLowerCase(), PurchaseProtectionQuickQuote.class);
    m.put(PremiumCreditCardProtectionQuote.TYPE.toLowerCase(), PremiumCreditCardProtectionQuote.class);
    this.elements = Collections.unmodifiableMap(m);
    this.customEvents = com.socotra.deployment.customer.CustomEvent.map();
    this.customEventsByType = this.customEvents.values().stream().collect(Collectors.toMap(com.socotra.coremodel.CustomEvent::eventType, Function.identity()));
    this.resourceDeclarations.putAll(DocumentConfigs.toResourceDeclaration());
    this.regions = Set.of();
    this.regionNumberingStrings = Map.of();
    this.defaultRegion = "";
    this.defaultCurrency = "USD";
    this.defaultTimezone = "America/New_York";
    this.defaultInstallmentPlanName = "Standard";
    this.defaultInvoicingPlanName = "";
    this.defaultPaymentNumberingPlanName = "";
    this.defaultDisbursementNumberingPlanName = "";
    this.defaultBillingPlanName = "";
    this.defaultDelinquencyPlanName = "Standard";
    this.defaultAutoRenewalPlanName = "Standard";
    this.defaultExcessCreditPlanName = "Standard";
    this.defaultShortfallTolerancePlanName = "";
    this.defaultBillingTrigger = BillingTrigger.issue;
    this.defaultBackdatedInstallmentsBilling = BackdatedInstallmentsBilling.deferDueDate;
    this.defaultInvoiceDocument = "";
    this.isSerialInvoiceNumberingEnabled = false;
    this.isEntityAnonymizationEnabled = false;

    this.auxDataSettings = Map.of();

    this.defaultAuxDataSettings = "";

    Collection searchObjs = new ArrayList();
    Collection<FieldSearchConfiguration> searchFieldsBankCustomerAccount = List.of(
        FieldSearchConfiguration.builder()
        .name("city")
        .type("string?")
.restricted(Optional.of(false))        .build(),
        FieldSearchConfiguration.builder()
        .name("email")
        .type("string")
.restricted(Optional.of(false))        .build(),
        FieldSearchConfiguration.builder()
        .name("state")
        .type("string?")
.restricted(Optional.of(false))        .build(),
        FieldSearchConfiguration.builder()
        .name("address")
        .type("string?")
.restricted(Optional.of(false))        .build(),
        FieldSearchConfiguration.builder()
        .name("zipCode")
        .type("string?")
.restricted(Optional.of(false))        .build(),
        FieldSearchConfiguration.builder()
        .name("lastName")
        .type("string")
.restricted(Optional.of(false))        .build(),
        FieldSearchConfiguration.builder()
        .name("firstName")
        .type("string")
.restricted(Optional.of(false))        .build(),
        FieldSearchConfiguration.builder()
        .name("dateOfBirth")
        .type("date")
.restricted(Optional.of(false))        .build(),
        FieldSearchConfiguration.builder()
        .name("phoneNumber")
        .type("string")
.restricted(Optional.of(false))        .build(),
        FieldSearchConfiguration.builder()
        .name("customerSince")
        .type("date")
.restricted(Optional.of(false))        .build(),
        FieldSearchConfiguration.builder()
        .name("bankAccountNumber")
        .type("string")
.restricted(Optional.of(false))        .build()
    );

    Collection<String> contentsBankCustomerAccount = List.of(
    );
    searchObjs.add(EntitySearchConfiguration.builder()
        .fields(searchFieldsBankCustomerAccount)
        .entityName("BankCustomerAccount")
        .entityType("AccountRef")
        .contents(contentsBankCustomerAccount)
        .build());

    Collection<FieldSearchConfiguration> searchFieldsBasicCreditCardProtection = List.of(
        FieldSearchConfiguration.builder()
        .name("cardType")
        .type("string")
.restricted(Optional.of(false))        .build(),
        FieldSearchConfiguration.builder()
        .name("creditLimit")
        .type("decimal")
.restricted(Optional.of(false))        .build(),
        FieldSearchConfiguration.builder()
        .name("numberOfCards")
        .type("decimal")
.restricted(Optional.of(false))        .build(),
        FieldSearchConfiguration.builder()
        .name("cardIssuedDate")
        .type("date")
.restricted(Optional.of(false))        .build(),
        FieldSearchConfiguration.builder()
        .name("hasPreviousClaims")
        .type("string")
.restricted(Optional.of(false))        .build(),
        FieldSearchConfiguration.builder()
        .name("bankRelationshipYears")
        .type("decimal")
.restricted(Optional.of(false))        .build()
    );

    Collection<String> contentsBasicCreditCardProtection = List.of(
        "FraudProtection!", 
        "CardReplacement!", 
        "UnauthorizedUse!"
    );
    searchObjs.add(EntitySearchConfiguration.builder()
        .fields(searchFieldsBasicCreditCardProtection)
        .entityName("BasicCreditCardProtection")
        .entityType("ProductRef")
        .contents(contentsBasicCreditCardProtection)
        .build());

    Collection<FieldSearchConfiguration> searchFieldsPremiumCreditCardProtection = List.of(
        FieldSearchConfiguration.builder()
        .name("cardType")
        .type("string")
.restricted(Optional.of(false))        .build(),
        FieldSearchConfiguration.builder()
        .name("creditLimit")
        .type("decimal")
.restricted(Optional.of(false))        .build(),
        FieldSearchConfiguration.builder()
        .name("numberOfCards")
        .type("decimal")
.restricted(Optional.of(false))        .build(),
        FieldSearchConfiguration.builder()
        .name("cardIssuedDate")
        .type("date")
.restricted(Optional.of(false))        .build(),
        FieldSearchConfiguration.builder()
        .name("frequentTraveler")
        .type("string")
.restricted(Optional.of(false))        .build(),
        FieldSearchConfiguration.builder()
        .name("hasPreviousClaims")
        .type("string")
.restricted(Optional.of(false))        .build(),
        FieldSearchConfiguration.builder()
        .name("averageMonthlySpend")
        .type("decimal")
.restricted(Optional.of(false))        .build(),
        FieldSearchConfiguration.builder()
        .name("bankRelationshipYears")
        .type("decimal")
.restricted(Optional.of(false))        .build()
    );

    Collection<String> contentsPremiumCreditCardProtection = List.of(
        "FraudProtection!", 
        "IdentityTheftProtection!", 
        "CardReplacement!", 
        "UnauthorizedUse!", 
        "PurchaseProtection!", 
        "TravelEmergencyAssistance!"
    );
    searchObjs.add(EntitySearchConfiguration.builder()
        .fields(searchFieldsPremiumCreditCardProtection)
        .entityName("PremiumCreditCardProtection")
        .entityType("ProductRef")
        .contents(contentsPremiumCreditCardProtection)
        .build());

    this.searchConfiguration = SearchConfiguration.builder()
    .defaultSearchable(true)
    .objects(Collections.unmodifiableCollection(searchObjs))
    .dataAccessControlEnabled(Optional.of(false))
    .dataMaskingEnabled(Optional.of(false))
    .build();

    buildObjectRepository();
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

  private static String trimSuffix(String input, String suffix) {
    if (input.endsWith(suffix)) {
      return input.substring(0, input.length() - suffix.length()).trim();
    } else {
      return input;
    }
  }
}