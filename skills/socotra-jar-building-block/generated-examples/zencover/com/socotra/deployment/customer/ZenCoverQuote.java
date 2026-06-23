package com.socotra.deployment.customer;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.type.*;
import com.socotra.platform.tools.ULID;
import com.socotra.coremodel.*;
import com.socotra.coremodel.constraints.*;
import com.socotra.coremodel.views.Internal;
import com.socotra.deployment.*;

import java.math.*;
import java.time.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.*;
import java.util.stream.Collectors;

public record ZenCoverQuote(
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
    Optional<String> region,
    Optional<String> jurisdiction,
    Optional<String> producerCode,
    Collection<ItemQuote> items,
    ZenCoverQuoteData data,
    @JsonView({Internal.class}) Element element,
    BillingLevel billingLevel,
    Optional<String> quoteNumber,
    Collection<ContactRoles> contacts,
    Optional<BigDecimal> invoiceFeeAmount,
    Optional<String> reservedPolicyNumber) implements ZenCover, Validatable<ZenCoverQuote>, com.socotra.coremodel.interfaces.Quote, Elemental, CustomerObject, SensitiveDataHolder<ZenCoverQuote.ZenCoverQuoteData>, ContactsHolder, NumberingTriggerHolder, MoratoriumCheck {

  public static final String TYPE = "ZenCoverQuote";
  private static final Map<String, ContactSlot> CONTACT_SLOTS = Map.ofEntries(

  );

  public Map<String, ContactSlot> contactSlots() { return CONTACT_SLOTS; }

  public NumberingTrigger numberingTrigger() {
    return NumberingTrigger.validation;
  }

  public ZenCoverQuote {
    items = items == null ? List.of() : List.copyOf(items);
    if(data == null) {
      data = ZenCoverQuoteData.builder().build();
    }
    contacts = contacts == null ? List.of() : contacts;
    invoiceFeeAmount = invoiceFeeAmount == null ? Optional.empty() : invoiceFeeAmount;
  }

  public String type() { return TYPE; }

  public ZenCoverQuote maskData(DataMaskingLevel level) {
    ZenCoverQuoteBuilder builder = toBuilder();
    builder.data(this.data.maskData(level));
    if (this.items != null) {
      builder.items(this.items.stream().map(x -> x.maskData(level)).toList());
    }
    return builder.build();
  }

  public ZenCoverQuote anonymizeData() {
    ZenCoverQuoteBuilder builder = toBuilder();
    builder.data(this.data.anonymizeData());
    if (this.items != null) {
      builder.items(this.items.stream().map(x -> x.anonymizeData()).toList());
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
    this.region.filter(r -> !config.getRegions().contains(r)).ifPresent(r -> validationItemBuilder.addError("'region' should be one of " + config.getRegions()));
    config.getProduct(productName()).ifPresent(product -> product.validateJurisdiction(config, this.jurisdiction.orElse(null), validationItemBuilder::addError));
    if (this.items == null || this.items.isEmpty()) {
      validationItemBuilder.addError("'items' should have at least one element");
    } else {
      this.items.forEach(c -> validationItems.addAll(c.validate(config, context)));
    }

    this.data.validate(config, context).forEach(error -> error.errors().forEach(validationItemBuilder::addError));

    validationItemBuilder.addErrors(validateContacts());
    if (validationItemBuilder.hasErrors()) {
      validationItems.add(validationItemBuilder.build());
    }
    return validationItems;
  }

  @Override
  public ZenCoverQuote correct(DeploymentConfig config, ValidationErrorResolver resolver) {
    ZenCoverQuoteBuilder builder = toBuilder();
    Map<String, java.lang.reflect.RecordComponent> fields = Arrays.stream(this.getClass().getRecordComponents()).collect(Collectors.toMap(java.lang.reflect.RecordComponent::getName, c -> c));
    if(items != null) {
      builder.items(items.stream().map(v -> v == null ? null : v.correct(config, resolver)).filter(v -> v != null).toList());
    } else {
      resolver.correct(this, fields.get("items"), builder::items, new Required<>(items));
    }
    builder.data(this.data.correct(config, resolver));

    return builder.build();
  }

  public ZenCoverQuote applyDefaults(DeploymentConfig config) {
    ZenCoverQuoteBuilder builder = toBuilder();
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

    if (this.items != null) { builder.items(this.items.stream().map(e -> e.applyDefaults(config)).toList()); }

    return builder.build();
  }

  public ZenCoverQuote applyAvailabilityRemovals(Instant referenceDate) {
    ZenCoverQuoteBuilder builder = toBuilder();
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

  public static ZenCoverQuote from(DeploymentFactory factory, com.socotra.coremodel.interfaces.Quote quote) {
    if (quote == null) {
      return null;
    }
    Element element = quote.element();
    return new ZenCoverQuote(
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
        quote.region(),
        quote.jurisdiction(),
        quote.producerCode(),
        element.elements().stream().filter(e -> e.type().equalsIgnoreCase(ItemQuote.TYPE)).map(e -> ItemQuote.fromElement(factory, e)).toList(),
        element.resolve(ZenCoverQuoteData.TYPE, factory),
        element,
        quote.billingLevel(),
        quote.quoteNumber(),
        quote.contacts(),
        quote.invoiceFeeAmount(),
        quote.reservedPolicyNumber()
    );
  }

  public static ZenCoverQuoteBuilder builder() {
    return new ZenCoverQuoteBuilder(QuoteState.draft, Optional.empty(), Optional.empty());
  }

  public ZenCoverQuoteBuilder toBuilder() {
    return new ZenCoverQuoteBuilder(this.quoteState, this.createdAt, this.createdBy)
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
        .region(this.region)
        .jurisdiction(this.jurisdiction)
        .producerCode(this.producerCode)
        .items(this.items)

        .data(this.data)
        .element(this.element)
        .billingLevel(this.billingLevel)
        .quoteNumber(this.quoteNumber)
        .contacts(this.contacts)
        .invoiceFeeAmount(this.invoiceFeeAmount)
        .reservedPolicyNumber(this.reservedPolicyNumber);
  }

  // Moratorium Check Implementation
  @Override
  public Map<String, MoratoriumConfig> checkForMoratoriums(DeploymentConfig config) {
    return Map.of();
  }

  public static class ZenCoverQuoteBuilder {
    private final QuoteState quoteState;
    private Optional<UUID> createdBy;
    private Optional<Instant> createdAt;
    private ZenCoverQuoteBuilder(QuoteState quoteState, Optional<Instant> createdAt, Optional<UUID> createdBy) {
      this.quoteState = quoteState;
      this.createdAt = createdAt;
      this.createdBy = createdBy;
    }
    private Optional<BigDecimal> invoiceFeeAmount;
    public ZenCoverQuoteBuilder invoiceFeeAmount(Optional<BigDecimal> invoiceFeeAmount) { this.invoiceFeeAmount = invoiceFeeAmount; return this; }
    private Collection<ContactRoles> contacts;
    public ZenCoverQuoteBuilder contacts(Collection<ContactRoles> contacts) { this.contacts = contacts; return this; }
    public ZenCoverQuoteBuilder addContact(ContactRoles contact) { if (!(this.contacts instanceof ArrayList)) { this.contacts = new ArrayList<>(this.contacts); }; this.contacts.add(contact); return this; }
    private ULID locator;
    public ZenCoverQuoteBuilder locator(ULID locator) { this.locator = locator; return this; }
    private ULID groupLocator;
    public ZenCoverQuoteBuilder groupLocator(ULID groupLocator) { this.groupLocator = groupLocator; return this; }
    private String productName;
    public ZenCoverQuoteBuilder productName(String productName) { this.productName = productName; return this; }
    private ULID accountLocator;
    public ZenCoverQuoteBuilder accountLocator(ULID accountLocator) { this.accountLocator = accountLocator; return this; }
    private Optional<Instant> startTime;
    public ZenCoverQuoteBuilder startTime(Instant startTime) { this.startTime = Optional.ofNullable(startTime); return this; }
    public ZenCoverQuoteBuilder startTime(Optional<Instant> startTime) { this.startTime = startTime; return this; }
    private Optional<Instant> endTime;
    public ZenCoverQuoteBuilder endTime(Instant endTime) { this.endTime = Optional.ofNullable(endTime); return this; }
    public ZenCoverQuoteBuilder endTime(Optional<Instant> endTime) { this.endTime = endTime; return this; }
    private Optional<String> timezone;
    public ZenCoverQuoteBuilder timezone(String timezone) { this.timezone = Optional.ofNullable(timezone); return this; }
    public ZenCoverQuoteBuilder timezone(Optional<String> timezone) { this.timezone = timezone; return this; }
    private Optional<String> currency;
    public ZenCoverQuoteBuilder currency(String currency) { this.currency = Optional.ofNullable(currency); return this; }
    public ZenCoverQuoteBuilder currency(Optional<String> currency) { this.currency = currency; return this; }
    private Optional<String> underwritingStatus;
    public ZenCoverQuoteBuilder underwritingStatus(String underwritingStatus) { this.underwritingStatus = Optional.ofNullable(underwritingStatus); return this; }
    public ZenCoverQuoteBuilder underwritingStatus(Optional<String> underwritingStatus) { this.underwritingStatus = underwritingStatus; return this; }
    private Optional<Instant> expirationTime;
    public ZenCoverQuoteBuilder expirationTime(Instant expirationTime) { this.expirationTime = Optional.ofNullable(expirationTime); return this; }
    public ZenCoverQuoteBuilder expirationTime(Optional<Instant> expirationTime) { this.expirationTime = expirationTime; return this; }
    private Optional<Preferences> preferences;
    public ZenCoverQuoteBuilder preferences(Preferences preferences) { this.preferences = Optional.ofNullable(preferences); return this; }
    public ZenCoverQuoteBuilder preferences(Optional<Preferences> preferences) { this.preferences = preferences; return this; }
    private Optional<ULID> policyLocator;
    public ZenCoverQuoteBuilder policyLocator(ULID policyLocator) { this.policyLocator = Optional.ofNullable(policyLocator); return this; }
    public ZenCoverQuoteBuilder policyLocator(Optional<ULID> policyLocator) { this.policyLocator = policyLocator; return this; }
    private Optional<DurationBasis> durationBasis;
    public ZenCoverQuoteBuilder durationBasis(DurationBasis durationBasis) { this.durationBasis = Optional.ofNullable(durationBasis); return this; }
    public ZenCoverQuoteBuilder durationBasis(Optional<DurationBasis> durationBasis) { this.durationBasis = durationBasis; return this; }
    private Optional<String> delinquencyPlanName;
    public ZenCoverQuoteBuilder delinquencyPlanName(String delinquencyPlanName) { this.delinquencyPlanName = Optional.ofNullable(delinquencyPlanName); return this; }
    public ZenCoverQuoteBuilder delinquencyPlanName(Optional<String> delinquencyPlanName) { this.delinquencyPlanName = delinquencyPlanName; return this; }
    private Optional<String> autoRenewalPlanName;
    public ZenCoverQuoteBuilder autoRenewalPlanName(String autoRenewalPlanName) { this.autoRenewalPlanName = Optional.ofNullable(autoRenewalPlanName); return this; }
    public ZenCoverQuoteBuilder autoRenewalPlanName(Optional<String> autoRenewalPlanName) { this.autoRenewalPlanName = autoRenewalPlanName; return this; }
    private Optional<String> region;
    public ZenCoverQuoteBuilder region(Optional<String> region) { this.region = region; return this; }
    public ZenCoverQuoteBuilder region(String region) { this.region = Optional.ofNullable(region); return this; }
    private Optional<String> jurisdiction;
    public ZenCoverQuoteBuilder jurisdiction(Optional<String> jurisdiction) { this.jurisdiction = jurisdiction; return this; }
    public ZenCoverQuoteBuilder jurisdiction(String jurisdiction) { this.jurisdiction = Optional.ofNullable(jurisdiction); return this; }
    private Optional<String> producerCode;
    public ZenCoverQuoteBuilder producerCode(Optional<String> producerCode) { this.producerCode = producerCode; return this; }
    public ZenCoverQuoteBuilder producerCode(String producerCode) { this.producerCode = Optional.ofNullable(producerCode); return this; }
    private Collection<ItemQuote> items;
    public ZenCoverQuoteBuilder items(Collection<ItemQuote> items) { this.items = items; return this; }
    public ZenCoverQuoteBuilder addItem(Consumer<ItemQuote.ItemQuoteBuilder> mutator) { if (!(this.items instanceof ArrayList)) { this.items = new ArrayList<>(this.items); }; ItemQuote.ItemQuoteBuilder builder = ItemQuote.builder(this.element.tenantLocator(), this.element.rootLocator(), this.element.locator(), null, null); mutator.accept(builder); this.items.add(builder.build()); return this; }
    private ZenCoverQuoteData data;
    public ZenCoverQuoteBuilder data(ZenCoverQuoteData data) { this.data = data; return this; }
    private Element element;
    public ZenCoverQuoteBuilder element(Element element) { this.element = element; return this; }
    private BillingLevel billingLevel;
    public ZenCoverQuoteBuilder billingLevel(BillingLevel billingLevel) { this.billingLevel = billingLevel; return this; }
    private Optional<String> quoteNumber;
    public ZenCoverQuoteBuilder quoteNumber(Optional<String> quoteNumber) { this.quoteNumber = quoteNumber; return this; }
    public ZenCoverQuoteBuilder quoteNumber(String quoteNumber) { this.quoteNumber = Optional.ofNullable(quoteNumber); return this; }
    private Optional<String> reservedPolicyNumber;
    public ZenCoverQuoteBuilder reservedPolicyNumber(Optional<String> reservedPolicyNumber) { this.reservedPolicyNumber = reservedPolicyNumber; return this; }
    public ZenCoverQuoteBuilder reservedPolicyNumber(String reservedPolicyNumber) { this.reservedPolicyNumber = Optional.ofNullable(reservedPolicyNumber); return this; }
    public ZenCoverQuote build() {
      return new ZenCoverQuote(
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
          this.region,
          this.jurisdiction,
          this.producerCode,
          this.items,
          this.data,
          this.element,
          this.billingLevel,
          this.quoteNumber,
          this.contacts,
          this.invoiceFeeAmount,
          this.reservedPolicyNumber
      );
    }
  }

  public record ZenCoverQuoteData(OffsetDateTime contractTermEndDate, Integer coolingOffPeriod, BigDecimal discountAmount, String discountProfileCode, String discountTerm, String discountType, LocalDate expectedRenewalDate, Integer gracePeriod, String gracePeriodType, Integer newBusinessWaitPeriod, Integer settlementPeriod, Integer settlementPeriodOffsetInDays, String settlementPeriodType) implements ZenCoverData, CustomerObject, Validatable<ZenCoverQuoteData> {
    public static final String TYPE = "ZenCoverQuoteData";
    public static final Set<Integer> newBusinessWaitPeriodOptions = Set.of(Integer.valueOf(0),Integer.valueOf(7),Integer.valueOf(14));
    public static final Set<String> discountTypeOptions = Set.of("P","F");

    public ZenCoverQuoteData {
    if (newBusinessWaitPeriod == null) { newBusinessWaitPeriod = Integer.valueOf(0);}
    if (discountAmount != null) { discountAmount = com.socotra.platform.tools.NumberUtils.trimScale(discountAmount, 2, RoundingMode.HALF_EVEN);}
    }

    public String type() { return TYPE; }

    public ZenCoverQuoteData maskData(DataMaskingLevel level) {
      ZenCoverQuoteDataBuilder builder = toBuilder();
      if (level == DataMaskingLevel.level1) {
      }
      if (level == DataMaskingLevel.level2) {
      }
      if (level == DataMaskingLevel.none) {
      }
      return builder.build();
    }

    public ZenCoverQuoteData anonymizeData() {
      ZenCoverQuoteDataBuilder builder = toBuilder();
      return builder.build();
    }

    @Override
    public Collection<ValidationItem> validate(DeploymentConfig config, ValidatableContext context) {
      ValidationItem.ValidationItemBuilder validationItemBuilder = ValidationItem.builder().elementType(TYPE);
      if (coolingOffPeriod == null) { validationItemBuilder.addError("Non optional property 'zenCoverQuoteData.coolingOffPeriod' is missing"); }
      if (discountProfileCode != null && discountProfileCode.length() > 20000) { validationItemBuilder.addError("'zenCoverQuoteData.discountProfileCode' length is more than max length of 20000"); }
      if (discountTerm != null && discountTerm.length() > 20000) { validationItemBuilder.addError("'zenCoverQuoteData.discountTerm' length is more than max length of 20000"); }
      if (discountType != null && discountType.length() > 20000) { validationItemBuilder.addError("'zenCoverQuoteData.discountType' length is more than max length of 20000"); }
      if (discountType != null && !discountTypeOptions.contains(discountType)) { validationItemBuilder.addError("Property 'zenCoverQuoteData.discountType' should be one of " + discountTypeOptions); }
      if (gracePeriodType != null && gracePeriodType.length() > 20000) { validationItemBuilder.addError("'zenCoverQuoteData.gracePeriodType' length is more than max length of 20000"); }
      if (newBusinessWaitPeriod != null && !newBusinessWaitPeriodOptions.contains(newBusinessWaitPeriod)) { validationItemBuilder.addError("Property 'zenCoverQuoteData.newBusinessWaitPeriod' should be one of " + newBusinessWaitPeriodOptions); }
      if (settlementPeriodType != null && settlementPeriodType.length() > 20000) { validationItemBuilder.addError("'zenCoverQuoteData.settlementPeriodType' length is more than max length of 20000"); }

      return validationItemBuilder.hasErrors() ? List.of(validationItemBuilder.build()) : List.of();
    }

    @Override
    public ZenCoverQuoteData correct(DeploymentConfig config, ValidationErrorResolver resolver) {
      ZenCoverQuoteDataBuilder builder = this.toBuilder();
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
    public ZenCoverQuoteData applyAvailabilityRemovals(Instant referenceDate) {
      ZenCoverQuoteDataBuilder builder = toBuilder();

      return builder.build();
    }

    public static ZenCoverQuoteDataBuilder builder() {
      return new ZenCoverQuoteDataBuilder();
    }
    public ZenCoverQuoteDataBuilder toBuilder() {
      return new ZenCoverQuoteDataBuilder()
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

    public static class ZenCoverQuoteDataBuilder {
      private OffsetDateTime contractTermEndDate;
      public ZenCoverQuoteDataBuilder contractTermEndDate(OffsetDateTime contractTermEndDate) { this.contractTermEndDate = contractTermEndDate; return this; }
      private Integer coolingOffPeriod;
      public ZenCoverQuoteDataBuilder coolingOffPeriod(Integer coolingOffPeriod) { this.coolingOffPeriod = coolingOffPeriod; return this; }
      private BigDecimal discountAmount;
      public ZenCoverQuoteDataBuilder discountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; return this; }
      private String discountProfileCode;
      public ZenCoverQuoteDataBuilder discountProfileCode(String discountProfileCode) { this.discountProfileCode = discountProfileCode; return this; }
      private String discountTerm;
      public ZenCoverQuoteDataBuilder discountTerm(String discountTerm) { this.discountTerm = discountTerm; return this; }
      private String discountType;
      public ZenCoverQuoteDataBuilder discountType(String discountType) { this.discountType = discountType; return this; }
      private LocalDate expectedRenewalDate;
      public ZenCoverQuoteDataBuilder expectedRenewalDate(LocalDate expectedRenewalDate) { this.expectedRenewalDate = expectedRenewalDate; return this; }
      private Integer gracePeriod;
      public ZenCoverQuoteDataBuilder gracePeriod(Integer gracePeriod) { this.gracePeriod = gracePeriod; return this; }
      private String gracePeriodType;
      public ZenCoverQuoteDataBuilder gracePeriodType(String gracePeriodType) { this.gracePeriodType = gracePeriodType; return this; }
      private Integer newBusinessWaitPeriod;
      public ZenCoverQuoteDataBuilder newBusinessWaitPeriod(Integer newBusinessWaitPeriod) { this.newBusinessWaitPeriod = newBusinessWaitPeriod; return this; }
      private Integer settlementPeriod;
      public ZenCoverQuoteDataBuilder settlementPeriod(Integer settlementPeriod) { this.settlementPeriod = settlementPeriod; return this; }
      private Integer settlementPeriodOffsetInDays;
      public ZenCoverQuoteDataBuilder settlementPeriodOffsetInDays(Integer settlementPeriodOffsetInDays) { this.settlementPeriodOffsetInDays = settlementPeriodOffsetInDays; return this; }
      private String settlementPeriodType;
      public ZenCoverQuoteDataBuilder settlementPeriodType(String settlementPeriodType) { this.settlementPeriodType = settlementPeriodType; return this; }

      public ZenCoverQuoteData build() {
        return new ZenCoverQuoteData(contractTermEndDate, coolingOffPeriod, discountAmount, discountProfileCode, discountTerm, discountType, expectedRenewalDate, gracePeriod, gracePeriodType, newBusinessWaitPeriod, settlementPeriod, settlementPeriodOffsetInDays, settlementPeriodType);
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