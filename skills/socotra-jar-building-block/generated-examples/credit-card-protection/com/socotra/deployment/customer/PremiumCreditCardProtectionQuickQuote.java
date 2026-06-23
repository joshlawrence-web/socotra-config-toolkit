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

public record PremiumCreditCardProtectionQuickQuote(
    ULID locator,
    QuickQuoteState state,
    String productName,
    Optional<ULID> accountLocator,
    Optional<Instant> startTime,
    Optional<Instant> endTime,
    Optional<String> timezone,
    Optional<String> currency,
    Optional<Instant> expirationTime,
    Optional<Instant> createdAt,
    Optional<UUID> createdBy,
    Optional<DurationBasis> durationBasis,
    FraudProtectionQuickQuote fraudProtection,
    IdentityTheftProtectionQuickQuote identityTheftProtection,
    CardReplacementQuickQuote cardReplacement,
    UnauthorizedUseQuickQuote unauthorizedUse,
    PurchaseProtectionQuickQuote purchaseProtection,
    TravelEmergencyAssistanceQuickQuote travelEmergencyAssistance,
    FraudLimit fraudLimit,
    IdentityTheftLimit identityTheftLimit,
    PurchaseProtectionLimit purchaseProtectionLimit,
    TravelAssistanceLimit travelAssistanceLimit,
    CardReplacementBenefit cardReplacementBenefit,
    PremiumDeductible premiumDeductible,
    PremiumCreditCardProtectionQuickQuoteData data,
    @JsonView({Internal.class}) Element element,
    Collection<ContactRoles> contacts
  ) implements PremiumCreditCardProtection, ContactsHolder, Validatable, com.socotra.coremodel.interfaces.QuickQuote, Elemental, CustomerObjectWithData<PremiumCreditCardProtectionQuickQuote.PremiumCreditCardProtectionQuickQuoteData> {

  public static final String TYPE = "PremiumCreditCardProtectionQuickQuote";
  private static final Map<String, ContactSlot> CONTACT_SLOTS = Map.ofEntries(

  );
  public Map<String, ContactSlot> contactSlots() { return CONTACT_SLOTS; }

  public PremiumCreditCardProtectionQuickQuote {
    
    
    
    
    
    
    if(data == null) {
      data = PremiumCreditCardProtectionQuickQuoteData.builder().build();
    }
      contacts = contacts == null ? List.of() : contacts;
  }

  public String type() { return TYPE; }

  public PremiumCreditCardProtectionQuickQuote maskData(DataMaskingLevel level) {
    PremiumCreditCardProtectionQuickQuoteBuilder builder = toBuilder();
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

  public PremiumCreditCardProtectionQuickQuote anonymizeData() {
    PremiumCreditCardProtectionQuickQuoteBuilder builder = toBuilder();
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
    if (this.endTime.isEmpty()) { validationItemBuilder.addError("endTime is required but missing"); }
    if (this.startTime.isEmpty()) { validationItemBuilder.addError("startTime is required but missing"); }
    if (this.startTime.get().isAfter(this.endTime.get())) { validationItemBuilder.addError("startTime[" + startTime.get() + "] is after endTime[" + endTime.get() + "]"); }
    if (this.accountLocator == null) { validationItemBuilder.addError("Non optional property 'accountLocator' missing"); }
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

  public PremiumCreditCardProtectionQuickQuote applyDefaults(DeploymentConfig config) {
    PremiumCreditCardProtectionQuickQuoteBuilder builder = toBuilder();
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

    { FraudProtectionQuickQuote a = this.fraudProtection; if (this.fraudProtection == null) { a = FraudProtectionQuickQuote.builder(element.tenantLocator(), element.rootLocator(), element.locator(), null, null).build(); }; builder.fraudProtection(a.applyDefaults(config)); }

    { IdentityTheftProtectionQuickQuote a = this.identityTheftProtection; if (this.identityTheftProtection == null) { a = IdentityTheftProtectionQuickQuote.builder(element.tenantLocator(), element.rootLocator(), element.locator(), null, null).build(); }; builder.identityTheftProtection(a.applyDefaults(config)); }

    { CardReplacementQuickQuote a = this.cardReplacement; if (this.cardReplacement == null) { a = CardReplacementQuickQuote.builder(element.tenantLocator(), element.rootLocator(), element.locator(), null, null).build(); }; builder.cardReplacement(a.applyDefaults(config)); }

    { UnauthorizedUseQuickQuote a = this.unauthorizedUse; if (this.unauthorizedUse == null) { a = UnauthorizedUseQuickQuote.builder(element.tenantLocator(), element.rootLocator(), element.locator(), null, null).build(); }; builder.unauthorizedUse(a.applyDefaults(config)); }

    { PurchaseProtectionQuickQuote a = this.purchaseProtection; if (this.purchaseProtection == null) { a = PurchaseProtectionQuickQuote.builder(element.tenantLocator(), element.rootLocator(), element.locator(), null, null).build(); }; builder.purchaseProtection(a.applyDefaults(config)); }

    { TravelEmergencyAssistanceQuickQuote a = this.travelEmergencyAssistance; if (this.travelEmergencyAssistance == null) { a = TravelEmergencyAssistanceQuickQuote.builder(element.tenantLocator(), element.rootLocator(), element.locator(), null, null).build(); }; builder.travelEmergencyAssistance(a.applyDefaults(config)); }

    if (this.fraudLimit == null) { builder.fraudLimit(com.socotra.deployment.customer.FraudLimit.defaultOption()); }
    if (this.identityTheftLimit == null) { builder.identityTheftLimit(com.socotra.deployment.customer.IdentityTheftLimit.defaultOption()); }
    if (this.purchaseProtectionLimit == null) { builder.purchaseProtectionLimit(com.socotra.deployment.customer.PurchaseProtectionLimit.defaultOption()); }
    if (this.travelAssistanceLimit == null) { builder.travelAssistanceLimit(com.socotra.deployment.customer.TravelAssistanceLimit.defaultOption()); }
    if (this.cardReplacementBenefit == null) { builder.cardReplacementBenefit(com.socotra.deployment.customer.CardReplacementBenefit.defaultOption()); }
    if (this.premiumDeductible == null) { builder.premiumDeductible(com.socotra.deployment.customer.PremiumDeductible.defaultOption()); }
    return builder.build();
  }

  public PremiumCreditCardProtectionQuickQuote applyAvailabilityRemovals(Instant referenceDate) {
    PremiumCreditCardProtectionQuickQuoteBuilder builder = toBuilder();
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

  public static PremiumCreditCardProtectionQuickQuote from(DeploymentFactory factory, com.socotra.coremodel.interfaces.QuickQuote quote) {
    if (quote == null) {
      return null;
    }
    Element element = quote.element();
    return new PremiumCreditCardProtectionQuickQuote(
        quote.locator(),
        quote.state(),
        quote.productName(),
        quote.accountLocator(),
        quote.startTime(),
        quote.endTime(),
        quote.timezone(),
        quote.currency(),
        quote.expirationTime(),
        quote.createdAt(),
        quote.createdBy(),
        quote.durationBasis(),
        FraudProtectionQuickQuote.fromElement(factory, element.elements().stream().filter(e -> e.type().equalsIgnoreCase(FraudProtectionQuickQuote.TYPE)).findAny().orElse(null)),
        IdentityTheftProtectionQuickQuote.fromElement(factory, element.elements().stream().filter(e -> e.type().equalsIgnoreCase(IdentityTheftProtectionQuickQuote.TYPE)).findAny().orElse(null)),
        CardReplacementQuickQuote.fromElement(factory, element.elements().stream().filter(e -> e.type().equalsIgnoreCase(CardReplacementQuickQuote.TYPE)).findAny().orElse(null)),
        UnauthorizedUseQuickQuote.fromElement(factory, element.elements().stream().filter(e -> e.type().equalsIgnoreCase(UnauthorizedUseQuickQuote.TYPE)).findAny().orElse(null)),
        PurchaseProtectionQuickQuote.fromElement(factory, element.elements().stream().filter(e -> e.type().equalsIgnoreCase(PurchaseProtectionQuickQuote.TYPE)).findAny().orElse(null)),
        TravelEmergencyAssistanceQuickQuote.fromElement(factory, element.elements().stream().filter(e -> e.type().equalsIgnoreCase(TravelEmergencyAssistanceQuickQuote.TYPE)).findAny().orElse(null)),
        com.socotra.deployment.customer.FraudLimit.fromMap(element.coverageTerms()),
        com.socotra.deployment.customer.IdentityTheftLimit.fromMap(element.coverageTerms()),
        com.socotra.deployment.customer.PurchaseProtectionLimit.fromMap(element.coverageTerms()),
        com.socotra.deployment.customer.TravelAssistanceLimit.fromMap(element.coverageTerms()),
        com.socotra.deployment.customer.CardReplacementBenefit.fromMap(element.coverageTerms()),
        com.socotra.deployment.customer.PremiumDeductible.fromMap(element.coverageTerms()),
        element.resolve(PremiumCreditCardProtectionQuickQuoteData.TYPE, factory),
        element,
        quote.contacts()
    );
  }

  public static PremiumCreditCardProtectionQuickQuoteBuilder builder() {
    return new PremiumCreditCardProtectionQuickQuoteBuilder(QuickQuoteState.draft, Optional.empty(), Optional.empty());
  }

  public PremiumCreditCardProtectionQuickQuoteBuilder toBuilder() {
    return new PremiumCreditCardProtectionQuickQuoteBuilder(this.state, this.createdAt, this.createdBy)
        .locator(this.locator)
        .productName(this.productName)
        .accountLocator(this.accountLocator)
        .startTime(this.startTime)
        .endTime(this.endTime)
        .timezone(this.timezone)
        .currency(this.currency)
        .expirationTime(this.expirationTime)
        .durationBasis(this.durationBasis)
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
        .contacts(this.contacts);
  }

  public static class PremiumCreditCardProtectionQuickQuoteBuilder {
    private final QuickQuoteState state;
    private Optional<Instant> createdAt;
    private Optional<UUID> createdBy;
    private PremiumCreditCardProtectionQuickQuoteBuilder(QuickQuoteState state,Optional<Instant> createdAt, Optional<UUID> createdBy) {
      this.state = state;
    }
    private Collection<ContactRoles> contacts;
    public PremiumCreditCardProtectionQuickQuoteBuilder contacts(Collection<ContactRoles> contacts) { this.contacts = contacts; return this; }
    public PremiumCreditCardProtectionQuickQuoteBuilder addContact(ContactRoles contact) { if (!(this.contacts instanceof ArrayList)) { this.contacts = new ArrayList<>(this.contacts); }; this.contacts.add(contact); return this; }
    private ULID locator;
    public PremiumCreditCardProtectionQuickQuoteBuilder locator(ULID locator) { this.locator = locator; return this; }
    private String productName;
    public PremiumCreditCardProtectionQuickQuoteBuilder productName(String productName) { this.productName = productName; return this; }
    private Optional<ULID> accountLocator;
    public PremiumCreditCardProtectionQuickQuoteBuilder accountLocator(ULID accountLocator) { this.accountLocator = Optional.ofNullable(accountLocator); return this; }
    public PremiumCreditCardProtectionQuickQuoteBuilder accountLocator(Optional<ULID> accountLocator) { this.accountLocator = accountLocator; return this; }
    private Optional<Instant> startTime;
    public PremiumCreditCardProtectionQuickQuoteBuilder startTime(Instant startTime) { this.startTime = Optional.ofNullable(startTime); return this; }
    public PremiumCreditCardProtectionQuickQuoteBuilder startTime(Optional<Instant> startTime) { this.startTime = startTime; return this; }
    private Optional<Instant> endTime;
    public PremiumCreditCardProtectionQuickQuoteBuilder endTime(Instant endTime) { this.endTime = Optional.ofNullable(endTime); return this; }
    public PremiumCreditCardProtectionQuickQuoteBuilder endTime(Optional<Instant> endTime) { this.endTime = endTime; return this; }
    private Optional<String> timezone;
    public PremiumCreditCardProtectionQuickQuoteBuilder timezone(String timezone) { this.timezone = Optional.ofNullable(timezone); return this; }
    public PremiumCreditCardProtectionQuickQuoteBuilder timezone(Optional<String> timezone) { this.timezone = timezone; return this; }
    private Optional<String> currency;
    public PremiumCreditCardProtectionQuickQuoteBuilder currency(String currency) { this.currency = Optional.ofNullable(currency); return this; }
    public PremiumCreditCardProtectionQuickQuoteBuilder currency(Optional<String> currency) { this.currency = currency; return this; }
    private Optional<Instant> expirationTime;
    public PremiumCreditCardProtectionQuickQuoteBuilder expirationTime(Instant expirationTime) { this.expirationTime = Optional.ofNullable(expirationTime); return this; }
    public PremiumCreditCardProtectionQuickQuoteBuilder expirationTime(Optional<Instant> expirationTime) { this.expirationTime = expirationTime; return this; }
    private Optional<DurationBasis> durationBasis;
    public PremiumCreditCardProtectionQuickQuoteBuilder durationBasis(DurationBasis durationBasis) { this.durationBasis = Optional.ofNullable(durationBasis); return this; }
    public PremiumCreditCardProtectionQuickQuoteBuilder durationBasis(Optional<DurationBasis> durationBasis) { this.durationBasis = durationBasis; return this; }
    private FraudProtectionQuickQuote fraudProtection;
    public PremiumCreditCardProtectionQuickQuoteBuilder fraudProtection(FraudProtectionQuickQuote fraudProtection) { this.fraudProtection = fraudProtection; return this; }

    public PremiumCreditCardProtectionQuickQuoteBuilder addFraudProtection(Consumer<FraudProtectionQuickQuote.FraudProtectionQuickQuoteBuilder> mutator) { FraudProtectionQuickQuote.FraudProtectionQuickQuoteBuilder builder = FraudProtectionQuickQuote.builder(this.element.tenantLocator(), this.element.rootLocator(), this.element.locator(), null, null); mutator.accept(builder); this.fraudProtection = builder.build(); return this; }
    private IdentityTheftProtectionQuickQuote identityTheftProtection;
    public PremiumCreditCardProtectionQuickQuoteBuilder identityTheftProtection(IdentityTheftProtectionQuickQuote identityTheftProtection) { this.identityTheftProtection = identityTheftProtection; return this; }

    public PremiumCreditCardProtectionQuickQuoteBuilder addIdentityTheftProtection(Consumer<IdentityTheftProtectionQuickQuote.IdentityTheftProtectionQuickQuoteBuilder> mutator) { IdentityTheftProtectionQuickQuote.IdentityTheftProtectionQuickQuoteBuilder builder = IdentityTheftProtectionQuickQuote.builder(this.element.tenantLocator(), this.element.rootLocator(), this.element.locator(), null, null); mutator.accept(builder); this.identityTheftProtection = builder.build(); return this; }
    private CardReplacementQuickQuote cardReplacement;
    public PremiumCreditCardProtectionQuickQuoteBuilder cardReplacement(CardReplacementQuickQuote cardReplacement) { this.cardReplacement = cardReplacement; return this; }

    public PremiumCreditCardProtectionQuickQuoteBuilder addCardReplacement(Consumer<CardReplacementQuickQuote.CardReplacementQuickQuoteBuilder> mutator) { CardReplacementQuickQuote.CardReplacementQuickQuoteBuilder builder = CardReplacementQuickQuote.builder(this.element.tenantLocator(), this.element.rootLocator(), this.element.locator(), null, null); mutator.accept(builder); this.cardReplacement = builder.build(); return this; }
    private UnauthorizedUseQuickQuote unauthorizedUse;
    public PremiumCreditCardProtectionQuickQuoteBuilder unauthorizedUse(UnauthorizedUseQuickQuote unauthorizedUse) { this.unauthorizedUse = unauthorizedUse; return this; }

    public PremiumCreditCardProtectionQuickQuoteBuilder addUnauthorizedUse(Consumer<UnauthorizedUseQuickQuote.UnauthorizedUseQuickQuoteBuilder> mutator) { UnauthorizedUseQuickQuote.UnauthorizedUseQuickQuoteBuilder builder = UnauthorizedUseQuickQuote.builder(this.element.tenantLocator(), this.element.rootLocator(), this.element.locator(), null, null); mutator.accept(builder); this.unauthorizedUse = builder.build(); return this; }
    private PurchaseProtectionQuickQuote purchaseProtection;
    public PremiumCreditCardProtectionQuickQuoteBuilder purchaseProtection(PurchaseProtectionQuickQuote purchaseProtection) { this.purchaseProtection = purchaseProtection; return this; }

    public PremiumCreditCardProtectionQuickQuoteBuilder addPurchaseProtection(Consumer<PurchaseProtectionQuickQuote.PurchaseProtectionQuickQuoteBuilder> mutator) { PurchaseProtectionQuickQuote.PurchaseProtectionQuickQuoteBuilder builder = PurchaseProtectionQuickQuote.builder(this.element.tenantLocator(), this.element.rootLocator(), this.element.locator(), null, null); mutator.accept(builder); this.purchaseProtection = builder.build(); return this; }
    private TravelEmergencyAssistanceQuickQuote travelEmergencyAssistance;
    public PremiumCreditCardProtectionQuickQuoteBuilder travelEmergencyAssistance(TravelEmergencyAssistanceQuickQuote travelEmergencyAssistance) { this.travelEmergencyAssistance = travelEmergencyAssistance; return this; }

    public PremiumCreditCardProtectionQuickQuoteBuilder addTravelEmergencyAssistance(Consumer<TravelEmergencyAssistanceQuickQuote.TravelEmergencyAssistanceQuickQuoteBuilder> mutator) { TravelEmergencyAssistanceQuickQuote.TravelEmergencyAssistanceQuickQuoteBuilder builder = TravelEmergencyAssistanceQuickQuote.builder(this.element.tenantLocator(), this.element.rootLocator(), this.element.locator(), null, null); mutator.accept(builder); this.travelEmergencyAssistance = builder.build(); return this; }
    private FraudLimit fraudLimit;
    public PremiumCreditCardProtectionQuickQuoteBuilder fraudLimit(FraudLimit fraudLimit) { this.fraudLimit = fraudLimit; return this; }

    private IdentityTheftLimit identityTheftLimit;
    public PremiumCreditCardProtectionQuickQuoteBuilder identityTheftLimit(IdentityTheftLimit identityTheftLimit) { this.identityTheftLimit = identityTheftLimit; return this; }

    private PurchaseProtectionLimit purchaseProtectionLimit;
    public PremiumCreditCardProtectionQuickQuoteBuilder purchaseProtectionLimit(PurchaseProtectionLimit purchaseProtectionLimit) { this.purchaseProtectionLimit = purchaseProtectionLimit; return this; }

    private TravelAssistanceLimit travelAssistanceLimit;
    public PremiumCreditCardProtectionQuickQuoteBuilder travelAssistanceLimit(TravelAssistanceLimit travelAssistanceLimit) { this.travelAssistanceLimit = travelAssistanceLimit; return this; }

    private CardReplacementBenefit cardReplacementBenefit;
    public PremiumCreditCardProtectionQuickQuoteBuilder cardReplacementBenefit(CardReplacementBenefit cardReplacementBenefit) { this.cardReplacementBenefit = cardReplacementBenefit; return this; }

    private PremiumDeductible premiumDeductible;
    public PremiumCreditCardProtectionQuickQuoteBuilder premiumDeductible(PremiumDeductible premiumDeductible) { this.premiumDeductible = premiumDeductible; return this; }

    private PremiumCreditCardProtectionQuickQuoteData data;
    public PremiumCreditCardProtectionQuickQuoteBuilder data(PremiumCreditCardProtectionQuickQuoteData data) { this.data = data; return this; }
    private Element element;
    public PremiumCreditCardProtectionQuickQuoteBuilder element(Element element) { this.element = element; return this; }
    public PremiumCreditCardProtectionQuickQuote build() {
      return new PremiumCreditCardProtectionQuickQuote(
          this.locator,
          this.state,
          this.productName,
          this.accountLocator,
          this.startTime,
          this.endTime,
          this.timezone,
          this.currency,
          this.expirationTime,
          this.createdAt,
          this.createdBy,
          this.durationBasis,
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
          this.contacts
      );
    }
  }

  public record PremiumCreditCardProtectionQuickQuoteData(BigDecimal averageMonthlySpend, BigDecimal bankRelationshipYears, LocalDate cardIssuedDate, String cardType, BigDecimal creditLimit, String frequentTraveler, String hasPreviousClaims, BigDecimal numberOfCards) implements PremiumCreditCardProtectionData, CustomerObject, Validatable {
    public static final String TYPE = "PremiumCreditCardProtectionQuickQuoteData";
    public static final Set<String> cardTypeOptions = Set.of("Visa Signature","Visa Infinite","Mastercard World","Mastercard World Elite","American Express Gold","American Express Platinum");
    public static final Set<String> frequentTravelerOptions = Set.of("Yes","No");
    public static final Set<String> hasPreviousClaimsOptions = Set.of("None","1-2 claims","3+ claims");

    public PremiumCreditCardProtectionQuickQuoteData {
      if (creditLimit != null) { creditLimit = com.socotra.platform.tools.NumberUtils.trimScale(creditLimit, 2, RoundingMode.HALF_EVEN);}
    if (numberOfCards != null) { numberOfCards = com.socotra.platform.tools.NumberUtils.trimScale(numberOfCards, 8, RoundingMode.HALF_EVEN);}
    if (averageMonthlySpend != null) { averageMonthlySpend = com.socotra.platform.tools.NumberUtils.trimScale(averageMonthlySpend, 2, RoundingMode.HALF_EVEN);}
    if (bankRelationshipYears != null) { bankRelationshipYears = com.socotra.platform.tools.NumberUtils.trimScale(bankRelationshipYears, 8, RoundingMode.HALF_EVEN);}
    }

    public String type() { return TYPE; }

    public PremiumCreditCardProtectionQuickQuoteData maskData(DataMaskingLevel level) {
      PremiumCreditCardProtectionQuickQuoteDataBuilder builder = toBuilder();
      if (level == DataMaskingLevel.level1) {
      }
      if (level == DataMaskingLevel.level2) {
      }
      if (level == DataMaskingLevel.none) {
      }
      return builder.build();
    }

    public PremiumCreditCardProtectionQuickQuoteData anonymizeData() {
      PremiumCreditCardProtectionQuickQuoteDataBuilder builder = toBuilder();
      return builder.build();
    }

    @Override
    public Collection<ValidationItem> validate(DeploymentConfig config, ValidatableContext context) {
      ValidationItem.ValidationItemBuilder validationItemBuilder = ValidationItem.builder().elementType(TYPE);
      if (averageMonthlySpend == null) { validationItemBuilder.addError("Non optional property 'premiumCreditCardProtectionQuickQuoteData.averageMonthlySpend' is missing"); }
      if (averageMonthlySpend != null && averageMonthlySpend.compareTo(new BigDecimal("100")) < 0) { validationItemBuilder.addError("'premiumCreditCardProtectionQuickQuoteData.averageMonthlySpend': " +  averageMonthlySpend + " is less than min of 100 with configured precision of 2 and rounding mode: halfEven"); }
      if (averageMonthlySpend != null && averageMonthlySpend.compareTo(new BigDecimal("25000")) > 0) { validationItemBuilder.addError("'premiumCreditCardProtectionQuickQuoteData.averageMonthlySpend': " +  averageMonthlySpend + " is more than max of 25000 with configured precision of 2 and rounding mode: halfEven"); }
      if (bankRelationshipYears == null) { validationItemBuilder.addError("Non optional property 'premiumCreditCardProtectionQuickQuoteData.bankRelationshipYears' is missing"); }
      if (bankRelationshipYears != null && bankRelationshipYears.compareTo(new BigDecimal("0")) < 0) { validationItemBuilder.addError("'premiumCreditCardProtectionQuickQuoteData.bankRelationshipYears': " +  bankRelationshipYears + " is less than min of 0 with configured precision of 8 and rounding mode: halfEven"); }
      if (bankRelationshipYears != null && bankRelationshipYears.compareTo(new BigDecimal("50")) > 0) { validationItemBuilder.addError("'premiumCreditCardProtectionQuickQuoteData.bankRelationshipYears': " +  bankRelationshipYears + " is more than max of 50 with configured precision of 8 and rounding mode: halfEven"); }
      if (cardIssuedDate == null) { validationItemBuilder.addError("Non optional property 'premiumCreditCardProtectionQuickQuoteData.cardIssuedDate' is missing"); }
      if (cardType == null || cardType.isBlank()) { validationItemBuilder.addError("Non optional property 'premiumCreditCardProtectionQuickQuoteData.cardType' is missing"); }
      if (cardType != null && cardType.length() > 20000) { validationItemBuilder.addError("'premiumCreditCardProtectionQuickQuoteData.cardType' length is more than max length of 20000"); }
      if (cardType != null && !cardTypeOptions.contains(cardType)) { validationItemBuilder.addError("Property 'premiumCreditCardProtectionQuickQuoteData.cardType' should be one of " + cardTypeOptions); }
      if (creditLimit == null) { validationItemBuilder.addError("Non optional property 'premiumCreditCardProtectionQuickQuoteData.creditLimit' is missing"); }
      if (creditLimit != null && creditLimit.compareTo(new BigDecimal("5000")) < 0) { validationItemBuilder.addError("'premiumCreditCardProtectionQuickQuoteData.creditLimit': " +  creditLimit + " is less than min of 5000 with configured precision of 2 and rounding mode: halfEven"); }
      if (creditLimit != null && creditLimit.compareTo(new BigDecimal("100000")) > 0) { validationItemBuilder.addError("'premiumCreditCardProtectionQuickQuoteData.creditLimit': " +  creditLimit + " is more than max of 100000 with configured precision of 2 and rounding mode: halfEven"); }
      if (frequentTraveler == null || frequentTraveler.isBlank()) { validationItemBuilder.addError("Non optional property 'premiumCreditCardProtectionQuickQuoteData.frequentTraveler' is missing"); }
      if (frequentTraveler != null && frequentTraveler.length() > 20000) { validationItemBuilder.addError("'premiumCreditCardProtectionQuickQuoteData.frequentTraveler' length is more than max length of 20000"); }
      if (frequentTraveler != null && !frequentTravelerOptions.contains(frequentTraveler)) { validationItemBuilder.addError("Property 'premiumCreditCardProtectionQuickQuoteData.frequentTraveler' should be one of " + frequentTravelerOptions); }
      if (hasPreviousClaims == null || hasPreviousClaims.isBlank()) { validationItemBuilder.addError("Non optional property 'premiumCreditCardProtectionQuickQuoteData.hasPreviousClaims' is missing"); }
      if (hasPreviousClaims != null && hasPreviousClaims.length() > 20000) { validationItemBuilder.addError("'premiumCreditCardProtectionQuickQuoteData.hasPreviousClaims' length is more than max length of 20000"); }
      if (hasPreviousClaims != null && !hasPreviousClaimsOptions.contains(hasPreviousClaims)) { validationItemBuilder.addError("Property 'premiumCreditCardProtectionQuickQuoteData.hasPreviousClaims' should be one of " + hasPreviousClaimsOptions); }
      if (numberOfCards == null) { validationItemBuilder.addError("Non optional property 'premiumCreditCardProtectionQuickQuoteData.numberOfCards' is missing"); }
      if (numberOfCards != null && numberOfCards.compareTo(new BigDecimal("1")) < 0) { validationItemBuilder.addError("'premiumCreditCardProtectionQuickQuoteData.numberOfCards': " +  numberOfCards + " is less than min of 1 with configured precision of 8 and rounding mode: halfEven"); }
      if (numberOfCards != null && numberOfCards.compareTo(new BigDecimal("10")) > 0) { validationItemBuilder.addError("'premiumCreditCardProtectionQuickQuoteData.numberOfCards': " +  numberOfCards + " is more than max of 10 with configured precision of 8 and rounding mode: halfEven"); }

      return validationItemBuilder.hasErrors() ? List.of(validationItemBuilder.build()) : List.of();
    }

    @Override
    public PremiumCreditCardProtectionQuickQuoteData applyAvailabilityRemovals(Instant referenceDate) {
      PremiumCreditCardProtectionQuickQuoteDataBuilder builder = toBuilder();

      return builder.build();
    }

    public static PremiumCreditCardProtectionQuickQuoteDataBuilder builder() {
      return new PremiumCreditCardProtectionQuickQuoteDataBuilder();
    }
    public PremiumCreditCardProtectionQuickQuoteDataBuilder toBuilder() {
      return new PremiumCreditCardProtectionQuickQuoteDataBuilder()
      .averageMonthlySpend(this.averageMonthlySpend)
      .bankRelationshipYears(this.bankRelationshipYears)
      .cardIssuedDate(this.cardIssuedDate)
      .cardType(this.cardType)
      .creditLimit(this.creditLimit)
      .frequentTraveler(this.frequentTraveler)
      .hasPreviousClaims(this.hasPreviousClaims)
      .numberOfCards(this.numberOfCards);
    }

    public static class PremiumCreditCardProtectionQuickQuoteDataBuilder {
      private BigDecimal averageMonthlySpend;
      public PremiumCreditCardProtectionQuickQuoteDataBuilder averageMonthlySpend(BigDecimal averageMonthlySpend) { this.averageMonthlySpend = averageMonthlySpend; return this; }
      private BigDecimal bankRelationshipYears;
      public PremiumCreditCardProtectionQuickQuoteDataBuilder bankRelationshipYears(BigDecimal bankRelationshipYears) { this.bankRelationshipYears = bankRelationshipYears; return this; }
      private LocalDate cardIssuedDate;
      public PremiumCreditCardProtectionQuickQuoteDataBuilder cardIssuedDate(LocalDate cardIssuedDate) { this.cardIssuedDate = cardIssuedDate; return this; }
      private String cardType;
      public PremiumCreditCardProtectionQuickQuoteDataBuilder cardType(String cardType) { this.cardType = cardType; return this; }
      private BigDecimal creditLimit;
      public PremiumCreditCardProtectionQuickQuoteDataBuilder creditLimit(BigDecimal creditLimit) { this.creditLimit = creditLimit; return this; }
      private String frequentTraveler;
      public PremiumCreditCardProtectionQuickQuoteDataBuilder frequentTraveler(String frequentTraveler) { this.frequentTraveler = frequentTraveler; return this; }
      private String hasPreviousClaims;
      public PremiumCreditCardProtectionQuickQuoteDataBuilder hasPreviousClaims(String hasPreviousClaims) { this.hasPreviousClaims = hasPreviousClaims; return this; }
      private BigDecimal numberOfCards;
      public PremiumCreditCardProtectionQuickQuoteDataBuilder numberOfCards(BigDecimal numberOfCards) { this.numberOfCards = numberOfCards; return this; }

      public PremiumCreditCardProtectionQuickQuoteData build() {
        return new PremiumCreditCardProtectionQuickQuoteData(averageMonthlySpend, bankRelationshipYears, cardIssuedDate, cardType, creditLimit, frequentTraveler, hasPreviousClaims, numberOfCards);
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