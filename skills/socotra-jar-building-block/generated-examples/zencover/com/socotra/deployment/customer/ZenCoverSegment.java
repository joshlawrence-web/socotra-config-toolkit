package com.socotra.deployment.customer;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.type.*;
import com.socotra.platform.tools.ULID;
import com.socotra.coremodel.*;
import com.socotra.coremodel.constraints.*;
import com.socotra.deployment.*;
import com.socotra.coremodel.views.Internal;

import java.math.*;
import java.time.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.*;
import java.util.stream.Collectors;

public record ZenCoverSegment(
    ULID locator,
    @JsonView({Internal.class}) ULID transactionLocator,
    @JsonView({Internal.class}) Optional<ULID> basedOn,
    SegmentType segmentType,
    Instant startTime,
    Instant endTime,
    BigDecimal duration,
    Collection<ItemPolicy> items,
    ZenCoverSegmentData data,
    Optional<ProducerInfo> producerInfo,
    @JsonView({Internal.class}) Element element
  ) implements ZenCover, Validatable<ZenCoverSegment>, com.socotra.coremodel.interfaces.Segment, Elemental, MoratoriumCheck, CustomerObject, SensitiveDataHolder<ZenCoverSegment.ZenCoverSegmentData> {

  public static final String TYPE = "ZenCoverSegment";

  public ZenCoverSegment {
    basedOn = basedOn == null ? Optional.empty() : basedOn;
    startTime = startTime == null ? null : startTime.truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
    endTime = endTime == null ? null : endTime.truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
    items = items == null ? List.of() : List.copyOf(items);
    if(data == null) {
      data = ZenCoverSegmentData.builder().build();
    }
    producerInfo = producerInfo == null ? Optional.empty() : producerInfo;
  }

  public String type() { return TYPE; }

  public ZenCoverSegment maskData(DataMaskingLevel level) {
    ZenCoverSegmentBuilder builder = toBuilder();
    builder.data(this.data.maskData(level));
    if (this.items != null) {
      builder.items(this.items.stream().map(x -> x.maskData(level)).toList());
    }
    return builder.build();
  }

  public ZenCoverSegment anonymizeData() {
    ZenCoverSegmentBuilder builder = toBuilder();
    builder.data(this.data.anonymizeData());
    if (this.items != null) {
      builder.items(this.items.stream().map(x -> x.anonymizeData()).toList());
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
    if (this.items == null || this.items.isEmpty()) {
      validationItemBuilder.addError("'items' should have at least one element");
    } else {
      this.items.forEach(c -> validationItems.addAll(c.validate(config, context)));
    }

    this.data.validate(config, context).forEach(error -> error.errors().forEach(validationItemBuilder::addError));

    if (validationItemBuilder.hasErrors()) {
      validationItems.add(validationItemBuilder.build());
    }
    return validationItems;
  }

  @Override
  public ZenCoverSegment correct(DeploymentConfig config, ValidationErrorResolver resolver) {
    ZenCoverSegmentBuilder builder = toBuilder();
    Map<String, java.lang.reflect.RecordComponent> fields = Arrays.stream(this.getClass().getRecordComponents()).collect(Collectors.toMap(java.lang.reflect.RecordComponent::getName, c -> c));
    if(items != null) {
      builder.items(items.stream().map(v -> v == null ? null : v.correct(config, resolver)).filter(v -> v != null).toList());
    } else {
      resolver.correct(this, fields.get("items"), builder::items, new Required<>(items));
    }
    builder.data(this.data.correct(config, resolver));

    return builder.build();
  }

  public ZenCoverSegment applyDefaults(DeploymentConfig config) {
    ZenCoverSegmentBuilder builder = toBuilder();
    if (this.items != null) { builder.items(this.items.stream().map(e -> e.applyDefaults(config)).toList()); }

    return builder.build();
  }

  public ZenCoverSegment applyAvailabilityRemovals(Instant referenceDate) {
    ZenCoverSegmentBuilder builder = toBuilder();
    if (this.items != null) {
      builder.items(this.items.stream().map(e -> e.applyAvailabilityRemovals(referenceDate)).toList());
    }

    builder.data(this.data.applyAvailabilityRemovals(referenceDate));
    return builder.build();
  }

  public Element toElement(DeploymentFactory factory) {
    Map<String, Object> ct = Map.of();
    Collection<Element> subElements = new ArrayList<>();
    if (this.items != null) {
      this.items.forEach(c -> subElements.add(c.toElement(factory)));
    }

    Map<String, Object> data = CustomerDataHolder.transform(factory, this.data());
    return element.toBuilder()
        .type(type())
        .elements(Collections.unmodifiableCollection(subElements))
        .coverageTerms(ct)
        .data(data)
        .category(ElementCategory.product)
        .build();
  }

  public static ZenCoverSegment from(DeploymentFactory factory, com.socotra.coremodel.interfaces.Segment segment) {
    if (segment == null) {
      return null;
    }
    Element element = segment.element();
    return new ZenCoverSegment(
        segment.locator(),
        segment.transactionLocator(),
        segment.basedOn(),
        segment.segmentType(),
        segment.startTime(),
        segment.endTime(),
        segment.duration(),
        element.elements().stream().filter(e -> e.type().equalsIgnoreCase(ItemPolicy.TYPE)).map(e -> ItemPolicy.fromElement(factory, e)).toList(),
        element.resolve(ZenCoverSegmentData.TYPE, factory),
        segment.producerInfo(),
        element
    );
  }

  public static ZenCoverSegment copyFrom(UUID tenantLocator, ZenCoverQuote other) {
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
    ZenCoverSegmentBuilder builder = new ZenCoverSegmentBuilder(rootLocator, null, SegmentType.coverage)
        .element(origin);
    return builder
        .startTime(other.startTime().orElseThrow())
        .endTime(other.endTime().orElseThrow())
        .items(other.items().stream().map(e -> ItemPolicy.copyFrom(tenantLocator, rootLocator, builder.element.locator(), e)).toList())
        .data(ZenCoverSegmentData.copyFrom(other.data()))
        .producerInfo(other.producerCode().isPresent() ? Optional.of(ProducerInfo.builder().producerCode(other.producerCode()).producerCodeOfRecord(other.producerCode()).build()) : Optional.empty())
        .build();
  }

  public static ZenCoverSegment copyFrom(UUID tenantLocator, com.socotra.coremodel.interfaces.Segment segment) {
    ZenCoverSegment other = (ZenCoverSegment) segment;
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
    ZenCoverSegmentBuilder builder = new ZenCoverSegmentBuilder(rootLocator, null, SegmentType.coverage)
        .element(origin);
    return builder
        .basedOn(other.locator())
        .startTime(other.startTime())
        .endTime(other.endTime())
        .duration(other.duration())
        .items(other.items().stream().map(e -> ItemPolicy.copyFrom(tenantLocator, rootLocator, builder.element.locator(), e)).toList())
        .data(ZenCoverSegmentData.copyFrom(other.data()))
        .producerInfo(other.producerInfo())
        .build();
  }

  public ZenCoverSegmentBuilder toBuilder() {
    return new ZenCoverSegmentBuilder(this.locator, this.transactionLocator, this.segmentType)
        .basedOn(this.basedOn)
        .startTime(this.startTime)
        .endTime(this.endTime)
        .duration(this.duration)
        .items(this.items)

        .data(this.data)
        .producerInfo(this.producerInfo)
        .element(this.element);
  }

  // Moratorium Check Implementation
  @Override
  public Map<String, MoratoriumConfig> checkForMoratoriums(DeploymentConfig config) {
    return Map.of();
  }

  public static class ZenCoverSegmentBuilder {
    private final ULID locator;
    private final ULID transactionLocator;
    private final SegmentType segmentType;
    private ZenCoverSegmentBuilder(ULID locator, ULID transactionLocator, SegmentType segmentType) {
      this.locator = locator;
      this.transactionLocator = transactionLocator;
      this.segmentType = segmentType;
    }
    private Optional<ULID> basedOn;
    public ZenCoverSegmentBuilder basedOn(ULID locator) { this.basedOn = Optional.ofNullable(locator); return this; }
    public ZenCoverSegmentBuilder basedOn(Optional<ULID> basedOn) { this.basedOn = basedOn; return this; }
    private Instant startTime;
    public ZenCoverSegmentBuilder startTime(Instant startTime) { this.startTime = startTime; return this; }
    private Instant endTime;
    public ZenCoverSegmentBuilder endTime(Instant endTime) { this.endTime = endTime; return this; }
    private BigDecimal duration;
    public ZenCoverSegmentBuilder duration(BigDecimal duration) { this.duration = duration; return this; }
    private Collection<ItemPolicy> items;
    public ZenCoverSegmentBuilder items(Collection<ItemPolicy> items) { this.items = items; return this; }

    public ZenCoverSegmentBuilder addItem(Consumer<ItemPolicy.ItemPolicyBuilder> mutator) { if (!(this.items instanceof ArrayList)) { this.items = new ArrayList<>(this.items); }; ItemPolicy.ItemPolicyBuilder builder = ItemPolicy.builder(this.element.tenantLocator(), this.element.rootLocator(), this.element.locator(), null, null); mutator.accept(builder); this.items.add(builder.build()); return this; }
    private ZenCoverSegmentData data;
    public ZenCoverSegmentBuilder data(ZenCoverSegmentData data) { this.data = data; return this; }
    private Element element;
    public ZenCoverSegmentBuilder element(Element element) { this.element = element; return this; }
    private Optional<ProducerInfo> producerInfo;
    public ZenCoverSegmentBuilder producerInfo(ProducerInfo producerInfo) { this.producerInfo = Optional.ofNullable(producerInfo); return this; }
    public ZenCoverSegmentBuilder producerInfo(Optional<ProducerInfo> producerInfo) { this.producerInfo = producerInfo; return this; }

    public ZenCoverSegment build() {
      return new ZenCoverSegment(
          this.locator,
          this.transactionLocator,
          this.basedOn,
          this.segmentType,
          this.startTime,
          this.endTime,
          this.duration,
          this.items,
          this.data,
          this.producerInfo,
          this.element
      );
    }
  }

  public record ZenCoverSegmentData(OffsetDateTime contractTermEndDate, Integer coolingOffPeriod, BigDecimal discountAmount, String discountProfileCode, String discountTerm, String discountType, LocalDate expectedRenewalDate, Integer gracePeriod, String gracePeriodType, Integer newBusinessWaitPeriod, Integer settlementPeriod, Integer settlementPeriodOffsetInDays, String settlementPeriodType) implements ZenCoverData, CustomerObject, Validatable<ZenCoverSegmentData> {
    public static final String TYPE = "ZenCoverSegmentData";
    public static final Set<Integer> newBusinessWaitPeriodOptions = Set.of(Integer.valueOf(0),Integer.valueOf(7),Integer.valueOf(14));
    public static final Set<String> discountTypeOptions = Set.of("P","F");

    public ZenCoverSegmentData {
    if (newBusinessWaitPeriod == null) { newBusinessWaitPeriod = Integer.valueOf(0);}
    if (discountAmount != null) { discountAmount = com.socotra.platform.tools.NumberUtils.trimScale(discountAmount, 2, RoundingMode.HALF_EVEN);}
    }

    public String type() { return TYPE; }

    public ZenCoverSegmentData maskData(DataMaskingLevel level) {
      ZenCoverSegmentDataBuilder builder = toBuilder();
      if (level == DataMaskingLevel.level1) {
      }
      if (level == DataMaskingLevel.level2) {
      }
      if (level == DataMaskingLevel.none) {
      }
      return builder.build();
    }

    public ZenCoverSegmentData anonymizeData() {
      ZenCoverSegmentDataBuilder builder = toBuilder();
      return builder.build();
    }

    @Override
    public Collection<ValidationItem> validate(DeploymentConfig config, ValidatableContext context) {
      ValidationItem.ValidationItemBuilder validationItemBuilder = ValidationItem.builder().elementType(TYPE);
      if (coolingOffPeriod == null) { validationItemBuilder.addError("Non optional property 'zenCoverSegmentData.coolingOffPeriod' is missing"); }
      if (discountProfileCode != null && discountProfileCode.length() > 20000) { validationItemBuilder.addError("'zenCoverSegmentData.discountProfileCode' length is more than max length of 20000"); }
      if (discountTerm != null && discountTerm.length() > 20000) { validationItemBuilder.addError("'zenCoverSegmentData.discountTerm' length is more than max length of 20000"); }
      if (discountType != null && discountType.length() > 20000) { validationItemBuilder.addError("'zenCoverSegmentData.discountType' length is more than max length of 20000"); }
      if (discountType != null && !discountTypeOptions.contains(discountType)) { validationItemBuilder.addError("Property 'zenCoverSegmentData.discountType' should be one of " + discountTypeOptions); }
      if (gracePeriodType != null && gracePeriodType.length() > 20000) { validationItemBuilder.addError("'zenCoverSegmentData.gracePeriodType' length is more than max length of 20000"); }
      if (newBusinessWaitPeriod != null && !newBusinessWaitPeriodOptions.contains(newBusinessWaitPeriod)) { validationItemBuilder.addError("Property 'zenCoverSegmentData.newBusinessWaitPeriod' should be one of " + newBusinessWaitPeriodOptions); }
      if (settlementPeriodType != null && settlementPeriodType.length() > 20000) { validationItemBuilder.addError("'zenCoverSegmentData.settlementPeriodType' length is more than max length of 20000"); }

      return validationItemBuilder.hasErrors() ? List.of(validationItemBuilder.build()) : List.of();
    }

    @Override
    public ZenCoverSegmentData correct(DeploymentConfig config, ValidationErrorResolver resolver) {
      ZenCoverSegmentDataBuilder builder = this.toBuilder();
      Map<String, java.lang.reflect.RecordComponent> fields = Arrays.stream(this.getClass().getRecordComponents()).collect(java.util.stream.Collectors.toMap(java.lang.reflect.RecordComponent::getName, c -> c));
      resolver.correct(this, fields.get("coolingOffPeriod"), builder::coolingOffPeriod, new Required<>(coolingOffPeriod));
      resolver.correct(this, fields.get("discountProfileCode"), builder::discountProfileCode, new MaxLength(20000));
      resolver.correct(this, fields.get("discountTerm"), builder::discountTerm, new MaxLength(20000));
      resolver.correct(this, fields.get("discountType"), builder::discountType, new MaxLength(20000), new Options<>(discountTypeOptions));
      resolver.correct(this, fields.get("gracePeriodType"), builder::gracePeriodType, new MaxLength(20000));
      resolver.correct(this, fields.get("newBusinessWaitPeriod"), builder::newBusinessWaitPeriod, new Options<>(newBusinessWaitPeriodOptions));
      resolver.correct(this, fields.get("settlementPeriodType"), builder::settlementPeriodType, new MaxLength(20000));

      return builder.build();
    }

    @Override
    public ZenCoverSegmentData applyAvailabilityRemovals(Instant referenceDate) {
      ZenCoverSegmentDataBuilder builder = toBuilder();

      return builder.build();
    }

    public static ZenCoverSegmentDataBuilder builder() {
      return new ZenCoverSegmentDataBuilder();
    }
    public ZenCoverSegmentDataBuilder toBuilder() {
      return new ZenCoverSegmentDataBuilder()
      .contractTermEndDate(this.contractTermEndDate)
      .coolingOffPeriod(this.coolingOffPeriod)
      .discountAmount(this.discountAmount)
      .discountProfileCode(this.discountProfileCode)
      .discountTerm(this.discountTerm)
      .discountType(this.discountType)
      .expectedRenewalDate(this.expectedRenewalDate)
      .gracePeriod(this.gracePeriod)
      .gracePeriodType(this.gracePeriodType)
      .newBusinessWaitPeriod(this.newBusinessWaitPeriod)
      .settlementPeriod(this.settlementPeriod)
      .settlementPeriodOffsetInDays(this.settlementPeriodOffsetInDays)
      .settlementPeriodType(this.settlementPeriodType);
    }

    public static class ZenCoverSegmentDataBuilder {
      private OffsetDateTime contractTermEndDate;
      public ZenCoverSegmentDataBuilder contractTermEndDate(OffsetDateTime contractTermEndDate) { this.contractTermEndDate = contractTermEndDate; return this; }
      private Integer coolingOffPeriod;
      public ZenCoverSegmentDataBuilder coolingOffPeriod(Integer coolingOffPeriod) { this.coolingOffPeriod = coolingOffPeriod; return this; }
      private BigDecimal discountAmount;
      public ZenCoverSegmentDataBuilder discountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; return this; }
      private String discountProfileCode;
      public ZenCoverSegmentDataBuilder discountProfileCode(String discountProfileCode) { this.discountProfileCode = discountProfileCode; return this; }
      private String discountTerm;
      public ZenCoverSegmentDataBuilder discountTerm(String discountTerm) { this.discountTerm = discountTerm; return this; }
      private String discountType;
      public ZenCoverSegmentDataBuilder discountType(String discountType) { this.discountType = discountType; return this; }
      private LocalDate expectedRenewalDate;
      public ZenCoverSegmentDataBuilder expectedRenewalDate(LocalDate expectedRenewalDate) { this.expectedRenewalDate = expectedRenewalDate; return this; }
      private Integer gracePeriod;
      public ZenCoverSegmentDataBuilder gracePeriod(Integer gracePeriod) { this.gracePeriod = gracePeriod; return this; }
      private String gracePeriodType;
      public ZenCoverSegmentDataBuilder gracePeriodType(String gracePeriodType) { this.gracePeriodType = gracePeriodType; return this; }
      private Integer newBusinessWaitPeriod;
      public ZenCoverSegmentDataBuilder newBusinessWaitPeriod(Integer newBusinessWaitPeriod) { this.newBusinessWaitPeriod = newBusinessWaitPeriod; return this; }
      private Integer settlementPeriod;
      public ZenCoverSegmentDataBuilder settlementPeriod(Integer settlementPeriod) { this.settlementPeriod = settlementPeriod; return this; }
      private Integer settlementPeriodOffsetInDays;
      public ZenCoverSegmentDataBuilder settlementPeriodOffsetInDays(Integer settlementPeriodOffsetInDays) { this.settlementPeriodOffsetInDays = settlementPeriodOffsetInDays; return this; }
      private String settlementPeriodType;
      public ZenCoverSegmentDataBuilder settlementPeriodType(String settlementPeriodType) { this.settlementPeriodType = settlementPeriodType; return this; }

      public ZenCoverSegmentData build() {
        return new ZenCoverSegmentData(contractTermEndDate, coolingOffPeriod, discountAmount, discountProfileCode, discountTerm, discountType, expectedRenewalDate, gracePeriod, gracePeriodType, newBusinessWaitPeriod, settlementPeriod, settlementPeriodOffsetInDays, settlementPeriodType);
      }
    }

    public static ZenCoverSegmentData copyFrom(ZenCoverQuote.ZenCoverQuoteData other) {
      if (other == null) {
        return null;
      }
      return new ZenCoverSegmentData(
          other.contractTermEndDate(),
          other.coolingOffPeriod(),
          other.discountAmount(),
          other.discountProfileCode(),
          other.discountTerm(),
          other.discountType(),
          other.expectedRenewalDate(),
          other.gracePeriod(),
          other.gracePeriodType(),
          other.newBusinessWaitPeriod(),
          other.settlementPeriod(),
          other.settlementPeriodOffsetInDays(),
          other.settlementPeriodType()
      );
    }

    public static ZenCoverSegmentData copyFrom(ZenCoverSegmentData other) {
      if (other == null) {
        return null;
      }
      return new ZenCoverSegmentData(
          other.contractTermEndDate(),
          other.coolingOffPeriod(),
          other.discountAmount(),
          other.discountProfileCode(),
          other.discountTerm(),
          other.discountType(),
          other.expectedRenewalDate(),
          other.gracePeriod(),
          other.gracePeriodType(),
          other.newBusinessWaitPeriod(),
          other.settlementPeriod(),
          other.settlementPeriodOffsetInDays(),
          other.settlementPeriodType()
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