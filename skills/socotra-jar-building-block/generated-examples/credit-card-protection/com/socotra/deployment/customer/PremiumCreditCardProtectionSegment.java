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

public record PremiumCreditCardProtectionSegment(
    ULID locator,
    @JsonView({Internal.class}) ULID transactionLocator,
    @JsonView({Internal.class}) Optional<ULID> basedOn,
    SegmentType segmentType,
    Instant startTime,
    Instant endTime,
    BigDecimal duration,
    FraudProtectionPolicy fraudProtection,
    IdentityTheftProtectionPolicy identityTheftProtection,
    CardReplacementPolicy cardReplacement,
    UnauthorizedUsePolicy unauthorizedUse,
    PurchaseProtectionPolicy purchaseProtection,
    TravelEmergencyAssistancePolicy travelEmergencyAssistance,
    FraudLimit fraudLimit,
    IdentityTheftLimit identityTheftLimit,
    PurchaseProtectionLimit purchaseProtectionLimit,
    TravelAssistanceLimit travelAssistanceLimit,
    CardReplacementBenefit cardReplacementBenefit,
    PremiumDeductible premiumDeductible,
    PremiumCreditCardProtectionSegmentData data,
    @JsonView({Internal.class}) Element element
  ) implements PremiumCreditCardProtection, Validatable, com.socotra.coremodel.interfaces.Segment, Elemental, CustomerObjectWithData<PremiumCreditCardProtectionSegment.PremiumCreditCardProtectionSegmentData> {

  public static final String TYPE = "PremiumCreditCardProtectionSegment";

  public PremiumCreditCardProtectionSegment {
    basedOn = basedOn == null ? Optional.empty() : basedOn;
    startTime = startTime == null ? null : startTime.truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
    endTime = endTime == null ? null : endTime.truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
    
    
    
    
    
    
    if(data == null) {
      data = PremiumCreditCardProtectionSegmentData.builder().build();
    }
  }

  public String type() { return TYPE; }

  public PremiumCreditCardProtectionSegment maskData(DataMaskingLevel level) {
    PremiumCreditCardProtectionSegmentBuilder builder = toBuilder();
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

  public PremiumCreditCardProtectionSegment anonymizeData() {
    PremiumCreditCardProtectionSegmentBuilder builder = toBuilder();
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

    if (validationItemBuilder.hasErrors()) {
      validationItems.add(validationItemBuilder.build());
    }
    return validationItems;
  }

  public PremiumCreditCardProtectionSegment applyDefaults(DeploymentConfig config) {
    PremiumCreditCardProtectionSegmentBuilder builder = toBuilder();
    { FraudProtectionPolicy a = this.fraudProtection; if (this.fraudProtection == null) { a = FraudProtectionPolicy.builder(element.tenantLocator(), element.rootLocator(), element.locator(), null, null).build(); }; builder.fraudProtection(a.applyDefaults(config)); }

    { IdentityTheftProtectionPolicy a = this.identityTheftProtection; if (this.identityTheftProtection == null) { a = IdentityTheftProtectionPolicy.builder(element.tenantLocator(), element.rootLocator(), element.locator(), null, null).build(); }; builder.identityTheftProtection(a.applyDefaults(config)); }

    { CardReplacementPolicy a = this.cardReplacement; if (this.cardReplacement == null) { a = CardReplacementPolicy.builder(element.tenantLocator(), element.rootLocator(), element.locator(), null, null).build(); }; builder.cardReplacement(a.applyDefaults(config)); }

    { UnauthorizedUsePolicy a = this.unauthorizedUse; if (this.unauthorizedUse == null) { a = UnauthorizedUsePolicy.builder(element.tenantLocator(), element.rootLocator(), element.locator(), null, null).build(); }; builder.unauthorizedUse(a.applyDefaults(config)); }

    { PurchaseProtectionPolicy a = this.purchaseProtection; if (this.purchaseProtection == null) { a = PurchaseProtectionPolicy.builder(element.tenantLocator(), element.rootLocator(), element.locator(), null, null).build(); }; builder.purchaseProtection(a.applyDefaults(config)); }

    { TravelEmergencyAssistancePolicy a = this.travelEmergencyAssistance; if (this.travelEmergencyAssistance == null) { a = TravelEmergencyAssistancePolicy.builder(element.tenantLocator(), element.rootLocator(), element.locator(), null, null).build(); }; builder.travelEmergencyAssistance(a.applyDefaults(config)); }

    if (this.fraudLimit == null) { builder.fraudLimit(com.socotra.deployment.customer.FraudLimit.defaultOption()); }
    if (this.identityTheftLimit == null) { builder.identityTheftLimit(com.socotra.deployment.customer.IdentityTheftLimit.defaultOption()); }
    if (this.purchaseProtectionLimit == null) { builder.purchaseProtectionLimit(com.socotra.deployment.customer.PurchaseProtectionLimit.defaultOption()); }
    if (this.travelAssistanceLimit == null) { builder.travelAssistanceLimit(com.socotra.deployment.customer.TravelAssistanceLimit.defaultOption()); }
    if (this.cardReplacementBenefit == null) { builder.cardReplacementBenefit(com.socotra.deployment.customer.CardReplacementBenefit.defaultOption()); }
    if (this.premiumDeductible == null) { builder.premiumDeductible(com.socotra.deployment.customer.PremiumDeductible.defaultOption()); }
    return builder.build();
  }

  public PremiumCreditCardProtectionSegment applyAvailabilityRemovals(Instant referenceDate) {
    PremiumCreditCardProtectionSegmentBuilder builder = toBuilder();
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

  public static PremiumCreditCardProtectionSegment from(DeploymentFactory factory, com.socotra.coremodel.interfaces.Segment segment) {
    if (segment == null) {
      return null;
    }
    Element element = segment.element();
    return new PremiumCreditCardProtectionSegment(
        segment.locator(),
        segment.transactionLocator(),
        segment.basedOn(),
        segment.segmentType(),
        segment.startTime(),
        segment.endTime(),
        segment.duration(),
        FraudProtectionPolicy.fromElement(factory, element.elements().stream().filter(e -> e.type().equalsIgnoreCase(FraudProtectionPolicy.TYPE)).findAny().orElse(null)),
        IdentityTheftProtectionPolicy.fromElement(factory, element.elements().stream().filter(e -> e.type().equalsIgnoreCase(IdentityTheftProtectionPolicy.TYPE)).findAny().orElse(null)),
        CardReplacementPolicy.fromElement(factory, element.elements().stream().filter(e -> e.type().equalsIgnoreCase(CardReplacementPolicy.TYPE)).findAny().orElse(null)),
        UnauthorizedUsePolicy.fromElement(factory, element.elements().stream().filter(e -> e.type().equalsIgnoreCase(UnauthorizedUsePolicy.TYPE)).findAny().orElse(null)),
        PurchaseProtectionPolicy.fromElement(factory, element.elements().stream().filter(e -> e.type().equalsIgnoreCase(PurchaseProtectionPolicy.TYPE)).findAny().orElse(null)),
        TravelEmergencyAssistancePolicy.fromElement(factory, element.elements().stream().filter(e -> e.type().equalsIgnoreCase(TravelEmergencyAssistancePolicy.TYPE)).findAny().orElse(null)),
        com.socotra.deployment.customer.FraudLimit.fromMap(element.coverageTerms()),
        com.socotra.deployment.customer.IdentityTheftLimit.fromMap(element.coverageTerms()),
        com.socotra.deployment.customer.PurchaseProtectionLimit.fromMap(element.coverageTerms()),
        com.socotra.deployment.customer.TravelAssistanceLimit.fromMap(element.coverageTerms()),
        com.socotra.deployment.customer.CardReplacementBenefit.fromMap(element.coverageTerms()),
        com.socotra.deployment.customer.PremiumDeductible.fromMap(element.coverageTerms()),
        element.resolve(PremiumCreditCardProtectionSegmentData.TYPE, factory),
        element
    );
  }

  public static PremiumCreditCardProtectionSegment copyFrom(UUID tenantLocator, PremiumCreditCardProtectionQuote other) {
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
    PremiumCreditCardProtectionSegmentBuilder builder = new PremiumCreditCardProtectionSegmentBuilder(rootLocator, null, SegmentType.coverage)
        .element(origin);
    return builder
        .startTime(other.startTime().orElseThrow())
        .endTime(other.endTime().orElseThrow())
        .fraudProtection(FraudProtectionPolicy.copyFrom(tenantLocator, rootLocator, builder.element.locator(), other.fraudProtection()))
        .identityTheftProtection(IdentityTheftProtectionPolicy.copyFrom(tenantLocator, rootLocator, builder.element.locator(), other.identityTheftProtection()))
        .cardReplacement(CardReplacementPolicy.copyFrom(tenantLocator, rootLocator, builder.element.locator(), other.cardReplacement()))
        .unauthorizedUse(UnauthorizedUsePolicy.copyFrom(tenantLocator, rootLocator, builder.element.locator(), other.unauthorizedUse()))
        .purchaseProtection(PurchaseProtectionPolicy.copyFrom(tenantLocator, rootLocator, builder.element.locator(), other.purchaseProtection()))
        .travelEmergencyAssistance(TravelEmergencyAssistancePolicy.copyFrom(tenantLocator, rootLocator, builder.element.locator(), other.travelEmergencyAssistance()))
        .fraudLimit(other.fraudLimit())
        .identityTheftLimit(other.identityTheftLimit())
        .purchaseProtectionLimit(other.purchaseProtectionLimit())
        .travelAssistanceLimit(other.travelAssistanceLimit())
        .cardReplacementBenefit(other.cardReplacementBenefit())
        .premiumDeductible(other.premiumDeductible())
        .data(PremiumCreditCardProtectionSegmentData.copyFrom(other.data()))
        .build();
  }

  public static PremiumCreditCardProtectionSegment copyFrom(UUID tenantLocator, com.socotra.coremodel.interfaces.Segment segment) {
    PremiumCreditCardProtectionSegment other = (PremiumCreditCardProtectionSegment) segment;
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
    PremiumCreditCardProtectionSegmentBuilder builder = new PremiumCreditCardProtectionSegmentBuilder(rootLocator, null, SegmentType.coverage)
        .element(origin);
    return builder
        .basedOn(other.locator())
        .startTime(other.startTime())
        .endTime(other.endTime())
        .duration(other.duration())
        .fraudProtection(FraudProtectionPolicy.copyFrom(tenantLocator, rootLocator, builder.element.locator(), other.fraudProtection()))
        .identityTheftProtection(IdentityTheftProtectionPolicy.copyFrom(tenantLocator, rootLocator, builder.element.locator(), other.identityTheftProtection()))
        .cardReplacement(CardReplacementPolicy.copyFrom(tenantLocator, rootLocator, builder.element.locator(), other.cardReplacement()))
        .unauthorizedUse(UnauthorizedUsePolicy.copyFrom(tenantLocator, rootLocator, builder.element.locator(), other.unauthorizedUse()))
        .purchaseProtection(PurchaseProtectionPolicy.copyFrom(tenantLocator, rootLocator, builder.element.locator(), other.purchaseProtection()))
        .travelEmergencyAssistance(TravelEmergencyAssistancePolicy.copyFrom(tenantLocator, rootLocator, builder.element.locator(), other.travelEmergencyAssistance()))
        .fraudLimit(other.fraudLimit())
        .identityTheftLimit(other.identityTheftLimit())
        .purchaseProtectionLimit(other.purchaseProtectionLimit())
        .travelAssistanceLimit(other.travelAssistanceLimit())
        .cardReplacementBenefit(other.cardReplacementBenefit())
        .premiumDeductible(other.premiumDeductible())
        .data(PremiumCreditCardProtectionSegmentData.copyFrom(other.data()))
        .build();
  }

  public PremiumCreditCardProtectionSegmentBuilder toBuilder() {
    return new PremiumCreditCardProtectionSegmentBuilder(this.locator, this.transactionLocator, this.segmentType)
        .basedOn(this.basedOn)
        .startTime(this.startTime)
        .endTime(this.endTime)
        .duration(this.duration)
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
        .element(this.element);
  }

  public static class PremiumCreditCardProtectionSegmentBuilder {
    private final ULID locator;
    private final ULID transactionLocator;
    private final SegmentType segmentType;
    private PremiumCreditCardProtectionSegmentBuilder(ULID locator, ULID transactionLocator, SegmentType segmentType) {
      this.locator = locator;
      this.transactionLocator = transactionLocator;
      this.segmentType = segmentType;
    }
    private Optional<ULID> basedOn;
    public PremiumCreditCardProtectionSegmentBuilder basedOn(ULID locator) { this.basedOn = Optional.ofNullable(locator); return this; }
    public PremiumCreditCardProtectionSegmentBuilder basedOn(Optional<ULID> basedOn) { this.basedOn = basedOn; return this; }
    private Instant startTime;
    public PremiumCreditCardProtectionSegmentBuilder startTime(Instant startTime) { this.startTime = startTime; return this; }
    private Instant endTime;
    public PremiumCreditCardProtectionSegmentBuilder endTime(Instant endTime) { this.endTime = endTime; return this; }
    private BigDecimal duration;
    public PremiumCreditCardProtectionSegmentBuilder duration(BigDecimal duration) { this.duration = duration; return this; }
    private FraudProtectionPolicy fraudProtection;
    public PremiumCreditCardProtectionSegmentBuilder fraudProtection(FraudProtectionPolicy fraudProtection) { this.fraudProtection = fraudProtection; return this; }

    public PremiumCreditCardProtectionSegmentBuilder addFraudProtection(Consumer<FraudProtectionPolicy.FraudProtectionPolicyBuilder> mutator) { FraudProtectionPolicy.FraudProtectionPolicyBuilder builder = FraudProtectionPolicy.builder(this.element.tenantLocator(), this.element.rootLocator(), this.element.locator(), null, null); mutator.accept(builder); this.fraudProtection = builder.build(); return this; }
    private IdentityTheftProtectionPolicy identityTheftProtection;
    public PremiumCreditCardProtectionSegmentBuilder identityTheftProtection(IdentityTheftProtectionPolicy identityTheftProtection) { this.identityTheftProtection = identityTheftProtection; return this; }

    public PremiumCreditCardProtectionSegmentBuilder addIdentityTheftProtection(Consumer<IdentityTheftProtectionPolicy.IdentityTheftProtectionPolicyBuilder> mutator) { IdentityTheftProtectionPolicy.IdentityTheftProtectionPolicyBuilder builder = IdentityTheftProtectionPolicy.builder(this.element.tenantLocator(), this.element.rootLocator(), this.element.locator(), null, null); mutator.accept(builder); this.identityTheftProtection = builder.build(); return this; }
    private CardReplacementPolicy cardReplacement;
    public PremiumCreditCardProtectionSegmentBuilder cardReplacement(CardReplacementPolicy cardReplacement) { this.cardReplacement = cardReplacement; return this; }

    public PremiumCreditCardProtectionSegmentBuilder addCardReplacement(Consumer<CardReplacementPolicy.CardReplacementPolicyBuilder> mutator) { CardReplacementPolicy.CardReplacementPolicyBuilder builder = CardReplacementPolicy.builder(this.element.tenantLocator(), this.element.rootLocator(), this.element.locator(), null, null); mutator.accept(builder); this.cardReplacement = builder.build(); return this; }
    private UnauthorizedUsePolicy unauthorizedUse;
    public PremiumCreditCardProtectionSegmentBuilder unauthorizedUse(UnauthorizedUsePolicy unauthorizedUse) { this.unauthorizedUse = unauthorizedUse; return this; }

    public PremiumCreditCardProtectionSegmentBuilder addUnauthorizedUse(Consumer<UnauthorizedUsePolicy.UnauthorizedUsePolicyBuilder> mutator) { UnauthorizedUsePolicy.UnauthorizedUsePolicyBuilder builder = UnauthorizedUsePolicy.builder(this.element.tenantLocator(), this.element.rootLocator(), this.element.locator(), null, null); mutator.accept(builder); this.unauthorizedUse = builder.build(); return this; }
    private PurchaseProtectionPolicy purchaseProtection;
    public PremiumCreditCardProtectionSegmentBuilder purchaseProtection(PurchaseProtectionPolicy purchaseProtection) { this.purchaseProtection = purchaseProtection; return this; }

    public PremiumCreditCardProtectionSegmentBuilder addPurchaseProtection(Consumer<PurchaseProtectionPolicy.PurchaseProtectionPolicyBuilder> mutator) { PurchaseProtectionPolicy.PurchaseProtectionPolicyBuilder builder = PurchaseProtectionPolicy.builder(this.element.tenantLocator(), this.element.rootLocator(), this.element.locator(), null, null); mutator.accept(builder); this.purchaseProtection = builder.build(); return this; }
    private TravelEmergencyAssistancePolicy travelEmergencyAssistance;
    public PremiumCreditCardProtectionSegmentBuilder travelEmergencyAssistance(TravelEmergencyAssistancePolicy travelEmergencyAssistance) { this.travelEmergencyAssistance = travelEmergencyAssistance; return this; }

    public PremiumCreditCardProtectionSegmentBuilder addTravelEmergencyAssistance(Consumer<TravelEmergencyAssistancePolicy.TravelEmergencyAssistancePolicyBuilder> mutator) { TravelEmergencyAssistancePolicy.TravelEmergencyAssistancePolicyBuilder builder = TravelEmergencyAssistancePolicy.builder(this.element.tenantLocator(), this.element.rootLocator(), this.element.locator(), null, null); mutator.accept(builder); this.travelEmergencyAssistance = builder.build(); return this; }
    private FraudLimit fraudLimit;
    public PremiumCreditCardProtectionSegmentBuilder fraudLimit(FraudLimit fraudLimit) { this.fraudLimit = fraudLimit; return this; }

    private IdentityTheftLimit identityTheftLimit;
    public PremiumCreditCardProtectionSegmentBuilder identityTheftLimit(IdentityTheftLimit identityTheftLimit) { this.identityTheftLimit = identityTheftLimit; return this; }

    private PurchaseProtectionLimit purchaseProtectionLimit;
    public PremiumCreditCardProtectionSegmentBuilder purchaseProtectionLimit(PurchaseProtectionLimit purchaseProtectionLimit) { this.purchaseProtectionLimit = purchaseProtectionLimit; return this; }

    private TravelAssistanceLimit travelAssistanceLimit;
    public PremiumCreditCardProtectionSegmentBuilder travelAssistanceLimit(TravelAssistanceLimit travelAssistanceLimit) { this.travelAssistanceLimit = travelAssistanceLimit; return this; }

    private CardReplacementBenefit cardReplacementBenefit;
    public PremiumCreditCardProtectionSegmentBuilder cardReplacementBenefit(CardReplacementBenefit cardReplacementBenefit) { this.cardReplacementBenefit = cardReplacementBenefit; return this; }

    private PremiumDeductible premiumDeductible;
    public PremiumCreditCardProtectionSegmentBuilder premiumDeductible(PremiumDeductible premiumDeductible) { this.premiumDeductible = premiumDeductible; return this; }

    private PremiumCreditCardProtectionSegmentData data;
    public PremiumCreditCardProtectionSegmentBuilder data(PremiumCreditCardProtectionSegmentData data) { this.data = data; return this; }
    private Element element;
    public PremiumCreditCardProtectionSegmentBuilder element(Element element) { this.element = element; return this; }

    public PremiumCreditCardProtectionSegment build() {
      return new PremiumCreditCardProtectionSegment(
          this.locator,
          this.transactionLocator,
          this.basedOn,
          this.segmentType,
          this.startTime,
          this.endTime,
          this.duration,
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
          this.element
      );
    }
  }

  public record PremiumCreditCardProtectionSegmentData(BigDecimal averageMonthlySpend, BigDecimal bankRelationshipYears, LocalDate cardIssuedDate, String cardType, BigDecimal creditLimit, String frequentTraveler, String hasPreviousClaims, BigDecimal numberOfCards) implements PremiumCreditCardProtectionData, CustomerObject, Validatable {
    public static final String TYPE = "PremiumCreditCardProtectionSegmentData";
    public static final Set<String> cardTypeOptions = Set.of("Visa Signature","Visa Infinite","Mastercard World","Mastercard World Elite","American Express Gold","American Express Platinum");
    public static final Set<String> frequentTravelerOptions = Set.of("Yes","No");
    public static final Set<String> hasPreviousClaimsOptions = Set.of("None","1-2 claims","3+ claims");

    public PremiumCreditCardProtectionSegmentData {
      if (creditLimit != null) { creditLimit = com.socotra.platform.tools.NumberUtils.trimScale(creditLimit, 2, RoundingMode.HALF_EVEN);}
    if (numberOfCards != null) { numberOfCards = com.socotra.platform.tools.NumberUtils.trimScale(numberOfCards, 8, RoundingMode.HALF_EVEN);}
    if (averageMonthlySpend != null) { averageMonthlySpend = com.socotra.platform.tools.NumberUtils.trimScale(averageMonthlySpend, 2, RoundingMode.HALF_EVEN);}
    if (bankRelationshipYears != null) { bankRelationshipYears = com.socotra.platform.tools.NumberUtils.trimScale(bankRelationshipYears, 8, RoundingMode.HALF_EVEN);}
    }

    public String type() { return TYPE; }

    public PremiumCreditCardProtectionSegmentData maskData(DataMaskingLevel level) {
      PremiumCreditCardProtectionSegmentDataBuilder builder = toBuilder();
      if (level == DataMaskingLevel.level1) {
      }
      if (level == DataMaskingLevel.level2) {
      }
      if (level == DataMaskingLevel.none) {
      }
      return builder.build();
    }

    public PremiumCreditCardProtectionSegmentData anonymizeData() {
      PremiumCreditCardProtectionSegmentDataBuilder builder = toBuilder();
      return builder.build();
    }

    @Override
    public Collection<ValidationItem> validate(DeploymentConfig config, ValidatableContext context) {
      ValidationItem.ValidationItemBuilder validationItemBuilder = ValidationItem.builder().elementType(TYPE);
      if (averageMonthlySpend == null) { validationItemBuilder.addError("Non optional property 'premiumCreditCardProtectionSegmentData.averageMonthlySpend' is missing"); }
      if (averageMonthlySpend != null && averageMonthlySpend.compareTo(new BigDecimal("100")) < 0) { validationItemBuilder.addError("'premiumCreditCardProtectionSegmentData.averageMonthlySpend': " +  averageMonthlySpend + " is less than min of 100 with configured precision of 2 and rounding mode: halfEven"); }
      if (averageMonthlySpend != null && averageMonthlySpend.compareTo(new BigDecimal("25000")) > 0) { validationItemBuilder.addError("'premiumCreditCardProtectionSegmentData.averageMonthlySpend': " +  averageMonthlySpend + " is more than max of 25000 with configured precision of 2 and rounding mode: halfEven"); }
      if (bankRelationshipYears == null) { validationItemBuilder.addError("Non optional property 'premiumCreditCardProtectionSegmentData.bankRelationshipYears' is missing"); }
      if (bankRelationshipYears != null && bankRelationshipYears.compareTo(new BigDecimal("0")) < 0) { validationItemBuilder.addError("'premiumCreditCardProtectionSegmentData.bankRelationshipYears': " +  bankRelationshipYears + " is less than min of 0 with configured precision of 8 and rounding mode: halfEven"); }
      if (bankRelationshipYears != null && bankRelationshipYears.compareTo(new BigDecimal("50")) > 0) { validationItemBuilder.addError("'premiumCreditCardProtectionSegmentData.bankRelationshipYears': " +  bankRelationshipYears + " is more than max of 50 with configured precision of 8 and rounding mode: halfEven"); }
      if (cardIssuedDate == null) { validationItemBuilder.addError("Non optional property 'premiumCreditCardProtectionSegmentData.cardIssuedDate' is missing"); }
      if (cardType == null || cardType.isBlank()) { validationItemBuilder.addError("Non optional property 'premiumCreditCardProtectionSegmentData.cardType' is missing"); }
      if (cardType != null && cardType.length() > 20000) { validationItemBuilder.addError("'premiumCreditCardProtectionSegmentData.cardType' length is more than max length of 20000"); }
      if (cardType != null && !cardTypeOptions.contains(cardType)) { validationItemBuilder.addError("Property 'premiumCreditCardProtectionSegmentData.cardType' should be one of " + cardTypeOptions); }
      if (creditLimit == null) { validationItemBuilder.addError("Non optional property 'premiumCreditCardProtectionSegmentData.creditLimit' is missing"); }
      if (creditLimit != null && creditLimit.compareTo(new BigDecimal("5000")) < 0) { validationItemBuilder.addError("'premiumCreditCardProtectionSegmentData.creditLimit': " +  creditLimit + " is less than min of 5000 with configured precision of 2 and rounding mode: halfEven"); }
      if (creditLimit != null && creditLimit.compareTo(new BigDecimal("100000")) > 0) { validationItemBuilder.addError("'premiumCreditCardProtectionSegmentData.creditLimit': " +  creditLimit + " is more than max of 100000 with configured precision of 2 and rounding mode: halfEven"); }
      if (frequentTraveler == null || frequentTraveler.isBlank()) { validationItemBuilder.addError("Non optional property 'premiumCreditCardProtectionSegmentData.frequentTraveler' is missing"); }
      if (frequentTraveler != null && frequentTraveler.length() > 20000) { validationItemBuilder.addError("'premiumCreditCardProtectionSegmentData.frequentTraveler' length is more than max length of 20000"); }
      if (frequentTraveler != null && !frequentTravelerOptions.contains(frequentTraveler)) { validationItemBuilder.addError("Property 'premiumCreditCardProtectionSegmentData.frequentTraveler' should be one of " + frequentTravelerOptions); }
      if (hasPreviousClaims == null || hasPreviousClaims.isBlank()) { validationItemBuilder.addError("Non optional property 'premiumCreditCardProtectionSegmentData.hasPreviousClaims' is missing"); }
      if (hasPreviousClaims != null && hasPreviousClaims.length() > 20000) { validationItemBuilder.addError("'premiumCreditCardProtectionSegmentData.hasPreviousClaims' length is more than max length of 20000"); }
      if (hasPreviousClaims != null && !hasPreviousClaimsOptions.contains(hasPreviousClaims)) { validationItemBuilder.addError("Property 'premiumCreditCardProtectionSegmentData.hasPreviousClaims' should be one of " + hasPreviousClaimsOptions); }
      if (numberOfCards == null) { validationItemBuilder.addError("Non optional property 'premiumCreditCardProtectionSegmentData.numberOfCards' is missing"); }
      if (numberOfCards != null && numberOfCards.compareTo(new BigDecimal("1")) < 0) { validationItemBuilder.addError("'premiumCreditCardProtectionSegmentData.numberOfCards': " +  numberOfCards + " is less than min of 1 with configured precision of 8 and rounding mode: halfEven"); }
      if (numberOfCards != null && numberOfCards.compareTo(new BigDecimal("10")) > 0) { validationItemBuilder.addError("'premiumCreditCardProtectionSegmentData.numberOfCards': " +  numberOfCards + " is more than max of 10 with configured precision of 8 and rounding mode: halfEven"); }

      return validationItemBuilder.hasErrors() ? List.of(validationItemBuilder.build()) : List.of();
    }

    @Override
    public PremiumCreditCardProtectionSegmentData applyAvailabilityRemovals(Instant referenceDate) {
      PremiumCreditCardProtectionSegmentDataBuilder builder = toBuilder();

      return builder.build();
    }

    public static PremiumCreditCardProtectionSegmentDataBuilder builder() {
      return new PremiumCreditCardProtectionSegmentDataBuilder();
    }
    public PremiumCreditCardProtectionSegmentDataBuilder toBuilder() {
      return new PremiumCreditCardProtectionSegmentDataBuilder()
      .averageMonthlySpend(this.averageMonthlySpend)
      .bankRelationshipYears(this.bankRelationshipYears)
      .cardIssuedDate(this.cardIssuedDate)
      .cardType(this.cardType)
      .creditLimit(this.creditLimit)
      .frequentTraveler(this.frequentTraveler)
      .hasPreviousClaims(this.hasPreviousClaims)
      .numberOfCards(this.numberOfCards);
    }

    public static class PremiumCreditCardProtectionSegmentDataBuilder {
      private BigDecimal averageMonthlySpend;
      public PremiumCreditCardProtectionSegmentDataBuilder averageMonthlySpend(BigDecimal averageMonthlySpend) { this.averageMonthlySpend = averageMonthlySpend; return this; }
      private BigDecimal bankRelationshipYears;
      public PremiumCreditCardProtectionSegmentDataBuilder bankRelationshipYears(BigDecimal bankRelationshipYears) { this.bankRelationshipYears = bankRelationshipYears; return this; }
      private LocalDate cardIssuedDate;
      public PremiumCreditCardProtectionSegmentDataBuilder cardIssuedDate(LocalDate cardIssuedDate) { this.cardIssuedDate = cardIssuedDate; return this; }
      private String cardType;
      public PremiumCreditCardProtectionSegmentDataBuilder cardType(String cardType) { this.cardType = cardType; return this; }
      private BigDecimal creditLimit;
      public PremiumCreditCardProtectionSegmentDataBuilder creditLimit(BigDecimal creditLimit) { this.creditLimit = creditLimit; return this; }
      private String frequentTraveler;
      public PremiumCreditCardProtectionSegmentDataBuilder frequentTraveler(String frequentTraveler) { this.frequentTraveler = frequentTraveler; return this; }
      private String hasPreviousClaims;
      public PremiumCreditCardProtectionSegmentDataBuilder hasPreviousClaims(String hasPreviousClaims) { this.hasPreviousClaims = hasPreviousClaims; return this; }
      private BigDecimal numberOfCards;
      public PremiumCreditCardProtectionSegmentDataBuilder numberOfCards(BigDecimal numberOfCards) { this.numberOfCards = numberOfCards; return this; }

      public PremiumCreditCardProtectionSegmentData build() {
        return new PremiumCreditCardProtectionSegmentData(averageMonthlySpend, bankRelationshipYears, cardIssuedDate, cardType, creditLimit, frequentTraveler, hasPreviousClaims, numberOfCards);
      }
    }

    public static PremiumCreditCardProtectionSegmentData copyFrom(PremiumCreditCardProtectionQuote.PremiumCreditCardProtectionQuoteData other) {
      if (other == null) {
        return null;
      }
      return new PremiumCreditCardProtectionSegmentData(
          other.averageMonthlySpend(),
          other.bankRelationshipYears(),
          other.cardIssuedDate(),
          other.cardType(),
          other.creditLimit(),
          other.frequentTraveler(),
          other.hasPreviousClaims(),
          other.numberOfCards()
      );
    }

    public static PremiumCreditCardProtectionSegmentData copyFrom(PremiumCreditCardProtectionSegmentData other) {
      if (other == null) {
        return null;
      }
      return new PremiumCreditCardProtectionSegmentData(
          other.averageMonthlySpend(),
          other.bankRelationshipYears(),
          other.cardIssuedDate(),
          other.cardType(),
          other.creditLimit(),
          other.frequentTraveler(),
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