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

public record BasicCreditCardProtectionQuickQuote(
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
    CardReplacementQuickQuote cardReplacement,
    UnauthorizedUseQuickQuote unauthorizedUse,
    FraudLimit fraudLimit,
    CardReplacementBenefit cardReplacementBenefit,
    BasicDeductible basicDeductible,
    BasicCreditCardProtectionQuickQuoteData data,
    @JsonView({Internal.class}) Element element,
    Collection<ContactRoles> contacts
  ) implements BasicCreditCardProtection, ContactsHolder, Validatable, com.socotra.coremodel.interfaces.QuickQuote, Elemental, CustomerObjectWithData<BasicCreditCardProtectionQuickQuote.BasicCreditCardProtectionQuickQuoteData> {

  public static final String TYPE = "BasicCreditCardProtectionQuickQuote";
  private static final Map<String, ContactSlot> CONTACT_SLOTS = Map.ofEntries(

  );
  public Map<String, ContactSlot> contactSlots() { return CONTACT_SLOTS; }

  public BasicCreditCardProtectionQuickQuote {
    
    
    
    if(data == null) {
      data = BasicCreditCardProtectionQuickQuoteData.builder().build();
    }
      contacts = contacts == null ? List.of() : contacts;
  }

  public String type() { return TYPE; }

  public BasicCreditCardProtectionQuickQuote maskData(DataMaskingLevel level) {
    BasicCreditCardProtectionQuickQuoteBuilder builder = toBuilder();
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

  public BasicCreditCardProtectionQuickQuote anonymizeData() {
    BasicCreditCardProtectionQuickQuoteBuilder builder = toBuilder();
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
    if (this.endTime.isEmpty()) { validationItemBuilder.addError("endTime is required but missing"); }
    if (this.startTime.isEmpty()) { validationItemBuilder.addError("startTime is required but missing"); }
    if (this.startTime.get().isAfter(this.endTime.get())) { validationItemBuilder.addError("startTime[" + startTime.get() + "] is after endTime[" + endTime.get() + "]"); }
    if (this.accountLocator == null) { validationItemBuilder.addError("Non optional property 'accountLocator' missing"); }
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

  public BasicCreditCardProtectionQuickQuote applyDefaults(DeploymentConfig config) {
    BasicCreditCardProtectionQuickQuoteBuilder builder = toBuilder();
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

    { CardReplacementQuickQuote a = this.cardReplacement; if (this.cardReplacement == null) { a = CardReplacementQuickQuote.builder(element.tenantLocator(), element.rootLocator(), element.locator(), null, null).build(); }; builder.cardReplacement(a.applyDefaults(config)); }

    { UnauthorizedUseQuickQuote a = this.unauthorizedUse; if (this.unauthorizedUse == null) { a = UnauthorizedUseQuickQuote.builder(element.tenantLocator(), element.rootLocator(), element.locator(), null, null).build(); }; builder.unauthorizedUse(a.applyDefaults(config)); }

    if (this.fraudLimit == null) { builder.fraudLimit(com.socotra.deployment.customer.FraudLimit.defaultOption()); }
    if (this.cardReplacementBenefit == null) { builder.cardReplacementBenefit(com.socotra.deployment.customer.CardReplacementBenefit.defaultOption()); }
    if (this.basicDeductible == null) { builder.basicDeductible(com.socotra.deployment.customer.BasicDeductible.defaultOption()); }
    return builder.build();
  }

  public BasicCreditCardProtectionQuickQuote applyAvailabilityRemovals(Instant referenceDate) {
    BasicCreditCardProtectionQuickQuoteBuilder builder = toBuilder();
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

  public static BasicCreditCardProtectionQuickQuote from(DeploymentFactory factory, com.socotra.coremodel.interfaces.QuickQuote quote) {
    if (quote == null) {
      return null;
    }
    Element element = quote.element();
    return new BasicCreditCardProtectionQuickQuote(
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
        CardReplacementQuickQuote.fromElement(factory, element.elements().stream().filter(e -> e.type().equalsIgnoreCase(CardReplacementQuickQuote.TYPE)).findAny().orElse(null)),
        UnauthorizedUseQuickQuote.fromElement(factory, element.elements().stream().filter(e -> e.type().equalsIgnoreCase(UnauthorizedUseQuickQuote.TYPE)).findAny().orElse(null)),
        com.socotra.deployment.customer.FraudLimit.fromMap(element.coverageTerms()),
        com.socotra.deployment.customer.CardReplacementBenefit.fromMap(element.coverageTerms()),
        com.socotra.deployment.customer.BasicDeductible.fromMap(element.coverageTerms()),
        element.resolve(BasicCreditCardProtectionQuickQuoteData.TYPE, factory),
        element,
        quote.contacts()
    );
  }

  public static BasicCreditCardProtectionQuickQuoteBuilder builder() {
    return new BasicCreditCardProtectionQuickQuoteBuilder(QuickQuoteState.draft, Optional.empty(), Optional.empty());
  }

  public BasicCreditCardProtectionQuickQuoteBuilder toBuilder() {
    return new BasicCreditCardProtectionQuickQuoteBuilder(this.state, this.createdAt, this.createdBy)
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
.cardReplacement(this.cardReplacement)
.unauthorizedUse(this.unauthorizedUse)

        .fraudLimit(this.fraudLimit)
.cardReplacementBenefit(this.cardReplacementBenefit)
.basicDeductible(this.basicDeductible)

        .data(this.data)
        .element(this.element)
        .contacts(this.contacts);
  }

  public static class BasicCreditCardProtectionQuickQuoteBuilder {
    private final QuickQuoteState state;
    private Optional<Instant> createdAt;
    private Optional<UUID> createdBy;
    private BasicCreditCardProtectionQuickQuoteBuilder(QuickQuoteState state,Optional<Instant> createdAt, Optional<UUID> createdBy) {
      this.state = state;
    }
    private Collection<ContactRoles> contacts;
    public BasicCreditCardProtectionQuickQuoteBuilder contacts(Collection<ContactRoles> contacts) { this.contacts = contacts; return this; }
    public BasicCreditCardProtectionQuickQuoteBuilder addContact(ContactRoles contact) { if (!(this.contacts instanceof ArrayList)) { this.contacts = new ArrayList<>(this.contacts); }; this.contacts.add(contact); return this; }
    private ULID locator;
    public BasicCreditCardProtectionQuickQuoteBuilder locator(ULID locator) { this.locator = locator; return this; }
    private String productName;
    public BasicCreditCardProtectionQuickQuoteBuilder productName(String productName) { this.productName = productName; return this; }
    private Optional<ULID> accountLocator;
    public BasicCreditCardProtectionQuickQuoteBuilder accountLocator(ULID accountLocator) { this.accountLocator = Optional.ofNullable(accountLocator); return this; }
    public BasicCreditCardProtectionQuickQuoteBuilder accountLocator(Optional<ULID> accountLocator) { this.accountLocator = accountLocator; return this; }
    private Optional<Instant> startTime;
    public BasicCreditCardProtectionQuickQuoteBuilder startTime(Instant startTime) { this.startTime = Optional.ofNullable(startTime); return this; }
    public BasicCreditCardProtectionQuickQuoteBuilder startTime(Optional<Instant> startTime) { this.startTime = startTime; return this; }
    private Optional<Instant> endTime;
    public BasicCreditCardProtectionQuickQuoteBuilder endTime(Instant endTime) { this.endTime = Optional.ofNullable(endTime); return this; }
    public BasicCreditCardProtectionQuickQuoteBuilder endTime(Optional<Instant> endTime) { this.endTime = endTime; return this; }
    private Optional<String> timezone;
    public BasicCreditCardProtectionQuickQuoteBuilder timezone(String timezone) { this.timezone = Optional.ofNullable(timezone); return this; }
    public BasicCreditCardProtectionQuickQuoteBuilder timezone(Optional<String> timezone) { this.timezone = timezone; return this; }
    private Optional<String> currency;
    public BasicCreditCardProtectionQuickQuoteBuilder currency(String currency) { this.currency = Optional.ofNullable(currency); return this; }
    public BasicCreditCardProtectionQuickQuoteBuilder currency(Optional<String> currency) { this.currency = currency; return this; }
    private Optional<Instant> expirationTime;
    public BasicCreditCardProtectionQuickQuoteBuilder expirationTime(Instant expirationTime) { this.expirationTime = Optional.ofNullable(expirationTime); return this; }
    public BasicCreditCardProtectionQuickQuoteBuilder expirationTime(Optional<Instant> expirationTime) { this.expirationTime = expirationTime; return this; }
    private Optional<DurationBasis> durationBasis;
    public BasicCreditCardProtectionQuickQuoteBuilder durationBasis(DurationBasis durationBasis) { this.durationBasis = Optional.ofNullable(durationBasis); return this; }
    public BasicCreditCardProtectionQuickQuoteBuilder durationBasis(Optional<DurationBasis> durationBasis) { this.durationBasis = durationBasis; return this; }
    private FraudProtectionQuickQuote fraudProtection;
    public BasicCreditCardProtectionQuickQuoteBuilder fraudProtection(FraudProtectionQuickQuote fraudProtection) { this.fraudProtection = fraudProtection; return this; }

    public BasicCreditCardProtectionQuickQuoteBuilder addFraudProtection(Consumer<FraudProtectionQuickQuote.FraudProtectionQuickQuoteBuilder> mutator) { FraudProtectionQuickQuote.FraudProtectionQuickQuoteBuilder builder = FraudProtectionQuickQuote.builder(this.element.tenantLocator(), this.element.rootLocator(), this.element.locator(), null, null); mutator.accept(builder); this.fraudProtection = builder.build(); return this; }
    private CardReplacementQuickQuote cardReplacement;
    public BasicCreditCardProtectionQuickQuoteBuilder cardReplacement(CardReplacementQuickQuote cardReplacement) { this.cardReplacement = cardReplacement; return this; }

    public BasicCreditCardProtectionQuickQuoteBuilder addCardReplacement(Consumer<CardReplacementQuickQuote.CardReplacementQuickQuoteBuilder> mutator) { CardReplacementQuickQuote.CardReplacementQuickQuoteBuilder builder = CardReplacementQuickQuote.builder(this.element.tenantLocator(), this.element.rootLocator(), this.element.locator(), null, null); mutator.accept(builder); this.cardReplacement = builder.build(); return this; }
    private UnauthorizedUseQuickQuote unauthorizedUse;
    public BasicCreditCardProtectionQuickQuoteBuilder unauthorizedUse(UnauthorizedUseQuickQuote unauthorizedUse) { this.unauthorizedUse = unauthorizedUse; return this; }

    public BasicCreditCardProtectionQuickQuoteBuilder addUnauthorizedUse(Consumer<UnauthorizedUseQuickQuote.UnauthorizedUseQuickQuoteBuilder> mutator) { UnauthorizedUseQuickQuote.UnauthorizedUseQuickQuoteBuilder builder = UnauthorizedUseQuickQuote.builder(this.element.tenantLocator(), this.element.rootLocator(), this.element.locator(), null, null); mutator.accept(builder); this.unauthorizedUse = builder.build(); return this; }
    private FraudLimit fraudLimit;
    public BasicCreditCardProtectionQuickQuoteBuilder fraudLimit(FraudLimit fraudLimit) { this.fraudLimit = fraudLimit; return this; }

    private CardReplacementBenefit cardReplacementBenefit;
    public BasicCreditCardProtectionQuickQuoteBuilder cardReplacementBenefit(CardReplacementBenefit cardReplacementBenefit) { this.cardReplacementBenefit = cardReplacementBenefit; return this; }

    private BasicDeductible basicDeductible;
    public BasicCreditCardProtectionQuickQuoteBuilder basicDeductible(BasicDeductible basicDeductible) { this.basicDeductible = basicDeductible; return this; }

    private BasicCreditCardProtectionQuickQuoteData data;
    public BasicCreditCardProtectionQuickQuoteBuilder data(BasicCreditCardProtectionQuickQuoteData data) { this.data = data; return this; }
    private Element element;
    public BasicCreditCardProtectionQuickQuoteBuilder element(Element element) { this.element = element; return this; }
    public BasicCreditCardProtectionQuickQuote build() {
      return new BasicCreditCardProtectionQuickQuote(
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
          this.cardReplacement,
          this.unauthorizedUse,
          this.fraudLimit,
          this.cardReplacementBenefit,
          this.basicDeductible,
          this.data,
          this.element,
          this.contacts
      );
    }
  }

  public record BasicCreditCardProtectionQuickQuoteData(BigDecimal bankRelationshipYears, LocalDate cardIssuedDate, String cardType, BigDecimal creditLimit, String hasPreviousClaims, BigDecimal numberOfCards) implements BasicCreditCardProtectionData, CustomerObject, Validatable {
    public static final String TYPE = "BasicCreditCardProtectionQuickQuoteData";
    public static final Set<String> cardTypeOptions = Set.of("Visa Classic","Mastercard Standard","Discover It","American Express Green");
    public static final Set<String> hasPreviousClaimsOptions = Set.of("None","1-2 claims","3+ claims");

    public BasicCreditCardProtectionQuickQuoteData {
      if (creditLimit != null) { creditLimit = com.socotra.platform.tools.NumberUtils.trimScale(creditLimit, 2, RoundingMode.HALF_EVEN);}
    if (numberOfCards != null) { numberOfCards = com.socotra.platform.tools.NumberUtils.trimScale(numberOfCards, 8, RoundingMode.HALF_EVEN);}
    if (bankRelationshipYears != null) { bankRelationshipYears = com.socotra.platform.tools.NumberUtils.trimScale(bankRelationshipYears, 8, RoundingMode.HALF_EVEN);}
    }

    public String type() { return TYPE; }

    public BasicCreditCardProtectionQuickQuoteData maskData(DataMaskingLevel level) {
      BasicCreditCardProtectionQuickQuoteDataBuilder builder = toBuilder();
      if (level == DataMaskingLevel.level1) {
      }
      if (level == DataMaskingLevel.level2) {
      }
      if (level == DataMaskingLevel.none) {
      }
      return builder.build();
    }

    public BasicCreditCardProtectionQuickQuoteData anonymizeData() {
      BasicCreditCardProtectionQuickQuoteDataBuilder builder = toBuilder();
      return builder.build();
    }

    @Override
    public Collection<ValidationItem> validate(DeploymentConfig config, ValidatableContext context) {
      ValidationItem.ValidationItemBuilder validationItemBuilder = ValidationItem.builder().elementType(TYPE);
      if (bankRelationshipYears == null) { validationItemBuilder.addError("Non optional property 'basicCreditCardProtectionQuickQuoteData.bankRelationshipYears' is missing"); }
      if (bankRelationshipYears != null && bankRelationshipYears.compareTo(new BigDecimal("0")) < 0) { validationItemBuilder.addError("'basicCreditCardProtectionQuickQuoteData.bankRelationshipYears': " +  bankRelationshipYears + " is less than min of 0 with configured precision of 8 and rounding mode: halfEven"); }
      if (bankRelationshipYears != null && bankRelationshipYears.compareTo(new BigDecimal("50")) > 0) { validationItemBuilder.addError("'basicCreditCardProtectionQuickQuoteData.bankRelationshipYears': " +  bankRelationshipYears + " is more than max of 50 with configured precision of 8 and rounding mode: halfEven"); }
      if (cardIssuedDate == null) { validationItemBuilder.addError("Non optional property 'basicCreditCardProtectionQuickQuoteData.cardIssuedDate' is missing"); }
      if (cardType == null || cardType.isBlank()) { validationItemBuilder.addError("Non optional property 'basicCreditCardProtectionQuickQuoteData.cardType' is missing"); }
      if (cardType != null && cardType.length() > 20000) { validationItemBuilder.addError("'basicCreditCardProtectionQuickQuoteData.cardType' length is more than max length of 20000"); }
      if (cardType != null && !cardTypeOptions.contains(cardType)) { validationItemBuilder.addError("Property 'basicCreditCardProtectionQuickQuoteData.cardType' should be one of " + cardTypeOptions); }
      if (creditLimit == null) { validationItemBuilder.addError("Non optional property 'basicCreditCardProtectionQuickQuoteData.creditLimit' is missing"); }
      if (creditLimit != null && creditLimit.compareTo(new BigDecimal("500")) < 0) { validationItemBuilder.addError("'basicCreditCardProtectionQuickQuoteData.creditLimit': " +  creditLimit + " is less than min of 500 with configured precision of 2 and rounding mode: halfEven"); }
      if (creditLimit != null && creditLimit.compareTo(new BigDecimal("25000")) > 0) { validationItemBuilder.addError("'basicCreditCardProtectionQuickQuoteData.creditLimit': " +  creditLimit + " is more than max of 25000 with configured precision of 2 and rounding mode: halfEven"); }
      if (hasPreviousClaims == null || hasPreviousClaims.isBlank()) { validationItemBuilder.addError("Non optional property 'basicCreditCardProtectionQuickQuoteData.hasPreviousClaims' is missing"); }
      if (hasPreviousClaims != null && hasPreviousClaims.length() > 20000) { validationItemBuilder.addError("'basicCreditCardProtectionQuickQuoteData.hasPreviousClaims' length is more than max length of 20000"); }
      if (hasPreviousClaims != null && !hasPreviousClaimsOptions.contains(hasPreviousClaims)) { validationItemBuilder.addError("Property 'basicCreditCardProtectionQuickQuoteData.hasPreviousClaims' should be one of " + hasPreviousClaimsOptions); }
      if (numberOfCards == null) { validationItemBuilder.addError("Non optional property 'basicCreditCardProtectionQuickQuoteData.numberOfCards' is missing"); }
      if (numberOfCards != null && numberOfCards.compareTo(new BigDecimal("1")) < 0) { validationItemBuilder.addError("'basicCreditCardProtectionQuickQuoteData.numberOfCards': " +  numberOfCards + " is less than min of 1 with configured precision of 8 and rounding mode: halfEven"); }
      if (numberOfCards != null && numberOfCards.compareTo(new BigDecimal("5")) > 0) { validationItemBuilder.addError("'basicCreditCardProtectionQuickQuoteData.numberOfCards': " +  numberOfCards + " is more than max of 5 with configured precision of 8 and rounding mode: halfEven"); }

      return validationItemBuilder.hasErrors() ? List.of(validationItemBuilder.build()) : List.of();
    }

    @Override
    public BasicCreditCardProtectionQuickQuoteData applyAvailabilityRemovals(Instant referenceDate) {
      BasicCreditCardProtectionQuickQuoteDataBuilder builder = toBuilder();

      return builder.build();
    }

    public static BasicCreditCardProtectionQuickQuoteDataBuilder builder() {
      return new BasicCreditCardProtectionQuickQuoteDataBuilder();
    }
    public BasicCreditCardProtectionQuickQuoteDataBuilder toBuilder() {
      return new BasicCreditCardProtectionQuickQuoteDataBuilder()
      .bankRelationshipYears(this.bankRelationshipYears)
      .cardIssuedDate(this.cardIssuedDate)
      .cardType(this.cardType)
      .creditLimit(this.creditLimit)
      .hasPreviousClaims(this.hasPreviousClaims)
      .numberOfCards(this.numberOfCards);
    }

    public static class BasicCreditCardProtectionQuickQuoteDataBuilder {
      private BigDecimal bankRelationshipYears;
      public BasicCreditCardProtectionQuickQuoteDataBuilder bankRelationshipYears(BigDecimal bankRelationshipYears) { this.bankRelationshipYears = bankRelationshipYears; return this; }
      private LocalDate cardIssuedDate;
      public BasicCreditCardProtectionQuickQuoteDataBuilder cardIssuedDate(LocalDate cardIssuedDate) { this.cardIssuedDate = cardIssuedDate; return this; }
      private String cardType;
      public BasicCreditCardProtectionQuickQuoteDataBuilder cardType(String cardType) { this.cardType = cardType; return this; }
      private BigDecimal creditLimit;
      public BasicCreditCardProtectionQuickQuoteDataBuilder creditLimit(BigDecimal creditLimit) { this.creditLimit = creditLimit; return this; }
      private String hasPreviousClaims;
      public BasicCreditCardProtectionQuickQuoteDataBuilder hasPreviousClaims(String hasPreviousClaims) { this.hasPreviousClaims = hasPreviousClaims; return this; }
      private BigDecimal numberOfCards;
      public BasicCreditCardProtectionQuickQuoteDataBuilder numberOfCards(BigDecimal numberOfCards) { this.numberOfCards = numberOfCards; return this; }

      public BasicCreditCardProtectionQuickQuoteData build() {
        return new BasicCreditCardProtectionQuickQuoteData(bankRelationshipYears, cardIssuedDate, cardType, creditLimit, hasPreviousClaims, numberOfCards);
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