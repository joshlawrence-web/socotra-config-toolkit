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

public record BasicCreditCardProtectionQuote(
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
    CardReplacementQuote cardReplacement,
    UnauthorizedUseQuote unauthorizedUse,
    FraudLimit fraudLimit,
    CardReplacementBenefit cardReplacementBenefit,
    BasicDeductible basicDeductible,
    BasicCreditCardProtectionQuoteData data,
    @JsonView({Internal.class}) Element element,
    BillingLevel billingLevel,
    Optional<String> quoteNumber,
    Collection<ContactRoles> contacts,
    Optional<BigDecimal> invoiceFeeAmount) implements BasicCreditCardProtection, Validatable, com.socotra.coremodel.interfaces.Quote, Elemental, CustomerObjectWithData<BasicCreditCardProtectionQuote.BasicCreditCardProtectionQuoteData>, CustomerObject, ContactsHolder, NumberingTriggerHolder {

  public static final String TYPE = "BasicCreditCardProtectionQuote";
  private static final Map<String, ContactSlot> CONTACT_SLOTS = Map.ofEntries(

  );

  public Map<String, ContactSlot> contactSlots() { return CONTACT_SLOTS; }

  public NumberingTrigger numberingTrigger() {
    return NumberingTrigger.validation;
  }

  public BasicCreditCardProtectionQuote {
    
    
    
    if(data == null) {
      data = BasicCreditCardProtectionQuoteData.builder().build();
    }
    contacts = contacts == null ? List.of() : contacts;
    invoiceFeeAmount = invoiceFeeAmount == null ? Optional.empty() : invoiceFeeAmount;
  }

  public String type() { return TYPE; }

  public BasicCreditCardProtectionQuote maskData(DataMaskingLevel level) {
    BasicCreditCardProtectionQuoteBuilder builder = toBuilder();
    builder.data(this.data.maskData(level));
    if (this.fraudProtection != null) {
      builder.fraudProtection(fraudProtection.maskData(level));
    }
    if (this.cardReplacement != null) {
      builder.cardReplacement(cardReplacement.maskData(level));
    }
    if (this.unauthorizedUse != null) {
      builder.unauthorizedUse(unauthorizedUse.maskData(level));
    }
    return builder.build();
  }

  public BasicCreditCardProtectionQuote anonymizeData() {
    BasicCreditCardProtectionQuoteBuilder builder = toBuilder();
    builder.data(this.data.anonymizeData());
    if (this.fraudProtection != null) {
      builder.fraudProtection(fraudProtection.anonymizeData());
    }
    if (this.cardReplacement != null) {
      builder.cardReplacement(cardReplacement.anonymizeData());
    }
    if (this.unauthorizedUse != null) {
      builder.unauthorizedUse(unauthorizedUse.anonymizeData());
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

    if (this.fraudLimit == null) {
      validationItemBuilder.addError("'fraudLimit' is required");
    } else {
      validationItems.addAll(this.fraudLimit.validate(config, context));
    }

    if (this.cardReplacementBenefit == null) {
      validationItemBuilder.addError("'cardReplacementBenefit' is required");
    } else {
      validationItems.addAll(this.cardReplacementBenefit.validate(config, context));
    }

    if (this.basicDeductible == null) {
      validationItemBuilder.addError("'basicDeductible' is required");
    } else {
      validationItems.addAll(this.basicDeductible.validate(config, context));
    }

    this.data.validate(config, context).forEach(error -> error.errors().forEach(validationItemBuilder::addError));

    validationItemBuilder.addErrors(validateContacts());
    if (validationItemBuilder.hasErrors()) {
      validationItems.add(validationItemBuilder.build());
    }
    return validationItems;
  }

  public BasicCreditCardProtectionQuote applyDefaults(DeploymentConfig config) {
    BasicCreditCardProtectionQuoteBuilder builder = toBuilder();
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

    { CardReplacementQuote a = this.cardReplacement; if (this.cardReplacement == null) { a = CardReplacementQuote.builder(element.tenantLocator(), element.rootLocator(), element.locator(), null, null).build(); }; builder.cardReplacement(a.applyDefaults(config)); }

    { UnauthorizedUseQuote a = this.unauthorizedUse; if (this.unauthorizedUse == null) { a = UnauthorizedUseQuote.builder(element.tenantLocator(), element.rootLocator(), element.locator(), null, null).build(); }; builder.unauthorizedUse(a.applyDefaults(config)); }

    if (this.fraudLimit == null) { builder.fraudLimit(com.socotra.deployment.customer.FraudLimit.defaultOption()); }
    if (this.cardReplacementBenefit == null) { builder.cardReplacementBenefit(com.socotra.deployment.customer.CardReplacementBenefit.defaultOption()); }
    if (this.basicDeductible == null) { builder.basicDeductible(com.socotra.deployment.customer.BasicDeductible.defaultOption()); }
    return builder.build();
  }

  public BasicCreditCardProtectionQuote applyAvailabilityRemovals(Instant referenceDate) {
    BasicCreditCardProtectionQuoteBuilder builder = toBuilder();
    if (this.fraudProtection != null) {
      builder.fraudProtection(fraudProtection.applyAvailabilityRemovals(referenceDate));
    }

    if (this.cardReplacement != null) {
      builder.cardReplacement(cardReplacement.applyAvailabilityRemovals(referenceDate));
    }

    if (this.unauthorizedUse != null) {
      builder.unauthorizedUse(unauthorizedUse.applyAvailabilityRemovals(referenceDate));
    }

    if (this.fraudLimit != null) {
      builder.fraudLimit(fraudLimit.applyAvailabilityRemovals(referenceDate));
    }

    if (this.cardReplacementBenefit != null) {
      builder.cardReplacementBenefit(cardReplacementBenefit.applyAvailabilityRemovals(referenceDate));
    }

    if (this.basicDeductible != null) {
      builder.basicDeductible(basicDeductible.applyAvailabilityRemovals(referenceDate));
    }

    builder.data(this.data.applyAvailabilityRemovals(referenceDate));
    return builder.build();
  }

  public Element toElement(DeploymentFactory factory) {
    Map<String, Object> ct = new HashMap<>();
    ct.put("FraudLimit", this.fraudLimit == null ? null : (Enum.class.isAssignableFrom(this.fraudLimit.getClass()) ? this.fraudLimit.toString() : this.fraudLimit));
    ct.put("CardReplacementBenefit", this.cardReplacementBenefit == null ? null : (Enum.class.isAssignableFrom(this.cardReplacementBenefit.getClass()) ? this.cardReplacementBenefit.toString() : this.cardReplacementBenefit));
    ct.put("BasicDeductible", this.basicDeductible == null ? null : (Enum.class.isAssignableFrom(this.basicDeductible.getClass()) ? this.basicDeductible.toString() : this.basicDeductible));
    ct = ct.entrySet().stream().filter(e -> e.getValue() != null).collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    Collection<Element> subElements = new ArrayList<>();
    if (this.fraudProtection != null) {
      subElements.add(this.fraudProtection.toElement(factory));
    }
    if (this.cardReplacement != null) {
      subElements.add(this.cardReplacement.toElement(factory));
    }
    if (this.unauthorizedUse != null) {
      subElements.add(this.unauthorizedUse.toElement(factory));
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

  public static BasicCreditCardProtectionQuote from(DeploymentFactory factory, com.socotra.coremodel.interfaces.Quote quote) {
    if (quote == null) {
      return null;
    }
    Element element = quote.element();
    return new BasicCreditCardProtectionQuote(
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
        CardReplacementQuote.fromElement(factory, element.elements().stream().filter(e -> e.type().equalsIgnoreCase(CardReplacementQuote.TYPE)).findAny().orElse(null)),
        UnauthorizedUseQuote.fromElement(factory, element.elements().stream().filter(e -> e.type().equalsIgnoreCase(UnauthorizedUseQuote.TYPE)).findAny().orElse(null)),
        com.socotra.deployment.customer.FraudLimit.fromMap(element.coverageTerms()),
        com.socotra.deployment.customer.CardReplacementBenefit.fromMap(element.coverageTerms()),
        com.socotra.deployment.customer.BasicDeductible.fromMap(element.coverageTerms()),
        element.resolve(BasicCreditCardProtectionQuoteData.TYPE, factory),
        element,
        quote.billingLevel(),
        quote.quoteNumber(),
        quote.contacts(),
        quote.invoiceFeeAmount()
    );
  }

  public static BasicCreditCardProtectionQuoteBuilder builder() {
    return new BasicCreditCardProtectionQuoteBuilder(QuoteState.draft, Optional.empty(), Optional.empty());
  }

  public BasicCreditCardProtectionQuoteBuilder toBuilder() {
    return new BasicCreditCardProtectionQuoteBuilder(this.quoteState, this.createdAt, this.createdBy)
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
.cardReplacement(this.cardReplacement)
.unauthorizedUse(this.unauthorizedUse)

        .fraudLimit(this.fraudLimit)
.cardReplacementBenefit(this.cardReplacementBenefit)
.basicDeductible(this.basicDeductible)

        .data(this.data)
        .element(this.element)
        .billingLevel(this.billingLevel)
        .quoteNumber(this.quoteNumber)
        .contacts(this.contacts)
        .invoiceFeeAmount(this.invoiceFeeAmount);
  }

  public static class BasicCreditCardProtectionQuoteBuilder {
    private final QuoteState quoteState;
    private Optional<UUID> createdBy;
    private Optional<Instant> createdAt;
    private BasicCreditCardProtectionQuoteBuilder(QuoteState quoteState, Optional<Instant> createdAt, Optional<UUID> createdBy) {
      this.quoteState = quoteState;
      this.createdAt = createdAt;
      this.createdBy = createdBy;
    }
    private Optional<BigDecimal> invoiceFeeAmount;
    public BasicCreditCardProtectionQuoteBuilder invoiceFeeAmount(Optional<BigDecimal> invoiceFeeAmount) { this.invoiceFeeAmount = invoiceFeeAmount; return this; }
    private Collection<ContactRoles> contacts;
    public BasicCreditCardProtectionQuoteBuilder contacts(Collection<ContactRoles> contacts) { this.contacts = contacts; return this; }
    public BasicCreditCardProtectionQuoteBuilder addContact(ContactRoles contact) { if (!(this.contacts instanceof ArrayList)) { this.contacts = new ArrayList<>(this.contacts); }; this.contacts.add(contact); return this; }
    private ULID locator;
    public BasicCreditCardProtectionQuoteBuilder locator(ULID locator) { this.locator = locator; return this; }
    private ULID groupLocator;
    public BasicCreditCardProtectionQuoteBuilder groupLocator(ULID groupLocator) { this.groupLocator = groupLocator; return this; }
    private String productName;
    public BasicCreditCardProtectionQuoteBuilder productName(String productName) { this.productName = productName; return this; }
    private ULID accountLocator;
    public BasicCreditCardProtectionQuoteBuilder accountLocator(ULID accountLocator) { this.accountLocator = accountLocator; return this; }
    private Optional<Instant> startTime;
    public BasicCreditCardProtectionQuoteBuilder startTime(Instant startTime) { this.startTime = Optional.ofNullable(startTime); return this; }
    public BasicCreditCardProtectionQuoteBuilder startTime(Optional<Instant> startTime) { this.startTime = startTime; return this; }
    private Optional<Instant> endTime;
    public BasicCreditCardProtectionQuoteBuilder endTime(Instant endTime) { this.endTime = Optional.ofNullable(endTime); return this; }
    public BasicCreditCardProtectionQuoteBuilder endTime(Optional<Instant> endTime) { this.endTime = endTime; return this; }
    private Optional<String> timezone;
    public BasicCreditCardProtectionQuoteBuilder timezone(String timezone) { this.timezone = Optional.ofNullable(timezone); return this; }
    public BasicCreditCardProtectionQuoteBuilder timezone(Optional<String> timezone) { this.timezone = timezone; return this; }
    private Optional<String> currency;
    public BasicCreditCardProtectionQuoteBuilder currency(String currency) { this.currency = Optional.ofNullable(currency); return this; }
    public BasicCreditCardProtectionQuoteBuilder currency(Optional<String> currency) { this.currency = currency; return this; }
    private Optional<String> underwritingStatus;
    public BasicCreditCardProtectionQuoteBuilder underwritingStatus(String underwritingStatus) { this.underwritingStatus = Optional.ofNullable(underwritingStatus); return this; }
    public BasicCreditCardProtectionQuoteBuilder underwritingStatus(Optional<String> underwritingStatus) { this.underwritingStatus = underwritingStatus; return this; }
    private Optional<Instant> expirationTime;
    public BasicCreditCardProtectionQuoteBuilder expirationTime(Instant expirationTime) { this.expirationTime = Optional.ofNullable(expirationTime); return this; }
    public BasicCreditCardProtectionQuoteBuilder expirationTime(Optional<Instant> expirationTime) { this.expirationTime = expirationTime; return this; }
    private Optional<Preferences> preferences;
    public BasicCreditCardProtectionQuoteBuilder preferences(Preferences preferences) { this.preferences = Optional.ofNullable(preferences); return this; }
    public BasicCreditCardProtectionQuoteBuilder preferences(Optional<Preferences> preferences) { this.preferences = preferences; return this; }
    private Optional<ULID> policyLocator;
    public BasicCreditCardProtectionQuoteBuilder policyLocator(ULID policyLocator) { this.policyLocator = Optional.ofNullable(policyLocator); return this; }
    public BasicCreditCardProtectionQuoteBuilder policyLocator(Optional<ULID> policyLocator) { this.policyLocator = policyLocator; return this; }
    private Optional<DurationBasis> durationBasis;
    public BasicCreditCardProtectionQuoteBuilder durationBasis(DurationBasis durationBasis) { this.durationBasis = Optional.ofNullable(durationBasis); return this; }
    public BasicCreditCardProtectionQuoteBuilder durationBasis(Optional<DurationBasis> durationBasis) { this.durationBasis = durationBasis; return this; }
    private Optional<String> delinquencyPlanName;
    public BasicCreditCardProtectionQuoteBuilder delinquencyPlanName(String delinquencyPlanName) { this.delinquencyPlanName = Optional.ofNullable(delinquencyPlanName); return this; }
    public BasicCreditCardProtectionQuoteBuilder delinquencyPlanName(Optional<String> delinquencyPlanName) { this.delinquencyPlanName = delinquencyPlanName; return this; }
    private Optional<String> autoRenewalPlanName;
    public BasicCreditCardProtectionQuoteBuilder autoRenewalPlanName(String autoRenewalPlanName) { this.autoRenewalPlanName = Optional.ofNullable(autoRenewalPlanName); return this; }
    public BasicCreditCardProtectionQuoteBuilder autoRenewalPlanName(Optional<String> autoRenewalPlanName) { this.autoRenewalPlanName = autoRenewalPlanName; return this; }
    private Optional<BillingTrigger> billingTrigger;
    public BasicCreditCardProtectionQuoteBuilder billingTrigger(BillingTrigger billingTrigger) { this.billingTrigger = Optional.ofNullable(billingTrigger); return this; }
    public BasicCreditCardProtectionQuoteBuilder billingTrigger(Optional<BillingTrigger> billingTrigger) { this.billingTrigger = billingTrigger; return this; }
    private Optional<String> region;
    public BasicCreditCardProtectionQuoteBuilder region(Optional<String> region) { this.region = region; return this; }
    public BasicCreditCardProtectionQuoteBuilder region(String region) { this.region = Optional.ofNullable(region); return this; }
    private FraudProtectionQuote fraudProtection;
    public BasicCreditCardProtectionQuoteBuilder fraudProtection(FraudProtectionQuote fraudProtection) { this.fraudProtection = fraudProtection; return this; }
    public BasicCreditCardProtectionQuoteBuilder addFraudProtection(Consumer<FraudProtectionQuote.FraudProtectionQuoteBuilder> mutator) { FraudProtectionQuote.FraudProtectionQuoteBuilder builder = FraudProtectionQuote.builder(this.element.tenantLocator(), this.element.rootLocator(), this.element.locator(), null, null); mutator.accept(builder); this.fraudProtection = builder.build(); return this; }
    private CardReplacementQuote cardReplacement;
    public BasicCreditCardProtectionQuoteBuilder cardReplacement(CardReplacementQuote cardReplacement) { this.cardReplacement = cardReplacement; return this; }
    public BasicCreditCardProtectionQuoteBuilder addCardReplacement(Consumer<CardReplacementQuote.CardReplacementQuoteBuilder> mutator) { CardReplacementQuote.CardReplacementQuoteBuilder builder = CardReplacementQuote.builder(this.element.tenantLocator(), this.element.rootLocator(), this.element.locator(), null, null); mutator.accept(builder); this.cardReplacement = builder.build(); return this; }
    private UnauthorizedUseQuote unauthorizedUse;
    public BasicCreditCardProtectionQuoteBuilder unauthorizedUse(UnauthorizedUseQuote unauthorizedUse) { this.unauthorizedUse = unauthorizedUse; return this; }
    public BasicCreditCardProtectionQuoteBuilder addUnauthorizedUse(Consumer<UnauthorizedUseQuote.UnauthorizedUseQuoteBuilder> mutator) { UnauthorizedUseQuote.UnauthorizedUseQuoteBuilder builder = UnauthorizedUseQuote.builder(this.element.tenantLocator(), this.element.rootLocator(), this.element.locator(), null, null); mutator.accept(builder); this.unauthorizedUse = builder.build(); return this; }
    private FraudLimit fraudLimit;
    public BasicCreditCardProtectionQuoteBuilder fraudLimit(FraudLimit fraudLimit) { this.fraudLimit = fraudLimit; return this; }

    private CardReplacementBenefit cardReplacementBenefit;
    public BasicCreditCardProtectionQuoteBuilder cardReplacementBenefit(CardReplacementBenefit cardReplacementBenefit) { this.cardReplacementBenefit = cardReplacementBenefit; return this; }

    private BasicDeductible basicDeductible;
    public BasicCreditCardProtectionQuoteBuilder basicDeductible(BasicDeductible basicDeductible) { this.basicDeductible = basicDeductible; return this; }

    private BasicCreditCardProtectionQuoteData data;
    public BasicCreditCardProtectionQuoteBuilder data(BasicCreditCardProtectionQuoteData data) { this.data = data; return this; }
    private Element element;
    public BasicCreditCardProtectionQuoteBuilder element(Element element) { this.element = element; return this; }
    private BillingLevel billingLevel;
    public BasicCreditCardProtectionQuoteBuilder billingLevel(BillingLevel billingLevel) { this.billingLevel = billingLevel; return this; }
    private Optional<String> quoteNumber;
    public BasicCreditCardProtectionQuoteBuilder quoteNumber(Optional<String> quoteNumber) { this.quoteNumber = quoteNumber; return this; }
    public BasicCreditCardProtectionQuoteBuilder quoteNumber(String quoteNumber) { this.quoteNumber = Optional.ofNullable(quoteNumber); return this; }
    public BasicCreditCardProtectionQuote build() {
      return new BasicCreditCardProtectionQuote(
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
          this.cardReplacement,
          this.unauthorizedUse,
          this.fraudLimit,
          this.cardReplacementBenefit,
          this.basicDeductible,
          this.data,
          this.element,
          this.billingLevel,
          this.quoteNumber,
          this.contacts,
          this.invoiceFeeAmount
      );
    }
  }

  public record BasicCreditCardProtectionQuoteData(BigDecimal bankRelationshipYears, LocalDate cardIssuedDate, String cardType, BigDecimal creditLimit, String hasPreviousClaims, BigDecimal numberOfCards) implements BasicCreditCardProtectionData, CustomerObject, Validatable {
    public static final String TYPE = "BasicCreditCardProtectionQuoteData";
    public static final Set<String> cardTypeOptions = Set.of("Visa Classic","Mastercard Standard","Discover It","American Express Green");
    public static final Set<String> hasPreviousClaimsOptions = Set.of("None","1-2 claims","3+ claims");

    public BasicCreditCardProtectionQuoteData {
      if (creditLimit != null) { creditLimit = com.socotra.platform.tools.NumberUtils.trimScale(creditLimit, 2, RoundingMode.HALF_EVEN);}
    if (numberOfCards != null) { numberOfCards = com.socotra.platform.tools.NumberUtils.trimScale(numberOfCards, 8, RoundingMode.HALF_EVEN);}
    if (bankRelationshipYears != null) { bankRelationshipYears = com.socotra.platform.tools.NumberUtils.trimScale(bankRelationshipYears, 8, RoundingMode.HALF_EVEN);}
    }

    public String type() { return TYPE; }

    public BasicCreditCardProtectionQuoteData maskData(DataMaskingLevel level) {
      BasicCreditCardProtectionQuoteDataBuilder builder = toBuilder();
      if (level == DataMaskingLevel.level1) {
      }
      if (level == DataMaskingLevel.level2) {
      }
      if (level == DataMaskingLevel.none) {
      }
      return builder.build();
    }

    public BasicCreditCardProtectionQuoteData anonymizeData() {
      BasicCreditCardProtectionQuoteDataBuilder builder = toBuilder();
      return builder.build();
    }

    @Override
    public Collection<ValidationItem> validate(DeploymentConfig config, ValidatableContext context) {
      ValidationItem.ValidationItemBuilder validationItemBuilder = ValidationItem.builder().elementType(TYPE);
      if (bankRelationshipYears == null) { validationItemBuilder.addError("Non optional property 'basicCreditCardProtectionQuoteData.bankRelationshipYears' is missing"); }
      if (bankRelationshipYears != null && bankRelationshipYears.compareTo(new BigDecimal("0")) < 0) { validationItemBuilder.addError("'basicCreditCardProtectionQuoteData.bankRelationshipYears': " +  bankRelationshipYears + " is less than min of 0 with configured precision of 8 and rounding mode: halfEven"); }
      if (bankRelationshipYears != null && bankRelationshipYears.compareTo(new BigDecimal("50")) > 0) { validationItemBuilder.addError("'basicCreditCardProtectionQuoteData.bankRelationshipYears': " +  bankRelationshipYears + " is more than max of 50 with configured precision of 8 and rounding mode: halfEven"); }
      if (cardIssuedDate == null) { validationItemBuilder.addError("Non optional property 'basicCreditCardProtectionQuoteData.cardIssuedDate' is missing"); }
      if (cardType == null || cardType.isBlank()) { validationItemBuilder.addError("Non optional property 'basicCreditCardProtectionQuoteData.cardType' is missing"); }
      if (cardType != null && cardType.length() > 20000) { validationItemBuilder.addError("'basicCreditCardProtectionQuoteData.cardType' length is more than max length of 20000"); }
      if (cardType != null && !cardTypeOptions.contains(cardType)) { validationItemBuilder.addError("Property 'basicCreditCardProtectionQuoteData.cardType' should be one of " + cardTypeOptions); }
      if (creditLimit == null) { validationItemBuilder.addError("Non optional property 'basicCreditCardProtectionQuoteData.creditLimit' is missing"); }
      if (creditLimit != null && creditLimit.compareTo(new BigDecimal("500")) < 0) { validationItemBuilder.addError("'basicCreditCardProtectionQuoteData.creditLimit': " +  creditLimit + " is less than min of 500 with configured precision of 2 and rounding mode: halfEven"); }
      if (creditLimit != null && creditLimit.compareTo(new BigDecimal("25000")) > 0) { validationItemBuilder.addError("'basicCreditCardProtectionQuoteData.creditLimit': " +  creditLimit + " is more than max of 25000 with configured precision of 2 and rounding mode: halfEven"); }
      if (hasPreviousClaims == null || hasPreviousClaims.isBlank()) { validationItemBuilder.addError("Non optional property 'basicCreditCardProtectionQuoteData.hasPreviousClaims' is missing"); }
      if (hasPreviousClaims != null && hasPreviousClaims.length() > 20000) { validationItemBuilder.addError("'basicCreditCardProtectionQuoteData.hasPreviousClaims' length is more than max length of 20000"); }
      if (hasPreviousClaims != null && !hasPreviousClaimsOptions.contains(hasPreviousClaims)) { validationItemBuilder.addError("Property 'basicCreditCardProtectionQuoteData.hasPreviousClaims' should be one of " + hasPreviousClaimsOptions); }
      if (numberOfCards == null) { validationItemBuilder.addError("Non optional property 'basicCreditCardProtectionQuoteData.numberOfCards' is missing"); }
      if (numberOfCards != null && numberOfCards.compareTo(new BigDecimal("1")) < 0) { validationItemBuilder.addError("'basicCreditCardProtectionQuoteData.numberOfCards': " +  numberOfCards + " is less than min of 1 with configured precision of 8 and rounding mode: halfEven"); }
      if (numberOfCards != null && numberOfCards.compareTo(new BigDecimal("5")) > 0) { validationItemBuilder.addError("'basicCreditCardProtectionQuoteData.numberOfCards': " +  numberOfCards + " is more than max of 5 with configured precision of 8 and rounding mode: halfEven"); }

      return validationItemBuilder.hasErrors() ? List.of(validationItemBuilder.build()) : List.of();
    }

    @Override
    public BasicCreditCardProtectionQuoteData applyAvailabilityRemovals(Instant referenceDate) {
      BasicCreditCardProtectionQuoteDataBuilder builder = toBuilder();

      return builder.build();
    }

    public static BasicCreditCardProtectionQuoteDataBuilder builder() {
      return new BasicCreditCardProtectionQuoteDataBuilder();
    }
    public BasicCreditCardProtectionQuoteDataBuilder toBuilder() {
      return new BasicCreditCardProtectionQuoteDataBuilder()
      .bankRelationshipYears(this.bankRelationshipYears)
      .cardIssuedDate(this.cardIssuedDate)
      .cardType(this.cardType)
      .creditLimit(this.creditLimit)
      .hasPreviousClaims(this.hasPreviousClaims)
      .numberOfCards(this.numberOfCards);
    }

    public static class BasicCreditCardProtectionQuoteDataBuilder {
      private BigDecimal bankRelationshipYears;
      public BasicCreditCardProtectionQuoteDataBuilder bankRelationshipYears(BigDecimal bankRelationshipYears) { this.bankRelationshipYears = bankRelationshipYears; return this; }
      private LocalDate cardIssuedDate;
      public BasicCreditCardProtectionQuoteDataBuilder cardIssuedDate(LocalDate cardIssuedDate) { this.cardIssuedDate = cardIssuedDate; return this; }
      private String cardType;
      public BasicCreditCardProtectionQuoteDataBuilder cardType(String cardType) { this.cardType = cardType; return this; }
      private BigDecimal creditLimit;
      public BasicCreditCardProtectionQuoteDataBuilder creditLimit(BigDecimal creditLimit) { this.creditLimit = creditLimit; return this; }
      private String hasPreviousClaims;
      public BasicCreditCardProtectionQuoteDataBuilder hasPreviousClaims(String hasPreviousClaims) { this.hasPreviousClaims = hasPreviousClaims; return this; }
      private BigDecimal numberOfCards;
      public BasicCreditCardProtectionQuoteDataBuilder numberOfCards(BigDecimal numberOfCards) { this.numberOfCards = numberOfCards; return this; }

      public BasicCreditCardProtectionQuoteData build() {
        return new BasicCreditCardProtectionQuoteData(bankRelationshipYears, cardIssuedDate, cardType, creditLimit, hasPreviousClaims, numberOfCards);
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