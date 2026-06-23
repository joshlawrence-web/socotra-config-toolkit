package com.socotra.deployment.customer;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.type.*;
import com.socotra.platform.tools.ULID;
import com.socotra.coremodel.*;
import com.socotra.coremodel.views.Internal;
import com.socotra.deployment.*;

import java.math.*;
import java.time.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.*;
import java.util.stream.Collectors;

public record PremiumCreditCardProtectionQuote(
    ULID locator,
    ULID groupLocator,
    QuoteState quoteState,
    String productName,
    ULID accountLocator,
    Optional<Instant> createdAt,
    Optional<UUID> createdBy,
    Optional<Instant> startTime,
    Optional<Instant> endTime,
    Optional<String> timezone,
    Optional<String> currency,
    Optional<String> underwritingStatus,
    Optional<Instant> expirationTime,
    Optional<Preferences> preferences,
    Optional<ULID> policyLocator,
    Optional<DurationBasis> durationBasis,
    Optional<String> delinquencyPlanName,
    Optional<String> autoRenewalPlanName,
    Optional<BillingTrigger> billingTrigger,
    Optional<String> region,
    FraudProtectionQuote fraudProtection,
    IdentityTheftProtectionQuote identityTheftProtection,
    CardReplacementQuote cardReplacement,
    UnauthorizedUseQuote unauthorizedUse,
    PurchaseProtectionQuote purchaseProtection,
    TravelEmergencyAssistanceQuote travelEmergencyAssistance,
    FraudLimit fraudLimit,
    IdentityTheftLimit identityTheftLimit,
    PurchaseProtectionLimit purchaseProtectionLimit,
    TravelAssistanceLimit travelAssistanceLimit,
    CardReplacementBenefit cardReplacementBenefit,
    PremiumDeductible premiumDeductible,
    PremiumCreditCardProtectionQuoteData data,
    @JsonView({Internal.class}) Element element,
    BillingLevel billingLevel,
    Optional<String> quoteNumber,
    Collection<ContactRoles> contacts,
    Optional<BigDecimal> invoiceFeeAmount) implements PremiumCreditCardProtection, Validatable, com.socotra.coremodel.interfaces.Quote, Elemental, CustomerObjectWithData<PremiumCreditCardProtectionQuote.PremiumCreditCardProtectionQuoteData>, CustomerObject, ContactsHolder, NumberingTriggerHolder {

  public static final String TYPE = "PremiumCreditCardProtectionQuote";
  private static final Map<String, ContactSlot> CONTACT_SLOTS = Map.ofEntries(

  );

  public Map<String, ContactSlot> contactSlots() { return CONTACT_SLOTS; }

  public NumberingTrigger numberingTrigger() {
    return NumberingTrigger.validation;
  }

  public PremiumCreditCardProtectionQuote {
    
    
    
    
    
    
    if(data == null) {
      data = PremiumCreditCardProtectionQuoteData.builder().build();
    }
    contacts = contacts == null ? List.of() : contacts;
    invoiceFeeAmount = invoiceFeeAmount == null ? Optional.empty() : invoiceFeeAmount;
  }

  public String type() { return TYPE; }

  public PremiumCreditCardProtectionQuote maskData(DataMaskingLevel level) {
    PremiumCreditCardProtectionQuoteBuilder builder = toBuilder();
    builder.data(this.data.maskData(level));
    if (this.fraudProtection != null) {
      builder.fraudProtection(fraudProtection.maskData(level));
    }
    if (this.identityTheftProtection != null) {
      builder.identityTheftProtection(identityTheftProtection.maskData(level));
    }
    if (this.cardReplacement != null) {
      builder.cardReplacement(cardReplacement.maskData(level));
    }
    if (this.unauthorizedUse != null) {
      builder.unauthorizedUse(unauthorizedUse.maskData(level));
    }
    if (this.purchaseProtection != null) {
      builder.purchaseProtection(purchaseProtection.maskData(level));
    }
    if (this.travelEmergencyAssistance != null) {
      builder.travelEmergencyAssistance(travelEmergencyAssistance.maskData(level));
    }
    return builder.build();
  }

  public PremiumCreditCardProtectionQuote anonymizeData() {
    PremiumCreditCardProtectionQuoteBuilder builder = toBuilder();
    builder.data(this.data.anonymizeData());
    if (this.fraudProtection != null) {
      builder.fraudProtection(fraudProtection.anonymizeData());
    }
    if (this.identityTheftProtection != null) {
      builder.identityTheftProtection(identityTheftProtection.anonymizeData());
    }
    if (this.cardReplacement != null) {
      builder.cardReplacement(cardReplacement.anonymizeData());
    }
    if (this.unauthorizedUse != null) {
      builder.unauthorizedUse(unauthorizedUse.anonymizeData());
    }
    if (this.purchaseProtection != null) {
      builder.purchaseProtection(purchaseProtection.anonymizeData());
    }
    if (this.travelEmergencyAssistance != null) {
      builder.travelEmergencyAssistance(travelEmergencyAssistance.anonymizeData());
    }
    return builder.build();
  }

  @Override
  public Collection<ValidationItem> validate(DeploymentConfig config, ValidatableContext context) {
    Collection<ValidationItem> validationItems = new ArrayList<>();
    ValidationItem.ValidationItemBuilder validationItemBuilder = ValidationItem.builder()
      .elementType(type()).locator(element.locator());
    if (this.groupLocator == null) { validationItemBuilder.addError("Non optional property 'groupLocator' missing"); }
    if (this.endTime.isEmpty()) { validationItemBuilder.addError("endTime is required but missing"); }
    if (this.startTime.isEmpty()) { validationItemBuilder.addError("startTime is required but missing"); }
    if (this.startTime.get().isAfter(this.endTime.get())) { validationItemBuilder.addError("startTime[" + startTime.get() + "] is after endTime[" + endTime.get() + "]"); }
    if (this.accountLocator == null) { validationItemBuilder.addError("Non optional property 'accountLocator' missing"); }
    this.region.ifPresent(r -> { if (!config.getRegions().contains(r)) { validationItemBuilder.addError("'region' should be one of " + config.getRegions()); }; } );
    if (this.fraudProtection == null) {
      validationItemBuilder.addError("'fraudProtection' is required");
    } else {
      validationItems.addAll(this.fraudProtection.validate(config, context));
    }

    if (this.identityTheftProtection == null) {
      validationItemBuilder.addError("'identityTheftProtection' is required");
    } else {
      validationItems.addAll(this.identityTheftProtection.validate(config, context));
    }

    if (this.cardReplacement == null) {
      validationItemBuilder.addError("'cardReplacement' is required");
    } else {
      validationItems.addAll(this.cardReplacement.validate(config, context));
    }

    if (this.unauthorizedUse == null) {
      validationItemBuilder.addError("'unauthorizedUse' is required");
    } else {
      validationItems.addAll(this.unauthorizedUse.validate(config, context));
    }

    if (this.purchaseProtection == null) {
      validationItemBuilder.addError("'purchaseProtection' is required");
    } else {
      validationItems.addAll(this.purchaseProtection.validate(config, context));
    }

    if (this.travelEmergencyAssistance == null) {
      validationItemBuilder.addError("'travelEmergencyAssistance' is required");
    } else {
      validationItems.addAll(this.travelEmergencyAssistance.validate(config, context));
    }

    if (this.fraudLimit == null) {
      validationItemBuilder.addError("'fraudLimit' is required");
    } else {
      validationItems.addAll(this.fraudLimit.validate(config, context));
    }

    if (this.identityTheftLimit == null) {
      validationItemBuilder.addError("'identityTheftLimit' is required");
    } else {
      validationItems.addAll(this.identityTheftLimit.validate(config, context));
    }

    if (this.purchaseProtectionLimit == null) {
      validationItemBuilder.addError("'purchaseProtectionLimit' is required");
    } else {
      validationItems.addAll(this.purchaseProtectionLimit.validate(config, context));
    }

    if (this.travelAssistanceLimit == null) {
      validationItemBuilder.addError("'travelAssistanceLimit' is required");
    } else {
      validationItems.addAll(this.travelAssistanceLimit.validate(config, context));
    }

    if (this.cardReplacementBenefit == null) {
      validationItemBuilder.addError("'cardReplacementBenefit' is required");
    } else {
      validationItems.addAll(this.cardReplacementBenefit.validate(config, context));
    }

    if (this.premiumDeductible == null) {
      validationItemBuilder.addError("'premiumDeductible' is required");
    } else {
      validationItems.addAll(this.premiumDeductible.validate(config, context));
    }

    this.data.validate(config, context).forEach(error -> error.errors().forEach(validationItemBuilder::addError));

    validationItemBuilder.addErrors(validateContacts());
    if (validationItemBuilder.hasErrors()) {
      validationItems.add(validationItemBuilder.build());
    }
    return validationItems;
  }

  public PremiumCreditCardProtectionQuote applyDefaults(DeploymentConfig config) {
    PremiumCreditCardProtectionQuoteBuilder builder = toBuilder();
    Product product = config.getProductRequired(this.productName);
    TimeService timeService = config.timeService(this.timezone.orElse(config.getDefaultTimezone()), product.defaultDurationBasis());
    if (this.currency.isEmpty()) {
      builder.currency(config.getDefaultCurrency());
    }
    if (this.timezone.isEmpty()) {
      builder.timezone(config.getDefaultTimezone());
    }
    if (this.startTime.isEmpty()) {
      builder.startTime(timeService.tomorrowMidnight());
    }
    if (this.endTime.isEmpty()) {
      builder.endTime(timeService.addDuration(this.startTime.isEmpty() ? timeService.tomorrowMidnight() : this.startTime.get(), product.defaultTermDuration()));
    }
    if (this.region.isEmpty() && !config.getRegions().isEmpty()) {
      builder.region(config.getDefaultRegion());
    }

    { FraudProtectionQuote a = this.fraudProtection; if (this.fraudProtection == null) { a = FraudProtectionQuote.builder(element.tenantLocator(), element.rootLocator(), element.locator(), null, null).build(); }; builder.fraudProtection(a.applyDefaults(config)); }

    { IdentityTheftProtectionQuote a = this.identityTheftProtection; if (this.identityTheftProtection == null) { a = IdentityTheftProtectionQuote.builder(element.tenantLocator(), element.rootLocator(), element.locator(), null, null).build(); }; builder.identityTheftProtection(a.applyDefaults(config)); }

    { CardReplacementQuote a = this.cardReplacement; if (this.cardReplacement == null) { a = CardReplacementQuote.builder(element.tenantLocator(), element.rootLocator(), element.locator(), null, null).build(); }; builder.cardReplacement(a.applyDefaults(config)); }

    { UnauthorizedUseQuote a = this.unauthorizedUse; if (this.unauthorizedUse == null) { a = UnauthorizedUseQuote.builder(element.tenantLocator(), element.rootLocator(), element.locator(), null, null).build(); }; builder.unauthorizedUse(a.applyDefaults(config)); }

    { PurchaseProtectionQuote a = this.purchaseProtection; if (this.purchaseProtection == null) { a = PurchaseProtectionQuote.builder(element.tenantLocator(), element.rootLocator(), element.locator(), null, null).build(); }; builder.purchaseProtection(a.applyDefaults(config)); }

    { TravelEmergencyAssistanceQuote a = this.travelEmergencyAssistance; if (this.travelEmergencyAssistance == null) { a = TravelEmergencyAssistanceQuote.builder(element.tenantLocator(), element.rootLocator(), element.locator(), null, null).build(); }; builder.travelEmergencyAssistance(a.applyDefaults(config)); }

    if (this.fraudLimit == null) { builder.fraudLimit(com.socotra.deployment.customer.FraudLimit.defaultOption()); }
    if (this.identityTheftLimit == null) { builder.identityTheftLimit(com.socotra.deployment.customer.IdentityTheftLimit.defaultOption()); }
    if (this.purchaseProtectionLimit == null) { builder.purchaseProtectionLimit(com.socotra.deployment.customer.PurchaseProtectionLimit.defaultOption()); }
    if (this.travelAssistanceLimit == null) { builder.travelAssistanceLimit(com.socotra.deployment.customer.TravelAssistanceLimit.defaultOption()); }
    if (this.cardReplacementBenefit == null) { builder.cardReplacementBenefit(com.socotra.deployment.customer.CardReplacementBenefit.defaultOption()); }
    if (this.premiumDeductible == null) { builder.premiumDeductible(com.socotra.deployment.customer.PremiumDeductible.defaultOption()); }
    return builder.build();
  }

  public PremiumCreditCardProtectionQuote applyAvailabilityRemovals(Instant referenceDate) {
    PremiumCreditCardProtectionQuoteBuilder builder = toBuilder();
    if (this.fraudProtection != null) {
      builder.fraudProtection(fraudProtection.applyAvailabilityRemovals(referenceDate));
    }

    if (this.identityTheftProtection != null) {
      builder.identityTheftProtection(identityTheftProtection.applyAvailabilityRemovals(referenceDate));
    }

    if (this.cardReplacement != null) {
      builder.cardReplacement(cardReplacement.applyAvailabilityRemovals(referenceDate));
    }

    if (this.unauthorizedUse != null) {
      builder.unauthorizedUse(unauthorizedUse.applyAvailabilityRemovals(referenceDate));
    }

    if (this.purchaseProtection != null) {
      builder.purchaseProtection(purchaseProtection.applyAvailabilityRemovals(referenceDate));
    }

    if (this.travelEmergencyAssistance != null) {
      builder.travelEmergencyAssistance(travelEmergencyAssistance.applyAvailabilityRemovals(referenceDate));
    }

    if (this.fraudLimit != null) {
      builder.fraudLimit(fraudLimit.applyAvailabilityRemovals(referenceDate));
    }

    if (this.identityTheftLimit != null) {
      builder.identityTheftLimit(identityTheftLimit.applyAvailabilityRemovals(referenceDate));
    }

    if (this.purchaseProtectionLimit != null) {
      builder.purchaseProtectionLimit(purchaseProtectionLimit.applyAvailabilityRemovals(referenceDate));
    }

    if (this.travelAssistanceLimit != null) {
      builder.travelAssistanceLimit(travelAssistanceLimit.applyAvailabilityRemovals(referenceDate));
    }

    if (this.cardReplacementBenefit != null) {
      builder.cardReplacementBenefit(cardReplacementBenefit.applyAvailabilityRemovals(referenceDate));
    }

    if (this.premiumDeductible != null) {
      builder.premiumDeductible(premiumDeductible.applyAvailabilityRemovals(referenceDate));
    }

    builder.data(this.data.applyAvailabilityRemovals(referenceDate));
    return builder.build();
  }

  public Element toElement(DeploymentFactory factory) {
    Map<String, Object> ct = new HashMap<>();
    ct.put("FraudLimit", this.fraudLimit == null ? null : (Enum.class.isAssignableFrom(this.fraudLimit.getClass()) ? this.fraudLimit.toString() : this.fraudLimit));
    ct.put("IdentityTheftLimit", this.identityTheftLimit == null ? null : (Enum.class.isAssignableFrom(this.identityTheftLimit.getClass()) ? this.identityTheftLimit.toString() : this.identityTheftLimit));
    ct.put("PurchaseProtectionLimit", this.purchaseProtectionLimit == null ? null : (Enum.class.isAssignableFrom(this.purchaseProtectionLimit.getClass()) ? this.purchaseProtectionLimit.toString() : this.purchaseProtectionLimit));
    ct.put("TravelAssistanceLimit", this.travelAssistanceLimit == null ? null : (Enum.class.isAssignableFrom(this.travelAssistanceLimit.getClass()) ? this.travelAssistanceLimit.toString() : this.travelAssistanceLimit));
    ct.put("CardReplacementBenefit", this.cardReplacementBenefit == null ? null : (Enum.class.isAssignableFrom(this.cardReplacementBenefit.getClass()) ? this.cardReplacementBenefit.toString() : this.cardReplacementBenefit));
    ct.put("PremiumDeductible", this.premiumDeductible == null ? null : (Enum.class.isAssignableFrom(this.premiumDeductible.getClass()) ? this.premiumDeductible.toString() : this.premiumDeductible));
    ct = ct.entrySet().stream().filter(e -> e.getValue() != null).collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    Collection<Element> subElements = new ArrayList<>();
    if (this.fraudProtection != null) {
      subElements.add(this.fraudProtection.toElement(factory));
    }
    if (this.identityTheftProtection != null) {
      subElements.add(this.identityTheftProtection.toElement(factory));
    }
    if (this.cardReplacement != null) {
      subElements.add(this.cardReplacement.toElement(factory));
    }
    if (this.unauthorizedUse != null) {
      subElements.add(this.unauthorizedUse.toElement(factory));
    }
    if (this.purchaseProtection != null) {
      subElements.add(this.purchaseProtection.toElement(factory));
    }
    if (this.travelEmergencyAssistance != null) {
      subElements.add(this.travelEmergencyAssistance.toElement(factory));
    }

    Map<String, Object> data = CustomerDataHolder.transform(factory, this.data());
    return element.toBuilder()
        .type(type())
        .elements(Collections.unmodifiableCollection(subElements))
        .coverageTerms(ct.entrySet().stream().filter(x -> x.getValue() != null).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
        .data(data)
        .category(ElementCategory.product)
        .build();
  }

  public static PremiumCreditCardProtectionQuote from(DeploymentFactory factory, com.socotra.coremodel.interfaces.Quote quote) {
    if (quote == null) {
      return null;
    }
    Element element = quote.element();
    return new PremiumCreditCardProtectionQuote(
        quote.locator(),
        quote.groupLocator(),
        quote.quoteState(),
        quote.productName(),
        quote.accountLocator(),
        quote.createdAt(),
        quote.createdBy(),
        quote.startTime(),
        quote.endTime(),
        quote.timezone(),
        quote.currency(),
        quote.underwritingStatus(),
        quote.expirationTime(),
        quote.preferences(),
        quote.policyLocator(),
        quote.durationBasis(),
        quote.delinquencyPlanName(),
        quote.autoRenewalPlanName(),
        quote.billingTrigger(),
        quote.region(),
        FraudProtectionQuote.fromElement(factory, element.elements().stream().filter(e -> e.type().equalsIgnoreCase(FraudProtectionQuote.TYPE)).findAny().orElse(null)),
        IdentityTheftProtectionQuote.fromElement(factory, element.elements().stream().filter(e -> e.type().equalsIgnoreCase(IdentityTheftProtectionQuote.TYPE)).findAny().orElse(null)),
        CardReplacementQuote.fromElement(factory, element.elements().stream().filter(e -> e.type().equalsIgnoreCase(CardReplacementQuote.TYPE)).findAny().orElse(null)),
        UnauthorizedUseQuote.fromElement(factory, element.elements().stream().filter(e -> e.type().equalsIgnoreCase(UnauthorizedUseQuote.TYPE)).findAny().orElse(null)),
        PurchaseProtectionQuote.fromElement(factory, element.elements().stream().filter(e -> e.type().equalsIgnoreCase(PurchaseProtectionQuote.TYPE)).findAny().orElse(null)),
        TravelEmergencyAssistanceQuote.fromElement(factory, element.elements().stream().filter(e -> e.type().equalsIgnoreCase(TravelEmergencyAssistanceQuote.TYPE)).findAny().orElse(null)),
        com.socotra.deployment.customer.FraudLimit.fromMap(element.coverageTerms()),
        com.socotra.deployment.customer.IdentityTheftLimit.fromMap(element.coverageTerms()),
        com.socotra.deployment.customer.PurchaseProtectionLimit.fromMap(element.coverageTerms()),
        com.socotra.deployment.customer.TravelAssistanceLimit.fromMap(element.coverageTerms()),
        com.socotra.deployment.customer.CardReplacementBenefit.fromMap(element.coverageTerms()),
        com.socotra.deployment.customer.PremiumDeductible.fromMap(element.coverageTerms()),
        element.resolve(PremiumCreditCardProtectionQuoteData.TYPE, factory),
        element,
        quote.billingLevel(),
        quote.quoteNumber(),
        quote.contacts(),
        quote.invoiceFeeAmount()
    );
  }

  public static PremiumCreditCardProtectionQuoteBuilder builder() {
    return new PremiumCreditCardProtectionQuoteBuilder(QuoteState.draft, Optional.empty(), Optional.empty());
  }

  public PremiumCreditCardProtectionQuoteBuilder toBuilder() {
    return new PremiumCreditCardProtectionQuoteBuilder(this.quoteState, this.createdAt, this.createdBy)
        .locator(this.locator)
        .groupLocator(this.groupLocator)
        .productName(this.productName)
        .accountLocator(this.accountLocator)
        .startTime(this.startTime)
        .endTime(this.endTime)
        .timezone(this.timezone)
        .currency(this.currency)
        .underwritingStatus(this.underwritingStatus)
        .expirationTime(this.expirationTime)
        .preferences(this.preferences)
        .policyLocator(this.policyLocator)
        .durationBasis(this.durationBasis)
        .delinquencyPlanName(this.delinquencyPlanName)
        .autoRenewalPlanName(this.autoRenewalPlanName)
        .billingTrigger(this.billingTrigger)
        .region(this.region)
        .fraudProtection(this.fraudProtection)
.identityTheftProtection(this.identityTheftProtection)
.cardReplacement(this.cardReplacement)
.unauthorizedUse(this.unauthorizedUse)
.purchaseProtection(this.purchaseProtection)
.travelEmergencyAssistance(this.travelEmergencyAssistance)

        .fraudLimit(this.fraudLimit)
.identityTheftLimit(this.identityTheftLimit)
.purchaseProtectionLimit(this.purchaseProtectionLimit)
.travelAssistanceLimit(this.travelAssistanceLimit)
.cardReplacementBenefit(this.cardReplacementBenefit)
.premiumDeductible(this.premiumDeductible)

        .data(this.data)
        .element(this.element)
        .billingLevel(this.billingLevel)
        .quoteNumber(this.quoteNumber)
        .contacts(this.contacts)
        .invoiceFeeAmount(this.invoiceFeeAmount);
  }

  public static class PremiumCreditCardProtectionQuoteBuilder {
    private final QuoteState quoteState;
    private Optional<UUID> createdBy;
    private Optional<Instant> createdAt;
    private PremiumCreditCardProtectionQuoteBuilder(QuoteState quoteState, Optional<Instant> createdAt, Optional<UUID> createdBy) {
      this.quoteState = quoteState;
      this.createdAt = createdAt;
      this.createdBy = createdBy;
    }
    private Optional<BigDecimal> invoiceFeeAmount;
    public PremiumCreditCardProtectionQuoteBuilder invoiceFeeAmount(Optional<BigDecimal> invoiceFeeAmount) { this.invoiceFeeAmount = invoiceFeeAmount; return this; }
    private Collection<ContactRoles> contacts;
    public PremiumCreditCardProtectionQuoteBuilder contacts(Collection<ContactRoles> contacts) { this.contacts = contacts; return this; }
    public PremiumCreditCardProtectionQuoteBuilder addContact(ContactRoles contact) { if (!(this.contacts instanceof ArrayList)) { this.contacts = new ArrayList<>(this.contacts); }; this.contacts.add(contact); return this; }
    private ULID locator;
    public PremiumCreditCardProtectionQuoteBuilder locator(ULID locator) { this.locator = locator; return this; }
    private ULID groupLocator;
    public PremiumCreditCardProtectionQuoteBuilder groupLocator(ULID groupLocator) { this.groupLocator = groupLocator; return this; }
    private String productName;
    public PremiumCreditCardProtectionQuoteBuilder productName(String productName) { this.productName = productName; return this; }
    private ULID accountLocator;
    public PremiumCreditCardProtectionQuoteBuilder accountLocator(ULID accountLocator) { this.accountLocator = accountLocator; return this; }
    private Optional<Instant> startTime;
    public PremiumCreditCardProtectionQuoteBuilder startTime(Instant startTime) { this.startTime = Optional.ofNullable(startTime); return this; }
    public PremiumCreditCardProtectionQuoteBuilder startTime(Optional<Instant> startTime) { this.startTime = startTime; return this; }
    private Optional<Instant> endTime;
    public PremiumCreditCardProtectionQuoteBuilder endTime(Instant endTime) { this.endTime = Optional.ofNullable(endTime); return this; }
    public PremiumCreditCardProtectionQuoteBuilder endTime(Optional<Instant> endTime) { this.endTime = endTime; return this; }
    private Optional<String> timezone;
    public PremiumCreditCardProtectionQuoteBuilder timezone(String timezone) { this.timezone = Optional.ofNullable(timezone); return this; }
    public PremiumCreditCardProtectionQuoteBuilder timezone(Optional<String> timezone) { this.timezone = timezone; return this; }
    private Optional<String> currency;
    public PremiumCreditCardProtectionQuoteBuilder currency(String currency) { this.currency = Optional.ofNullable(currency); return this; }
    public PremiumCreditCardProtectionQuoteBuilder currency(Optional<String> currency) { this.currency = currency; return this; }
    private Optional<String> underwritingStatus;
    public PremiumCreditCardProtectionQuoteBuilder underwritingStatus(String underwritingStatus) { this.underwritingStatus = Optional.ofNullable(underwritingStatus); return this; }
    public PremiumCreditCardProtectionQuoteBuilder underwritingStatus(Optional<String> underwritingStatus) { this.underwritingStatus = underwritingStatus; return this; }
    private Optional<Instant> expirationTime;
    public PremiumCreditCardProtectionQuoteBuilder expirationTime(Instant expirationTime) { this.expirationTime = Optional.ofNullable(expirationTime); return this; }
    public PremiumCreditCardProtectionQuoteBuilder expirationTime(Optional<Instant> expirationTime) { this.expirationTime = expirationTime; return this; }
    private Optional<Preferences> preferences;
    public PremiumCreditCardProtectionQuoteBuilder preferences(Preferences preferences) { this.preferences = Optional.ofNullable(preferences); return this; }
    public PremiumCreditCardProtectionQuoteBuilder preferences(Optional<Preferences> preferences) { this.preferences = preferences; return this; }
    private Optional<ULID> policyLocator;
    public PremiumCreditCardProtectionQuoteBuilder policyLocator(ULID policyLocator) { this.policyLocator = Optional.ofNullable(policyLocator); return this; }
    public PremiumCreditCardProtectionQuoteBuilder policyLocator(Optional<ULID> policyLocator) { this.policyLocator = policyLocator; return this; }
    private Optional<DurationBasis> durationBasis;
    public PremiumCreditCardProtectionQuoteBuilder durationBasis(DurationBasis durationBasis) { this.durationBasis = Optional.ofNullable(durationBasis); return this; }
    public PremiumCreditCardProtectionQuoteBuilder durationBasis(Optional<DurationBasis> durationBasis) { this.durationBasis = durationBasis; return this; }
    private Optional<String> delinquencyPlanName;
    public PremiumCreditCardProtectionQuoteBuilder delinquencyPlanName(String delinquencyPlanName) { this.delinquencyPlanName = Optional.ofNullable(delinquencyPlanName); return this; }
    public PremiumCreditCardProtectionQuoteBuilder delinquencyPlanName(Optional<String> delinquencyPlanName) { this.delinquencyPlanName = delinquencyPlanName; return this; }
    private Optional<String> autoRenewalPlanName;
    public PremiumCreditCardProtectionQuoteBuilder autoRenewalPlanName(String autoRenewalPlanName) { this.autoRenewalPlanName = Optional.ofNullable(autoRenewalPlanName); return this; }
    public PremiumCreditCardProtectionQuoteBuilder autoRenewalPlanName(Optional<String> autoRenewalPlanName) { this.autoRenewalPlanName = autoRenewalPlanName; return this; }
    private Optional<BillingTrigger> billingTrigger;
    public PremiumCreditCardProtectionQuoteBuilder billingTrigger(BillingTrigger billingTrigger) { this.billingTrigger = Optional.ofNullable(billingTrigger); return this; }
    public PremiumCreditCardProtectionQuoteBuilder billingTrigger(Optional<BillingTrigger> billingTrigger) { this.billingTrigger = billingTrigger; return this; }
    private Optional<String> region;
    public PremiumCreditCardProtectionQuoteBuilder region(Optional<String> region) { this.region = region; return this; }
    public PremiumCreditCardProtectionQuoteBuilder region(String region) { this.region = Optional.ofNullable(region); return this; }
    private FraudProtectionQuote fraudProtection;
    public PremiumCreditCardProtectionQuoteBuilder fraudProtection(FraudProtectionQuote fraudProtection) { this.fraudProtection = fraudProtection; return this; }
    public PremiumCreditCardProtectionQuoteBuilder addFraudProtection(Consumer<FraudProtectionQuote.FraudProtectionQuoteBuilder> mutator) { FraudProtectionQuote.FraudProtectionQuoteBuilder builder = FraudProtectionQuote.builder(this.element.tenantLocator(), this.element.rootLocator(), this.element.locator(), null, null); mutator.accept(builder); this.fraudProtection = builder.build(); return this; }
    private IdentityTheftProtectionQuote identityTheftProtection;
    public PremiumCreditCardProtectionQuoteBuilder identityTheftProtection(IdentityTheftProtectionQuote identityTheftProtection) { this.identityTheftProtection = identityTheftProtection; return this; }
    public PremiumCreditCardProtectionQuoteBuilder addIdentityTheftProtection(Consumer<IdentityTheftProtectionQuote.IdentityTheftProtectionQuoteBuilder> mutator) { IdentityTheftProtectionQuote.IdentityTheftProtectionQuoteBuilder builder = IdentityTheftProtectionQuote.builder(this.element.tenantLocator(), this.element.rootLocator(), this.element.locator(), null, null); mutator.accept(builder); this.identityTheftProtection = builder.build(); return this; }
    private CardReplacementQuote cardReplacement;
    public PremiumCreditCardProtectionQuoteBuilder cardReplacement(CardReplacementQuote cardReplacement) { this.cardReplacement = cardReplacement; return this; }
    public PremiumCreditCardProtectionQuoteBuilder addCardReplacement(Consumer<CardReplacementQuote.CardReplacementQuoteBuilder> mutator) { CardReplacementQuote.CardReplacementQuoteBuilder builder = CardReplacementQuote.builder(this.element.tenantLocator(), this.element.rootLocator(), this.element.locator(), null, null); mutator.accept(builder); this.cardReplacement = builder.build(); return this; }
    private UnauthorizedUseQuote unauthorizedUse;
    public PremiumCreditCardProtectionQuoteBuilder unauthorizedUse(UnauthorizedUseQuote unauthorizedUse) { this.unauthorizedUse = unauthorizedUse; return this; }
    public PremiumCreditCardProtectionQuoteBuilder addUnauthorizedUse(Consumer<UnauthorizedUseQuote.UnauthorizedUseQuoteBuilder> mutator) { UnauthorizedUseQuote.UnauthorizedUseQuoteBuilder builder = UnauthorizedUseQuote.builder(this.element.tenantLocator(), this.element.rootLocator(), this.element.locator(), null, null); mutator.accept(builder); this.unauthorizedUse = builder.build(); return this; }
    private PurchaseProtectionQuote purchaseProtection;
    public PremiumCreditCardProtectionQuoteBuilder purchaseProtection(PurchaseProtectionQuote purchaseProtection) { this.purchaseProtection = purchaseProtection; return this; }
    public PremiumCreditCardProtectionQuoteBuilder addPurchaseProtection(Consumer<PurchaseProtectionQuote.PurchaseProtectionQuoteBuilder> mutator) { PurchaseProtectionQuote.PurchaseProtectionQuoteBuilder builder = PurchaseProtectionQuote.builder(this.element.tenantLocator(), this.element.rootLocator(), this.element.locator(), null, null); mutator.accept(builder); this.purchaseProtection = builder.build(); return this; }
    private TravelEmergencyAssistanceQuote travelEmergencyAssistance;
    public PremiumCreditCardProtectionQuoteBuilder travelEmergencyAssistance(TravelEmergencyAssistanceQuote travelEmergencyAssistance) { this.travelEmergencyAssistance = travelEmergencyAssistance; return this; }
    public PremiumCreditCardProtectionQuoteBuilder addTravelEmergencyAssistance(Consumer<TravelEmergencyAssistanceQuote.TravelEmergencyAssistanceQuoteBuilder> mutator) { TravelEmergencyAssistanceQuote.TravelEmergencyAssistanceQuoteBuilder builder = TravelEmergencyAssistanceQuote.builder(this.element.tenantLocator(), this.element.rootLocator(), this.element.locator(), null, null); mutator.accept(builder); this.travelEmergencyAssistance = builder.build(); return this; }
    private FraudLimit fraudLimit;
    public PremiumCreditCardProtectionQuoteBuilder fraudLimit(FraudLimit fraudLimit) { this.fraudLimit = fraudLimit; return this; }

    private IdentityTheftLimit identityTheftLimit;
    public PremiumCreditCardProtectionQuoteBuilder identityTheftLimit(IdentityTheftLimit identityTheftLimit) { this.identityTheftLimit = identityTheftLimit; return this; }

    private PurchaseProtectionLimit purchaseProtectionLimit;
    public PremiumCreditCardProtectionQuoteBuilder purchaseProtectionLimit(PurchaseProtectionLimit purchaseProtectionLimit) { this.purchaseProtectionLimit = purchaseProtectionLimit; return this; }

    private TravelAssistanceLimit travelAssistanceLimit;
    public PremiumCreditCardProtectionQuoteBuilder travelAssistanceLimit(TravelAssistanceLimit travelAssistanceLimit) { this.travelAssistanceLimit = travelAssistanceLimit; return this; }

    private CardReplacementBenefit cardReplacementBenefit;
    public PremiumCreditCardProtectionQuoteBuilder cardReplacementBenefit(CardReplacementBenefit cardReplacementBenefit) { this.cardReplacementBenefit = cardReplacementBenefit; return this; }

    private PremiumDeductible premiumDeductible;
    public PremiumCreditCardProtectionQuoteBuilder premiumDeductible(PremiumDeductible premiumDeductible) { this.premiumDeductible = premiumDeductible; return this; }

    private PremiumCreditCardProtectionQuoteData data;
    public PremiumCreditCardProtectionQuoteBuilder data(PremiumCreditCardProtectionQuoteData data) { this.data = data; return this; }
    private Element element;
    public PremiumCreditCardProtectionQuoteBuilder element(Element element) { this.element = element; return this; }
    private BillingLevel billingLevel;
    public PremiumCreditCardProtectionQuoteBuilder billingLevel(BillingLevel billingLevel) { this.billingLevel = billingLevel; return this; }
    private Optional<String> quoteNumber;
    public PremiumCreditCardProtectionQuoteBuilder quoteNumber(Optional<String> quoteNumber) { this.quoteNumber = quoteNumber; return this; }
    public PremiumCreditCardProtectionQuoteBuilder quoteNumber(String quoteNumber) { this.quoteNumber = Optional.ofNullable(quoteNumber); return this; }
    public PremiumCreditCardProtectionQuote build() {
      return new PremiumCreditCardProtectionQuote(
          this.locator,
          this.groupLocator,
          this.quoteState,
          this.productName,
          this.accountLocator,
          this.createdAt,
          this.createdBy,
          this.startTime,
          this.endTime,
          this.timezone,
          this.currency,
          this.underwritingStatus,
          this.expirationTime,
          this.preferences,
          this.policyLocator,
          this.durationBasis,
          this.delinquencyPlanName,
          this.autoRenewalPlanName,
          this.billingTrigger,
          this.region,
          this.fraudProtection,
          this.identityTheftProtection,
          this.cardReplacement,
          this.unauthorizedUse,
          this.purchaseProtection,
          this.travelEmergencyAssistance,
          this.fraudLimit,
          this.identityTheftLimit,
          this.purchaseProtectionLimit,
          this.travelAssistanceLimit,
          this.cardReplacementBenefit,
          this.premiumDeductible,
          this.data,
          this.element,
          this.billingLevel,
          this.quoteNumber,
          this.contacts,
          this.invoiceFeeAmount
      );
    }
  }

  public record PremiumCreditCardProtectionQuoteData(BigDecimal averageMonthlySpend, BigDecimal bankRelationshipYears, LocalDate cardIssuedDate, String cardType, BigDecimal creditLimit, String frequentTraveler, String hasPreviousClaims, BigDecimal numberOfCards) implements PremiumCreditCardProtectionData, CustomerObject, Validatable {
    public static final String TYPE = "PremiumCreditCardProtectionQuoteData";
    public static final Set<String> cardTypeOptions = Set.of("Visa Signature","Visa Infinite","Mastercard World","Mastercard World Elite","American Express Gold","American Express Platinum");
    public static final Set<String> frequentTravelerOptions = Set.of("Yes","No");
    public static final Set<String> hasPreviousClaimsOptions = Set.of("None","1-2 claims","3+ claims");

    public PremiumCreditCardProtectionQuoteData {
      if (creditLimit != null) { creditLimit = com.socotra.platform.tools.NumberUtils.trimScale(creditLimit, 2, RoundingMode.HALF_EVEN);}
    if (numberOfCards != null) { numberOfCards = com.socotra.platform.tools.NumberUtils.trimScale(numberOfCards, 8, RoundingMode.HALF_EVEN);}
    if (averageMonthlySpend != null) { averageMonthlySpend = com.socotra.platform.tools.NumberUtils.trimScale(averageMonthlySpend, 2, RoundingMode.HALF_EVEN);}
    if (bankRelationshipYears != null) { bankRelationshipYears = com.socotra.platform.tools.NumberUtils.trimScale(bankRelationshipYears, 8, RoundingMode.HALF_EVEN);}
    }

    public String type() { return TYPE; }

    public PremiumCreditCardProtectionQuoteData maskData(DataMaskingLevel level) {
      PremiumCreditCardProtectionQuoteDataBuilder builder = toBuilder();
      if (level == DataMaskingLevel.level1) {
      }
      if (level == DataMaskingLevel.level2) {
      }
      if (level == DataMaskingLevel.none) {
      }
      return builder.build();
    }

    public PremiumCreditCardProtectionQuoteData anonymizeData() {
      PremiumCreditCardProtectionQuoteDataBuilder builder = toBuilder();
      return builder.build();
    }

    @Override
    public Collection<ValidationItem> validate(DeploymentConfig config, ValidatableContext context) {
      ValidationItem.ValidationItemBuilder validationItemBuilder = ValidationItem.builder().elementType(TYPE);
      if (averageMonthlySpend == null) { validationItemBuilder.addError("Non optional property 'premiumCreditCardProtectionQuoteData.averageMonthlySpend' is missing"); }
      if (averageMonthlySpend != null && averageMonthlySpend.compareTo(new BigDecimal("100")) < 0) { validationItemBuilder.addError("'premiumCreditCardProtectionQuoteData.averageMonthlySpend': " +  averageMonthlySpend + " is less than min of 100 with configured precision of 2 and rounding mode: halfEven"); }
      if (averageMonthlySpend != null && averageMonthlySpend.compareTo(new BigDecimal("25000")) > 0) { validationItemBuilder.addError("'premiumCreditCardProtectionQuoteData.averageMonthlySpend': " +  averageMonthlySpend + " is more than max of 25000 with configured precision of 2 and rounding mode: halfEven"); }
      if (bankRelationshipYears == null) { validationItemBuilder.addError("Non optional property 'premiumCreditCardProtectionQuoteData.bankRelationshipYears' is missing"); }
      if (bankRelationshipYears != null && bankRelationshipYears.compareTo(new BigDecimal("0")) < 0) { validationItemBuilder.addError("'premiumCreditCardProtectionQuoteData.bankRelationshipYears': " +  bankRelationshipYears + " is less than min of 0 with configured precision of 8 and rounding mode: halfEven"); }
      if (bankRelationshipYears != null && bankRelationshipYears.compareTo(new BigDecimal("50")) > 0) { validationItemBuilder.addError("'premiumCreditCardProtectionQuoteData.bankRelationshipYears': " +  bankRelationshipYears + " is more than max of 50 with configured precision of 8 and rounding mode: halfEven"); }
      if (cardIssuedDate == null) { validationItemBuilder.addError("Non optional property 'premiumCreditCardProtectionQuoteData.cardIssuedDate' is missing"); }
      if (cardType == null || cardType.isBlank()) { validationItemBuilder.addError("Non optional property 'premiumCreditCardProtectionQuoteData.cardType' is missing"); }
      if (cardType != null && cardType.length() > 20000) { validationItemBuilder.addError("'premiumCreditCardProtectionQuoteData.cardType' length is more than max length of 20000"); }
      if (cardType != null && !cardTypeOptions.contains(cardType)) { validationItemBuilder.addError("Property 'premiumCreditCardProtectionQuoteData.cardType' should be one of " + cardTypeOptions); }
      if (creditLimit == null) { validationItemBuilder.addError("Non optional property 'premiumCreditCardProtectionQuoteData.creditLimit' is missing"); }
      if (creditLimit != null && creditLimit.compareTo(new BigDecimal("5000")) < 0) { validationItemBuilder.addError("'premiumCreditCardProtectionQuoteData.creditLimit': " +  creditLimit + " is less than min of 5000 with configured precision of 2 and rounding mode: halfEven"); }
      if (creditLimit != null && creditLimit.compareTo(new BigDecimal("100000")) > 0) { validationItemBuilder.addError("'premiumCreditCardProtectionQuoteData.creditLimit': " +  creditLimit + " is more than max of 100000 with configured precision of 2 and rounding mode: halfEven"); }
      if (frequentTraveler == null || frequentTraveler.isBlank()) { validationItemBuilder.addError("Non optional property 'premiumCreditCardProtectionQuoteData.frequentTraveler' is missing"); }
      if (frequentTraveler != null && frequentTraveler.length() > 20000) { validationItemBuilder.addError("'premiumCreditCardProtectionQuoteData.frequentTraveler' length is more than max length of 20000"); }
      if (frequentTraveler != null && !frequentTravelerOptions.contains(frequentTraveler)) { validationItemBuilder.addError("Property 'premiumCreditCardProtectionQuoteData.frequentTraveler' should be one of " + frequentTravelerOptions); }
      if (hasPreviousClaims == null || hasPreviousClaims.isBlank()) { validationItemBuilder.addError("Non optional property 'premiumCreditCardProtectionQuoteData.hasPreviousClaims' is missing"); }
      if (hasPreviousClaims != null && hasPreviousClaims.length() > 20000) { validationItemBuilder.addError("'premiumCreditCardProtectionQuoteData.hasPreviousClaims' length is more than max length of 20000"); }
      if (hasPreviousClaims != null && !hasPreviousClaimsOptions.contains(hasPreviousClaims)) { validationItemBuilder.addError("Property 'premiumCreditCardProtectionQuoteData.hasPreviousClaims' should be one of " + hasPreviousClaimsOptions); }
      if (numberOfCards == null) { validationItemBuilder.addError("Non optional property 'premiumCreditCardProtectionQuoteData.numberOfCards' is missing"); }
      if (numberOfCards != null && numberOfCards.compareTo(new BigDecimal("1")) < 0) { validationItemBuilder.addError("'premiumCreditCardProtectionQuoteData.numberOfCards': " +  numberOfCards + " is less than min of 1 with configured precision of 8 and rounding mode: halfEven"); }
      if (numberOfCards != null && numberOfCards.compareTo(new BigDecimal("10")) > 0) { validationItemBuilder.addError("'premiumCreditCardProtectionQuoteData.numberOfCards': " +  numberOfCards + " is more than max of 10 with configured precision of 8 and rounding mode: halfEven"); }

      return validationItemBuilder.hasErrors() ? List.of(validationItemBuilder.build()) : List.of();
    }

    @Override
    public PremiumCreditCardProtectionQuoteData applyAvailabilityRemovals(Instant referenceDate) {
      PremiumCreditCardProtectionQuoteDataBuilder builder = toBuilder();

      return builder.build();
    }

    public static PremiumCreditCardProtectionQuoteDataBuilder builder() {
      return new PremiumCreditCardProtectionQuoteDataBuilder();
    }
    public PremiumCreditCardProtectionQuoteDataBuilder toBuilder() {
      return new PremiumCreditCardProtectionQuoteDataBuilder()
      .averageMonthlySpend(this.averageMonthlySpend)
      .bankRelationshipYears(this.bankRelationshipYears)
      .cardIssuedDate(this.cardIssuedDate)
      .cardType(this.cardType)
      .creditLimit(this.creditLimit)
      .frequentTraveler(this.frequentTraveler)
      .hasPreviousClaims(this.hasPreviousClaims)
      .numberOfCards(this.numberOfCards);
    }

    public static class PremiumCreditCardProtectionQuoteDataBuilder {
      private BigDecimal averageMonthlySpend;
      public PremiumCreditCardProtectionQuoteDataBuilder averageMonthlySpend(BigDecimal averageMonthlySpend) { this.averageMonthlySpend = averageMonthlySpend; return this; }
      private BigDecimal bankRelationshipYears;
      public PremiumCreditCardProtectionQuoteDataBuilder bankRelationshipYears(BigDecimal bankRelationshipYears) { this.bankRelationshipYears = bankRelationshipYears; return this; }
      private LocalDate cardIssuedDate;
      public PremiumCreditCardProtectionQuoteDataBuilder cardIssuedDate(LocalDate cardIssuedDate) { this.cardIssuedDate = cardIssuedDate; return this; }
      private String cardType;
      public PremiumCreditCardProtectionQuoteDataBuilder cardType(String cardType) { this.cardType = cardType; return this; }
      private BigDecimal creditLimit;
      public PremiumCreditCardProtectionQuoteDataBuilder creditLimit(BigDecimal creditLimit) { this.creditLimit = creditLimit; return this; }
      private String frequentTraveler;
      public PremiumCreditCardProtectionQuoteDataBuilder frequentTraveler(String frequentTraveler) { this.frequentTraveler = frequentTraveler; return this; }
      private String hasPreviousClaims;
      public PremiumCreditCardProtectionQuoteDataBuilder hasPreviousClaims(String hasPreviousClaims) { this.hasPreviousClaims = hasPreviousClaims; return this; }
      private BigDecimal numberOfCards;
      public PremiumCreditCardProtectionQuoteDataBuilder numberOfCards(BigDecimal numberOfCards) { this.numberOfCards = numberOfCards; return this; }

      public PremiumCreditCardProtectionQuoteData build() {
        return new PremiumCreditCardProtectionQuoteData(averageMonthlySpend, bankRelationshipYears, cardIssuedDate, cardType, creditLimit, frequentTraveler, hasPreviousClaims, numberOfCards);
      }
    }
  }

  public static boolean canAccessFields(DataMaskingLevel level, Map<String, Object> fields) {
    return true;
  }
  public static DataMaskingLevel fieldsMaximumLevel(Map<String, Object> fields) {
    DataMaskingLevel maxLevel = DataMaskingLevel.none;
    return maxLevel;
  }
  public static Map<String, Object> anonymizeFields(Map<String, Object> fields) {
    Map<String, Object> result = new HashMap<>();
    for (Map.Entry<String, Object> entry: fields.entrySet()) {
      String keyName = entry.getKey();
      Object fieldValue = entry.getValue();
      result.put(keyName, fieldValue);
    }
    return result;
  }

}