package com.socotra.deployment.customer;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.type.*;
import com.socotra.platform.tools.ULID;
import com.socotra.coremodel.*;
import com.socotra.deployment.*;
import com.socotra.coremodel.views.Internal;

import java.math.*;
import java.time.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.*;
import java.util.stream.Collectors;

public record BasicCreditCardProtectionSegment(
    ULID locator,
    @JsonView({Internal.class}) ULID transactionLocator,
    @JsonView({Internal.class}) Optional<ULID> basedOn,
    SegmentType segmentType,
    Instant startTime,
    Instant endTime,
    BigDecimal duration,
    FraudProtectionPolicy fraudProtection,
    CardReplacementPolicy cardReplacement,
    UnauthorizedUsePolicy unauthorizedUse,
    FraudLimit fraudLimit,
    CardReplacementBenefit cardReplacementBenefit,
    BasicDeductible basicDeductible,
    BasicCreditCardProtectionSegmentData data,
    @JsonView({Internal.class}) Element element
  ) implements BasicCreditCardProtection, Validatable, com.socotra.coremodel.interfaces.Segment, Elemental, CustomerObjectWithData<BasicCreditCardProtectionSegment.BasicCreditCardProtectionSegmentData> {

  public static final String TYPE = "BasicCreditCardProtectionSegment";

  public BasicCreditCardProtectionSegment {
    basedOn = basedOn == null ? Optional.empty() : basedOn;
    startTime = startTime == null ? null : startTime.truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
    endTime = endTime == null ? null : endTime.truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
    
    
    
    if(data == null) {
      data = BasicCreditCardProtectionSegmentData.builder().build();
    }
  }

  public String type() { return TYPE; }

  public BasicCreditCardProtectionSegment maskData(DataMaskingLevel level) {
    BasicCreditCardProtectionSegmentBuilder builder = toBuilder();
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

  public BasicCreditCardProtectionSegment anonymizeData() {
    BasicCreditCardProtectionSegmentBuilder builder = toBuilder();
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

  public Instant originalEffectiveTime() {
    return element.originalEffectiveTime().orElse(null);
  }

  @Override
  public Collection<ValidationItem> validate(DeploymentConfig config, ValidatableContext context) {
    Collection<ValidationItem> validationItems = new ArrayList<>();
    ValidationItem.ValidationItemBuilder validationItemBuilder = ValidationItem.builder()
      .elementType(type()).locator(element.locator());
    if (this.startTime.isAfter(this.endTime)) { validationItemBuilder.addError("startTime[" + startTime + "] is after endTime[" + endTime + "]"); }
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

    if (validationItemBuilder.hasErrors()) {
      validationItems.add(validationItemBuilder.build());
    }
    return validationItems;
  }

  public BasicCreditCardProtectionSegment applyDefaults(DeploymentConfig config) {
    BasicCreditCardProtectionSegmentBuilder builder = toBuilder();
    { FraudProtectionPolicy a = this.fraudProtection; if (this.fraudProtection == null) { a = FraudProtectionPolicy.builder(element.tenantLocator(), element.rootLocator(), element.locator(), null, null).build(); }; builder.fraudProtection(a.applyDefaults(config)); }

    { CardReplacementPolicy a = this.cardReplacement; if (this.cardReplacement == null) { a = CardReplacementPolicy.builder(element.tenantLocator(), element.rootLocator(), element.locator(), null, null).build(); }; builder.cardReplacement(a.applyDefaults(config)); }

    { UnauthorizedUsePolicy a = this.unauthorizedUse; if (this.unauthorizedUse == null) { a = UnauthorizedUsePolicy.builder(element.tenantLocator(), element.rootLocator(), element.locator(), null, null).build(); }; builder.unauthorizedUse(a.applyDefaults(config)); }

    if (this.fraudLimit == null) { builder.fraudLimit(com.socotra.deployment.customer.FraudLimit.defaultOption()); }
    if (this.cardReplacementBenefit == null) { builder.cardReplacementBenefit(com.socotra.deployment.customer.CardReplacementBenefit.defaultOption()); }
    if (this.basicDeductible == null) { builder.basicDeductible(com.socotra.deployment.customer.BasicDeductible.defaultOption()); }
    return builder.build();
  }

  public BasicCreditCardProtectionSegment applyAvailabilityRemovals(Instant referenceDate) {
    BasicCreditCardProtectionSegmentBuilder builder = toBuilder();
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

  public static BasicCreditCardProtectionSegment from(DeploymentFactory factory, com.socotra.coremodel.interfaces.Segment segment) {
    if (segment == null) {
      return null;
    }
    Element element = segment.element();
    return new BasicCreditCardProtectionSegment(
        segment.locator(),
        segment.transactionLocator(),
        segment.basedOn(),
        segment.segmentType(),
        segment.startTime(),
        segment.endTime(),
        segment.duration(),
        FraudProtectionPolicy.fromElement(factory, element.elements().stream().filter(e -> e.type().equalsIgnoreCase(FraudProtectionPolicy.TYPE)).findAny().orElse(null)),
        CardReplacementPolicy.fromElement(factory, element.elements().stream().filter(e -> e.type().equalsIgnoreCase(CardReplacementPolicy.TYPE)).findAny().orElse(null)),
        UnauthorizedUsePolicy.fromElement(factory, element.elements().stream().filter(e -> e.type().equalsIgnoreCase(UnauthorizedUsePolicy.TYPE)).findAny().orElse(null)),
        com.socotra.deployment.customer.FraudLimit.fromMap(element.coverageTerms()),
        com.socotra.deployment.customer.CardReplacementBenefit.fromMap(element.coverageTerms()),
        com.socotra.deployment.customer.BasicDeductible.fromMap(element.coverageTerms()),
        element.resolve(BasicCreditCardProtectionSegmentData.TYPE, factory),
        element
    );
  }

  public static BasicCreditCardProtectionSegment copyFrom(UUID tenantLocator, BasicCreditCardProtectionQuote other) {
    ULID rootLocator = ULID.generate();
    Element origin = Element.builder()
        .tenantLocator(tenantLocator)
        .type(other.type().substring(0, other.type().lastIndexOf("Quote")) + "Segment")
        .locator(rootLocator)
        .staticLocator(other.locator())
        .rootLocator(rootLocator)
        .parentLocator(rootLocator)
        .originalEffectiveTime(other.element().originalEffectiveTime())
        .build();
    BasicCreditCardProtectionSegmentBuilder builder = new BasicCreditCardProtectionSegmentBuilder(rootLocator, null, SegmentType.coverage)
        .element(origin);
    return builder
        .startTime(other.startTime().orElseThrow())
        .endTime(other.endTime().orElseThrow())
        .fraudProtection(FraudProtectionPolicy.copyFrom(tenantLocator, rootLocator, builder.element.locator(), other.fraudProtection()))
        .cardReplacement(CardReplacementPolicy.copyFrom(tenantLocator, rootLocator, builder.element.locator(), other.cardReplacement()))
        .unauthorizedUse(UnauthorizedUsePolicy.copyFrom(tenantLocator, rootLocator, builder.element.locator(), other.unauthorizedUse()))
        .fraudLimit(other.fraudLimit())
        .cardReplacementBenefit(other.cardReplacementBenefit())
        .basicDeductible(other.basicDeductible())
        .data(BasicCreditCardProtectionSegmentData.copyFrom(other.data()))
        .build();
  }

  public static BasicCreditCardProtectionSegment copyFrom(UUID tenantLocator, com.socotra.coremodel.interfaces.Segment segment) {
    BasicCreditCardProtectionSegment other = (BasicCreditCardProtectionSegment) segment;
    ULID rootLocator = ULID.generate();
    Element origin = Element.builder()
        .tenantLocator(tenantLocator)
        .type(other.type())
        .locator(rootLocator)
        .staticLocator(other.element().staticLocator())
        .originalEffectiveTime(other.element().originalEffectiveTime())
        .rootLocator(rootLocator)
        .parentLocator(rootLocator)
        .build();
    BasicCreditCardProtectionSegmentBuilder builder = new BasicCreditCardProtectionSegmentBuilder(rootLocator, null, SegmentType.coverage)
        .element(origin);
    return builder
        .basedOn(other.locator())
        .startTime(other.startTime())
        .endTime(other.endTime())
        .duration(other.duration())
        .fraudProtection(FraudProtectionPolicy.copyFrom(tenantLocator, rootLocator, builder.element.locator(), other.fraudProtection()))
        .cardReplacement(CardReplacementPolicy.copyFrom(tenantLocator, rootLocator, builder.element.locator(), other.cardReplacement()))
        .unauthorizedUse(UnauthorizedUsePolicy.copyFrom(tenantLocator, rootLocator, builder.element.locator(), other.unauthorizedUse()))
        .fraudLimit(other.fraudLimit())
        .cardReplacementBenefit(other.cardReplacementBenefit())
        .basicDeductible(other.basicDeductible())
        .data(BasicCreditCardProtectionSegmentData.copyFrom(other.data()))
        .build();
  }

  public BasicCreditCardProtectionSegmentBuilder toBuilder() {
    return new BasicCreditCardProtectionSegmentBuilder(this.locator, this.transactionLocator, this.segmentType)
        .basedOn(this.basedOn)
        .startTime(this.startTime)
        .endTime(this.endTime)
        .duration(this.duration)
        .fraudProtection(this.fraudProtection)
.cardReplacement(this.cardReplacement)
.unauthorizedUse(this.unauthorizedUse)

        .fraudLimit(this.fraudLimit)
.cardReplacementBenefit(this.cardReplacementBenefit)
.basicDeductible(this.basicDeductible)

        .data(this.data)
        .element(this.element);
  }

  public static class BasicCreditCardProtectionSegmentBuilder {
    private final ULID locator;
    private final ULID transactionLocator;
    private final SegmentType segmentType;
    private BasicCreditCardProtectionSegmentBuilder(ULID locator, ULID transactionLocator, SegmentType segmentType) {
      this.locator = locator;
      this.transactionLocator = transactionLocator;
      this.segmentType = segmentType;
    }
    private Optional<ULID> basedOn;
    public BasicCreditCardProtectionSegmentBuilder basedOn(ULID locator) { this.basedOn = Optional.ofNullable(locator); return this; }
    public BasicCreditCardProtectionSegmentBuilder basedOn(Optional<ULID> basedOn) { this.basedOn = basedOn; return this; }
    private Instant startTime;
    public BasicCreditCardProtectionSegmentBuilder startTime(Instant startTime) { this.startTime = startTime; return this; }
    private Instant endTime;
    public BasicCreditCardProtectionSegmentBuilder endTime(Instant endTime) { this.endTime = endTime; return this; }
    private BigDecimal duration;
    public BasicCreditCardProtectionSegmentBuilder duration(BigDecimal duration) { this.duration = duration; return this; }
    private FraudProtectionPolicy fraudProtection;
    public BasicCreditCardProtectionSegmentBuilder fraudProtection(FraudProtectionPolicy fraudProtection) { this.fraudProtection = fraudProtection; return this; }

    public BasicCreditCardProtectionSegmentBuilder addFraudProtection(Consumer<FraudProtectionPolicy.FraudProtectionPolicyBuilder> mutator) { FraudProtectionPolicy.FraudProtectionPolicyBuilder builder = FraudProtectionPolicy.builder(this.element.tenantLocator(), this.element.rootLocator(), this.element.locator(), null, null); mutator.accept(builder); this.fraudProtection = builder.build(); return this; }
    private CardReplacementPolicy cardReplacement;
    public BasicCreditCardProtectionSegmentBuilder cardReplacement(CardReplacementPolicy cardReplacement) { this.cardReplacement = cardReplacement; return this; }

    public BasicCreditCardProtectionSegmentBuilder addCardReplacement(Consumer<CardReplacementPolicy.CardReplacementPolicyBuilder> mutator) { CardReplacementPolicy.CardReplacementPolicyBuilder builder = CardReplacementPolicy.builder(this.element.tenantLocator(), this.element.rootLocator(), this.element.locator(), null, null); mutator.accept(builder); this.cardReplacement = builder.build(); return this; }
    private UnauthorizedUsePolicy unauthorizedUse;
    public BasicCreditCardProtectionSegmentBuilder unauthorizedUse(UnauthorizedUsePolicy unauthorizedUse) { this.unauthorizedUse = unauthorizedUse; return this; }

    public BasicCreditCardProtectionSegmentBuilder addUnauthorizedUse(Consumer<UnauthorizedUsePolicy.UnauthorizedUsePolicyBuilder> mutator) { UnauthorizedUsePolicy.UnauthorizedUsePolicyBuilder builder = UnauthorizedUsePolicy.builder(this.element.tenantLocator(), this.element.rootLocator(), this.element.locator(), null, null); mutator.accept(builder); this.unauthorizedUse = builder.build(); return this; }
    private FraudLimit fraudLimit;
    public BasicCreditCardProtectionSegmentBuilder fraudLimit(FraudLimit fraudLimit) { this.fraudLimit = fraudLimit; return this; }

    private CardReplacementBenefit cardReplacementBenefit;
    public BasicCreditCardProtectionSegmentBuilder cardReplacementBenefit(CardReplacementBenefit cardReplacementBenefit) { this.cardReplacementBenefit = cardReplacementBenefit; return this; }

    private BasicDeductible basicDeductible;
    public BasicCreditCardProtectionSegmentBuilder basicDeductible(BasicDeductible basicDeductible) { this.basicDeductible = basicDeductible; return this; }

    private BasicCreditCardProtectionSegmentData data;
    public BasicCreditCardProtectionSegmentBuilder data(BasicCreditCardProtectionSegmentData data) { this.data = data; return this; }
    private Element element;
    public BasicCreditCardProtectionSegmentBuilder element(Element element) { this.element = element; return this; }

    public BasicCreditCardProtectionSegment build() {
      return new BasicCreditCardProtectionSegment(
          this.locator,
          this.transactionLocator,
          this.basedOn,
          this.segmentType,
          this.startTime,
          this.endTime,
          this.duration,
          this.fraudProtection,
          this.cardReplacement,
          this.unauthorizedUse,
          this.fraudLimit,
          this.cardReplacementBenefit,
          this.basicDeductible,
          this.data,
          this.element
      );
    }
  }

  public record BasicCreditCardProtectionSegmentData(BigDecimal bankRelationshipYears, LocalDate cardIssuedDate, String cardType, BigDecimal creditLimit, String hasPreviousClaims, BigDecimal numberOfCards) implements BasicCreditCardProtectionData, CustomerObject, Validatable {
    public static final String TYPE = "BasicCreditCardProtectionSegmentData";
    public static final Set<String> cardTypeOptions = Set.of("Visa Classic","Mastercard Standard","Discover It","American Express Green");
    public static final Set<String> hasPreviousClaimsOptions = Set.of("None","1-2 claims","3+ claims");

    public BasicCreditCardProtectionSegmentData {
      if (creditLimit != null) { creditLimit = com.socotra.platform.tools.NumberUtils.trimScale(creditLimit, 2, RoundingMode.HALF_EVEN);}
    if (numberOfCards != null) { numberOfCards = com.socotra.platform.tools.NumberUtils.trimScale(numberOfCards, 8, RoundingMode.HALF_EVEN);}
    if (bankRelationshipYears != null) { bankRelationshipYears = com.socotra.platform.tools.NumberUtils.trimScale(bankRelationshipYears, 8, RoundingMode.HALF_EVEN);}
    }

    public String type() { return TYPE; }

    public BasicCreditCardProtectionSegmentData maskData(DataMaskingLevel level) {
      BasicCreditCardProtectionSegmentDataBuilder builder = toBuilder();
      if (level == DataMaskingLevel.level1) {
      }
      if (level == DataMaskingLevel.level2) {
      }
      if (level == DataMaskingLevel.none) {
      }
      return builder.build();
    }

    public BasicCreditCardProtectionSegmentData anonymizeData() {
      BasicCreditCardProtectionSegmentDataBuilder builder = toBuilder();
      return builder.build();
    }

    @Override
    public Collection<ValidationItem> validate(DeploymentConfig config, ValidatableContext context) {
      ValidationItem.ValidationItemBuilder validationItemBuilder = ValidationItem.builder().elementType(TYPE);
      if (bankRelationshipYears == null) { validationItemBuilder.addError("Non optional property 'basicCreditCardProtectionSegmentData.bankRelationshipYears' is missing"); }
      if (bankRelationshipYears != null && bankRelationshipYears.compareTo(new BigDecimal("0")) < 0) { validationItemBuilder.addError("'basicCreditCardProtectionSegmentData.bankRelationshipYears': " +  bankRelationshipYears + " is less than min of 0 with configured precision of 8 and rounding mode: halfEven"); }
      if (bankRelationshipYears != null && bankRelationshipYears.compareTo(new BigDecimal("50")) > 0) { validationItemBuilder.addError("'basicCreditCardProtectionSegmentData.bankRelationshipYears': " +  bankRelationshipYears + " is more than max of 50 with configured precision of 8 and rounding mode: halfEven"); }
      if (cardIssuedDate == null) { validationItemBuilder.addError("Non optional property 'basicCreditCardProtectionSegmentData.cardIssuedDate' is missing"); }
      if (cardType == null || cardType.isBlank()) { validationItemBuilder.addError("Non optional property 'basicCreditCardProtectionSegmentData.cardType' is missing"); }
      if (cardType != null && cardType.length() > 20000) { validationItemBuilder.addError("'basicCreditCardProtectionSegmentData.cardType' length is more than max length of 20000"); }
      if (cardType != null && !cardTypeOptions.contains(cardType)) { validationItemBuilder.addError("Property 'basicCreditCardProtectionSegmentData.cardType' should be one of " + cardTypeOptions); }
      if (creditLimit == null) { validationItemBuilder.addError("Non optional property 'basicCreditCardProtectionSegmentData.creditLimit' is missing"); }
      if (creditLimit != null && creditLimit.compareTo(new BigDecimal("500")) < 0) { validationItemBuilder.addError("'basicCreditCardProtectionSegmentData.creditLimit': " +  creditLimit + " is less than min of 500 with configured precision of 2 and rounding mode: halfEven"); }
      if (creditLimit != null && creditLimit.compareTo(new BigDecimal("25000")) > 0) { validationItemBuilder.addError("'basicCreditCardProtectionSegmentData.creditLimit': " +  creditLimit + " is more than max of 25000 with configured precision of 2 and rounding mode: halfEven"); }
      if (hasPreviousClaims == null || hasPreviousClaims.isBlank()) { validationItemBuilder.addError("Non optional property 'basicCreditCardProtectionSegmentData.hasPreviousClaims' is missing"); }
      if (hasPreviousClaims != null && hasPreviousClaims.length() > 20000) { validationItemBuilder.addError("'basicCreditCardProtectionSegmentData.hasPreviousClaims' length is more than max length of 20000"); }
      if (hasPreviousClaims != null && !hasPreviousClaimsOptions.contains(hasPreviousClaims)) { validationItemBuilder.addError("Property 'basicCreditCardProtectionSegmentData.hasPreviousClaims' should be one of " + hasPreviousClaimsOptions); }
      if (numberOfCards == null) { validationItemBuilder.addError("Non optional property 'basicCreditCardProtectionSegmentData.numberOfCards' is missing"); }
      if (numberOfCards != null && numberOfCards.compareTo(new BigDecimal("1")) < 0) { validationItemBuilder.addError("'basicCreditCardProtectionSegmentData.numberOfCards': " +  numberOfCards + " is less than min of 1 with configured precision of 8 and rounding mode: halfEven"); }
      if (numberOfCards != null && numberOfCards.compareTo(new BigDecimal("5")) > 0) { validationItemBuilder.addError("'basicCreditCardProtectionSegmentData.numberOfCards': " +  numberOfCards + " is more than max of 5 with configured precision of 8 and rounding mode: halfEven"); }

      return validationItemBuilder.hasErrors() ? List.of(validationItemBuilder.build()) : List.of();
    }

    @Override
    public BasicCreditCardProtectionSegmentData applyAvailabilityRemovals(Instant referenceDate) {
      BasicCreditCardProtectionSegmentDataBuilder builder = toBuilder();

      return builder.build();
    }

    public static BasicCreditCardProtectionSegmentDataBuilder builder() {
      return new BasicCreditCardProtectionSegmentDataBuilder();
    }
    public BasicCreditCardProtectionSegmentDataBuilder toBuilder() {
      return new BasicCreditCardProtectionSegmentDataBuilder()
      .bankRelationshipYears(this.bankRelationshipYears)
      .cardIssuedDate(this.cardIssuedDate)
      .cardType(this.cardType)
      .creditLimit(this.creditLimit)
      .hasPreviousClaims(this.hasPreviousClaims)
      .numberOfCards(this.numberOfCards);
    }

    public static class BasicCreditCardProtectionSegmentDataBuilder {
      private BigDecimal bankRelationshipYears;
      public BasicCreditCardProtectionSegmentDataBuilder bankRelationshipYears(BigDecimal bankRelationshipYears) { this.bankRelationshipYears = bankRelationshipYears; return this; }
      private LocalDate cardIssuedDate;
      public BasicCreditCardProtectionSegmentDataBuilder cardIssuedDate(LocalDate cardIssuedDate) { this.cardIssuedDate = cardIssuedDate; return this; }
      private String cardType;
      public BasicCreditCardProtectionSegmentDataBuilder cardType(String cardType) { this.cardType = cardType; return this; }
      private BigDecimal creditLimit;
      public BasicCreditCardProtectionSegmentDataBuilder creditLimit(BigDecimal creditLimit) { this.creditLimit = creditLimit; return this; }
      private String hasPreviousClaims;
      public BasicCreditCardProtectionSegmentDataBuilder hasPreviousClaims(String hasPreviousClaims) { this.hasPreviousClaims = hasPreviousClaims; return this; }
      private BigDecimal numberOfCards;
      public BasicCreditCardProtectionSegmentDataBuilder numberOfCards(BigDecimal numberOfCards) { this.numberOfCards = numberOfCards; return this; }

      public BasicCreditCardProtectionSegmentData build() {
        return new BasicCreditCardProtectionSegmentData(bankRelationshipYears, cardIssuedDate, cardType, creditLimit, hasPreviousClaims, numberOfCards);
      }
    }

    public static BasicCreditCardProtectionSegmentData copyFrom(BasicCreditCardProtectionQuote.BasicCreditCardProtectionQuoteData other) {
      if (other == null) {
        return null;
      }
      return new BasicCreditCardProtectionSegmentData(
          other.bankRelationshipYears(),
          other.cardIssuedDate(),
          other.cardType(),
          other.creditLimit(),
          other.hasPreviousClaims(),
          other.numberOfCards()
      );
    }

    public static BasicCreditCardProtectionSegmentData copyFrom(BasicCreditCardProtectionSegmentData other) {
      if (other == null) {
        return null;
      }
      return new BasicCreditCardProtectionSegmentData(
          other.bankRelationshipYears(),
          other.cardIssuedDate(),
          other.cardType(),
          other.creditLimit(),
          other.hasPreviousClaims(),
          other.numberOfCards()
      );
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