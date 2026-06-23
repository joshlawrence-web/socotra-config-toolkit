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
import java.util.regex.*;
import java.util.stream.Collectors;

public record ZenCoverQuickQuote(
    ULID locator,
    QuickQuoteState quickQuoteState,
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
    Optional<String> jurisdiction,
    Collection<ItemQuickQuote> items,
    ZenCoverQuickQuoteData data,
    @JsonView({Internal.class}) Element element,
    Collection<ContactRoles> contacts
  ) implements ZenCover, ContactsHolder, Validatable<ZenCoverQuickQuote>, com.socotra.coremodel.interfaces.QuickQuote, Elemental, CustomerObject, SensitiveDataHolder<ZenCoverQuickQuote.ZenCoverQuickQuoteData> {

  public static final String TYPE = "ZenCoverQuickQuote";
  private static final Map<String, ContactSlot> CONTACT_SLOTS = Map.ofEntries(

  );
  public Map<String, ContactSlot> contactSlots() { return CONTACT_SLOTS; }

  public ZenCoverQuickQuote {
    items = items == null ? List.of() : List.copyOf(items);
    if(data == null) {
      data = ZenCoverQuickQuoteData.builder().build();
    }
      contacts = contacts == null ? List.of() : contacts;
  }

  public String type() { return TYPE; }

  public ZenCoverQuickQuote maskData(DataMaskingLevel level) {
    ZenCoverQuickQuoteBuilder builder = toBuilder();
    builder.data(this.data.maskData(level));
    if (this.items != null) {
      builder.items(this.items.stream().map(x -> x.maskData(level)).toList());
    }
    return builder.build();
  }

  public ZenCoverQuickQuote anonymizeData() {
    ZenCoverQuickQuoteBuilder builder = toBuilder();
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
    config.getProduct(this.productName).ifPresent(product -> product.validateJurisdiction(config, this.jurisdiction.orElse(null), validationItemBuilder::addError));
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
  public ZenCoverQuickQuote correct(DeploymentConfig config, ValidationErrorResolver resolver) {
    ZenCoverQuickQuoteBuilder builder = toBuilder();
    Map<String, java.lang.reflect.RecordComponent> fields = Arrays.stream(this.getClass().getRecordComponents()).collect(Collectors.toMap(java.lang.reflect.RecordComponent::getName, c -> c));
    if(items != null) {
      builder.items(items.stream().map(v -> v == null ? null : v.correct(config, resolver)).filter(v -> v != null).toList());
    } else {
      resolver.correct(this, fields.get("items"), builder::items, new Required<>(items));
    }
    builder.data(this.data.correct(config, resolver));

    return builder.build();
  }

  public ZenCoverQuickQuote applyDefaults(DeploymentConfig config) {
    ZenCoverQuickQuoteBuilder builder = toBuilder();
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

    if (this.items != null) { builder.items(this.items.stream().map(e -> e.applyDefaults(config)).toList()); }

    return builder.build();
  }

  public ZenCoverQuickQuote applyAvailabilityRemovals(Instant referenceDate) {
    ZenCoverQuickQuoteBuilder builder = toBuilder();
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

  public static ZenCoverQuickQuote from(DeploymentFactory factory, com.socotra.coremodel.interfaces.QuickQuote quote) {
    if (quote == null) {
      return null;
    }
    Element element = quote.element();
    return new ZenCoverQuickQuote(
        quote.locator(),
        quote.quickQuoteState(),
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
        quote.jurisdiction(),
        element.elements().stream().filter(e -> e.type().equalsIgnoreCase(ItemQuickQuote.TYPE)).map(e -> ItemQuickQuote.fromElement(factory, e)).toList(),
        element.resolve(ZenCoverQuickQuoteData.TYPE, factory),
        element,
        quote.contacts()
    );
  }

  public static ZenCoverQuickQuoteBuilder builder() {
    return new ZenCoverQuickQuoteBuilder(QuickQuoteState.draft, Optional.empty(), Optional.empty());
  }

  public ZenCoverQuickQuoteBuilder toBuilder() {
    return new ZenCoverQuickQuoteBuilder(this.quickQuoteState, this.createdAt, this.createdBy)
        .locator(this.locator)
        .productName(this.productName)
        .accountLocator(this.accountLocator)
        .startTime(this.startTime)
        .endTime(this.endTime)
        .timezone(this.timezone)
        .currency(this.currency)
        .expirationTime(this.expirationTime)
        .durationBasis(this.durationBasis)
        .jurisdiction(this.jurisdiction)
        .items(this.items)

        .data(this.data)
        .element(this.element)
        .contacts(this.contacts);
  }

  public static class ZenCoverQuickQuoteBuilder {
    private final QuickQuoteState quickQuoteState;
    private Optional<Instant> createdAt;
    private Optional<UUID> createdBy;
    private ZenCoverQuickQuoteBuilder(QuickQuoteState quickQuoteState,Optional<Instant> createdAt, Optional<UUID> createdBy) {
      this.quickQuoteState = quickQuoteState;
    }
    private Collection<ContactRoles> contacts;
    public ZenCoverQuickQuoteBuilder contacts(Collection<ContactRoles> contacts) { this.contacts = contacts; return this; }
    public ZenCoverQuickQuoteBuilder addContact(ContactRoles contact) { if (!(this.contacts instanceof ArrayList)) { this.contacts = new ArrayList<>(this.contacts); }; this.contacts.add(contact); return this; }
    private ULID locator;
    public ZenCoverQuickQuoteBuilder locator(ULID locator) { this.locator = locator; return this; }
    private String productName;
    public ZenCoverQuickQuoteBuilder productName(String productName) { this.productName = productName; return this; }
    private Optional<ULID> accountLocator;
    public ZenCoverQuickQuoteBuilder accountLocator(ULID accountLocator) { this.accountLocator = Optional.ofNullable(accountLocator); return this; }
    public ZenCoverQuickQuoteBuilder accountLocator(Optional<ULID> accountLocator) { this.accountLocator = accountLocator; return this; }
    private Optional<Instant> startTime;
    public ZenCoverQuickQuoteBuilder startTime(Instant startTime) { this.startTime = Optional.ofNullable(startTime); return this; }
    public ZenCoverQuickQuoteBuilder startTime(Optional<Instant> startTime) { this.startTime = startTime; return this; }
    private Optional<Instant> endTime;
    public ZenCoverQuickQuoteBuilder endTime(Instant endTime) { this.endTime = Optional.ofNullable(endTime); return this; }
    public ZenCoverQuickQuoteBuilder endTime(Optional<Instant> endTime) { this.endTime = endTime; return this; }
    private Optional<String> timezone;
    public ZenCoverQuickQuoteBuilder timezone(String timezone) { this.timezone = Optional.ofNullable(timezone); return this; }
    public ZenCoverQuickQuoteBuilder timezone(Optional<String> timezone) { this.timezone = timezone; return this; }
    private Optional<String> currency;
    public ZenCoverQuickQuoteBuilder currency(String currency) { this.currency = Optional.ofNullable(currency); return this; }
    public ZenCoverQuickQuoteBuilder currency(Optional<String> currency) { this.currency = currency; return this; }
    private Optional<Instant> expirationTime;
    public ZenCoverQuickQuoteBuilder expirationTime(Instant expirationTime) { this.expirationTime = Optional.ofNullable(expirationTime); return this; }
    public ZenCoverQuickQuoteBuilder expirationTime(Optional<Instant> expirationTime) { this.expirationTime = expirationTime; return this; }
    private Optional<DurationBasis> durationBasis;
    public ZenCoverQuickQuoteBuilder durationBasis(DurationBasis durationBasis) { this.durationBasis = Optional.ofNullable(durationBasis); return this; }
    public ZenCoverQuickQuoteBuilder durationBasis(Optional<DurationBasis> durationBasis) { this.durationBasis = durationBasis; return this; }
    private Optional<String> jurisdiction;
    public ZenCoverQuickQuoteBuilder jurisdiction(String jurisdiction) { this.jurisdiction = Optional.ofNullable(jurisdiction); return this; }
    public ZenCoverQuickQuoteBuilder jurisdiction(Optional<String> jurisdiction) { this.jurisdiction = jurisdiction; return this; }
    private Collection<ItemQuickQuote> items;
    public ZenCoverQuickQuoteBuilder items(Collection<ItemQuickQuote> items) { this.items = items; return this; }

    public ZenCoverQuickQuoteBuilder addItem(Consumer<ItemQuickQuote.ItemQuickQuoteBuilder> mutator) { if (!(this.items instanceof ArrayList)) { this.items = new ArrayList<>(this.items); }; ItemQuickQuote.ItemQuickQuoteBuilder builder = ItemQuickQuote.builder(this.element.tenantLocator(), this.element.rootLocator(), this.element.locator(), null, null); mutator.accept(builder); this.items.add(builder.build()); return this; }
    private ZenCoverQuickQuoteData data;
    public ZenCoverQuickQuoteBuilder data(ZenCoverQuickQuoteData data) { this.data = data; return this; }
    private Element element;
    public ZenCoverQuickQuoteBuilder element(Element element) { this.element = element; return this; }
    public ZenCoverQuickQuote build() {
      return new ZenCoverQuickQuote(
          this.locator,
          this.quickQuoteState,
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
          this.jurisdiction,
          this.items,
          this.data,
          this.element,
          this.contacts
      );
    }
  }

  public record ZenCoverQuickQuoteData(OffsetDateTime contractTermEndDate, Integer coolingOffPeriod, BigDecimal discountAmount, String discountProfileCode, String discountTerm, String discountType, LocalDate expectedRenewalDate, Integer gracePeriod, String gracePeriodType, Integer newBusinessWaitPeriod, Integer settlementPeriod, Integer settlementPeriodOffsetInDays, String settlementPeriodType) implements ZenCoverData, CustomerObject, Validatable<ZenCoverQuickQuoteData> {
    public static final String TYPE = "ZenCoverQuickQuoteData";
    public static final Set<Integer> newBusinessWaitPeriodOptions = Set.of(Integer.valueOf(0),Integer.valueOf(7),Integer.valueOf(14));
    public static final Set<String> discountTypeOptions = Set.of("P","F");

    public ZenCoverQuickQuoteData {
    if (newBusinessWaitPeriod == null) { newBusinessWaitPeriod = Integer.valueOf(0);}
    if (discountAmount != null) { discountAmount = com.socotra.platform.tools.NumberUtils.trimScale(discountAmount, 2, RoundingMode.HALF_EVEN);}
    }

    public String type() { return TYPE; }

    public ZenCoverQuickQuoteData maskData(DataMaskingLevel level) {
      ZenCoverQuickQuoteDataBuilder builder = toBuilder();
      if (level == DataMaskingLevel.level1) {
      }
      if (level == DataMaskingLevel.level2) {
      }
      if (level == DataMaskingLevel.none) {
      }
      return builder.build();
    }

    public ZenCoverQuickQuoteData anonymizeData() {
      ZenCoverQuickQuoteDataBuilder builder = toBuilder();
      return builder.build();
    }

    @Override
    public Collection<ValidationItem> validate(DeploymentConfig config, ValidatableContext context) {
      ValidationItem.ValidationItemBuilder validationItemBuilder = ValidationItem.builder().elementType(TYPE);
      if (coolingOffPeriod == null) { validationItemBuilder.addError("Non optional property 'zenCoverQuickQuoteData.coolingOffPeriod' is missing"); }
      if (discountProfileCode != null && discountProfileCode.length() > 20000) { validationItemBuilder.addError("'zenCoverQuickQuoteData.discountProfileCode' length is more than max length of 20000"); }
      if (discountTerm != null && discountTerm.length() > 20000) { validationItemBuilder.addError("'zenCoverQuickQuoteData.discountTerm' length is more than max length of 20000"); }
      if (discountType != null && discountType.length() > 20000) { validationItemBuilder.addError("'zenCoverQuickQuoteData.discountType' length is more than max length of 20000"); }
      if (discountType != null && !discountTypeOptions.contains(discountType)) { validationItemBuilder.addError("Property 'zenCoverQuickQuoteData.discountType' should be one of " + discountTypeOptions); }
      if (gracePeriodType != null && gracePeriodType.length() > 20000) { validationItemBuilder.addError("'zenCoverQuickQuoteData.gracePeriodType' length is more than max length of 20000"); }
      if (newBusinessWaitPeriod != null && !newBusinessWaitPeriodOptions.contains(newBusinessWaitPeriod)) { validationItemBuilder.addError("Property 'zenCoverQuickQuoteData.newBusinessWaitPeriod' should be one of " + newBusinessWaitPeriodOptions); }
      if (settlementPeriodType != null && settlementPeriodType.length() > 20000) { validationItemBuilder.addError("'zenCoverQuickQuoteData.settlementPeriodType' length is more than max length of 20000"); }

      return validationItemBuilder.hasErrors() ? List.of(validationItemBuilder.build()) : List.of();
    }

    @Override
    public ZenCoverQuickQuoteData correct(DeploymentConfig config, ValidationErrorResolver resolver) {
      ZenCoverQuickQuoteDataBuilder builder = this.toBuilder();
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
    public ZenCoverQuickQuoteData applyAvailabilityRemovals(Instant referenceDate) {
      ZenCoverQuickQuoteDataBuilder builder = toBuilder();

      return builder.build();
    }

    public static ZenCoverQuickQuoteDataBuilder builder() {
      return new ZenCoverQuickQuoteDataBuilder();
    }
    public ZenCoverQuickQuoteDataBuilder toBuilder() {
      return new ZenCoverQuickQuoteDataBuilder()
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

    public static class ZenCoverQuickQuoteDataBuilder {
      private OffsetDateTime contractTermEndDate;
      public ZenCoverQuickQuoteDataBuilder contractTermEndDate(OffsetDateTime contractTermEndDate) { this.contractTermEndDate = contractTermEndDate; return this; }
      private Integer coolingOffPeriod;
      public ZenCoverQuickQuoteDataBuilder coolingOffPeriod(Integer coolingOffPeriod) { this.coolingOffPeriod = coolingOffPeriod; return this; }
      private BigDecimal discountAmount;
      public ZenCoverQuickQuoteDataBuilder discountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; return this; }
      private String discountProfileCode;
      public ZenCoverQuickQuoteDataBuilder discountProfileCode(String discountProfileCode) { this.discountProfileCode = discountProfileCode; return this; }
      private String discountTerm;
      public ZenCoverQuickQuoteDataBuilder discountTerm(String discountTerm) { this.discountTerm = discountTerm; return this; }
      private String discountType;
      public ZenCoverQuickQuoteDataBuilder discountType(String discountType) { this.discountType = discountType; return this; }
      private LocalDate expectedRenewalDate;
      public ZenCoverQuickQuoteDataBuilder expectedRenewalDate(LocalDate expectedRenewalDate) { this.expectedRenewalDate = expectedRenewalDate; return this; }
      private Integer gracePeriod;
      public ZenCoverQuickQuoteDataBuilder gracePeriod(Integer gracePeriod) { this.gracePeriod = gracePeriod; return this; }
      private String gracePeriodType;
      public ZenCoverQuickQuoteDataBuilder gracePeriodType(String gracePeriodType) { this.gracePeriodType = gracePeriodType; return this; }
      private Integer newBusinessWaitPeriod;
      public ZenCoverQuickQuoteDataBuilder newBusinessWaitPeriod(Integer newBusinessWaitPeriod) { this.newBusinessWaitPeriod = newBusinessWaitPeriod; return this; }
      private Integer settlementPeriod;
      public ZenCoverQuickQuoteDataBuilder settlementPeriod(Integer settlementPeriod) { this.settlementPeriod = settlementPeriod; return this; }
      private Integer settlementPeriodOffsetInDays;
      public ZenCoverQuickQuoteDataBuilder settlementPeriodOffsetInDays(Integer settlementPeriodOffsetInDays) { this.settlementPeriodOffsetInDays = settlementPeriodOffsetInDays; return this; }
      private String settlementPeriodType;
      public ZenCoverQuickQuoteDataBuilder settlementPeriodType(String settlementPeriodType) { this.settlementPeriodType = settlementPeriodType; return this; }

      public ZenCoverQuickQuoteData build() {
        return new ZenCoverQuickQuoteData(contractTermEndDate, coolingOffPeriod, discountAmount, discountProfileCode, discountTerm, discountType, expectedRenewalDate, gracePeriod, gracePeriodType, newBusinessWaitPeriod, settlementPeriod, settlementPeriodOffsetInDays, settlementPeriodType);
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