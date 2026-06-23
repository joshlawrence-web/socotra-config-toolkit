package com.socotra.deployment;

import com.socotra.coremodel.*;
import com.socotra.coremodel.interfaces.EventType;
import com.socotra.deployment.producermanagement.ProducerManagementConfig;
import com.socotra.deployment.workmanagement.WorkManagementConfig;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/** DeploymentConfig is the base class for generated Customer configuration by deployment service */
@Slf4j
public abstract class DeploymentConfig {

  @Getter protected String defaultTimezone;
  @Getter protected String defaultCurrency;
  @Getter protected String defaultAuxDataSettings;

  @Getter protected SearchConfiguration searchConfiguration;

  @Getter protected String defaultInstallmentPlanName;
  @Deprecated protected String defaultBillingPlanName;
  @Getter protected String defaultDelinquencyPlanName;
  @Deprecated protected BillingTrigger defaultBillingTrigger;
  @Getter protected String defaultAutoRenewalPlanName;
  protected BackdatedInstallmentsBilling defaultBackdatedInstallmentsBilling;

  @Deprecated protected String defaultWriteOffTolerancePlanName;
  @Getter protected String defaultShortfallTolerancePlanName;
  @Getter protected String defaultExcessCreditPlanName;
  @Getter protected String defaultInvoicingPlanName;
  @Getter protected String defaultPaymentNumberingPlanName;
  @Getter protected String defaultDisbursementNumberingPlanName;
  @Getter protected String defaultOperationsWorkbenchUIConfig;
  @Getter protected String defaultRetryPlanName;
  @Deprecated @Getter protected String defaultAutomationPluginSecret;
  protected Map<String, Product> products;
  protected Map<String, InstallmentPlan> installmentPlans;
  protected Map<String, InvoicingPlan> invoicingPlans;
  @Deprecated protected Map<String, BillingPlan> billingPlans;
  protected Map<String, DelinquencyPlan> delinquencyPlans;
  protected Map<String, AutoRenewalPlan> autoRenewalPlans;
  protected Map<String, ExcessCreditPlan> excessCreditPlans;
  protected Map<String, RetryPlan> retryPlans;
  protected Map<String, Class<? extends AccountDefinition>> accounts;
  protected Map<String, Class<? extends TableMetadata>> tables;
  protected Map<String, Class<? extends RangeTableMetadata>> rangeTables;
  protected Map<String, Class<? extends ConstraintTableMetadata>> constraintTables;
  protected Map<String, Class<? extends CustomerObject>> secrets;
  protected Map<String, Map<String, PropertyConstraint>> constraints;
  @Getter protected Set<String> regions;
  @Getter protected Set<String> jurisdictions = Set.of();
  @Getter protected String defaultRegion;
  protected Map<String, Class<? extends CustomerObject>> elements;
  protected Map<String, Class<? extends CustomerObject>> objectTypes = new HashMap<>();
  protected Map<String, ResourceDeclaration> resourceDeclarations = new HashMap<>();

  @Getter protected Map<String, CustomEvent> customEvents = Map.of();
  protected Map<EventType, CustomEvent> customEventsByType = Map.of();
  @Getter protected Map<String, TenantCustomEvent> tenantCustomEvents = Map.of();
  protected Map<EventType, TenantCustomEvent> tenantCustomEventsByType = Map.of();

  protected Map<String, Integer> auxDataSettings = Map.of();
  protected Map<String, Class<? extends CustomerObject>> payments = new HashMap<>();

  protected Map<String, Class<? extends CustomerObject>> disbursements = new HashMap<>();
  protected Map<String, ShortfallTolerancePlan> shortfallTolerancePlans;
  protected Map<String, NumberingPlan> numberingPlans = Map.of();
  protected Map<String, String> regionNumberingStrings = Map.of();
  protected Map<String, ExternalNumberingPlan> externalNumberingPlans = Map.of();
  @Getter protected Map<String, NumberingPlan> typeToNumberingPlan = new HashMap<>();

  @Getter
  protected Map<String, ExternalNumberingPlan> typeToExternalNumberingPlan = new HashMap<>();

  protected Map<String, ElementSchedule> typeToScheduleItems = new HashMap<>();
  @Getter protected Map<String, AutomationPluginDetails> automations = Map.of();
  @Getter protected List<String> webhookEnabledAutomationPluginPath = List.of();

  @Getter protected DataAccessControl dataAccessControl;
  @Deprecated protected DataSecurity dataSecurity;
  @Getter protected WorkManagementConfig workManagement;
  @Getter protected String defaultInvoiceDocument;
  @Getter protected boolean isSerialInvoiceNumberingEnabled = false;
  @Getter protected boolean isEntityAnonymizationEnabled = false;
  @Getter protected boolean isCustomerDataEncryptionEnabled = false;
  @Getter protected ProducerManagementConfig producerManagement;

  protected Map<String, Class<? extends CustomerObject>> contacts;
  @Getter protected Set<String> contactRoles;

  private static final Set<PluginType> BASELINE_PLUGINS;
  protected Set<PluginType> knownPlugins = BASELINE_PLUGINS;

  /*
   * There are presumable always existing plugin (effective ~ Sep 2025)
   * List of all known plugins are generated in deployment-service
   * to variable `knownPlugins`.
   * This list of fallback for this variable (i.e. always existing plugins)
   * DO NOT ADD new plugins here
   * */
  static {
    BASELINE_PLUGINS =
        Set.of(
            PluginType.rating,
            PluginType.underwriting,
            PluginType.validation,
            PluginType.documentSelection,
            PluginType.documentDataSnapshot,
            PluginType.preCommit,
            PluginType.renewal,
            PluginType.delinquencyEvent,
            PluginType.documentConsolidationSnapshot,
            PluginType.installments,
            PluginType.autopay,
            PluginType.paymentPostProcessing,
            PluginType.cancellation);
  }

  protected DeploymentFactory factory;

  protected void buildObjectRepository() {
    if (this.customEvents != null) {
      this.objectTypes.putAll(
          this.customEvents.entrySet().stream()
              .collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue().getClass())));
    }

    if (this.tenantCustomEvents != null) {
      this.objectTypes.putAll(
          this.tenantCustomEvents.entrySet().stream()
              .collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue().getClass())));
    }

    if (this.products != null) {
      this.objectTypes.putAll(
          this.products.entrySet().stream()
              .collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue().getClass())));
    }

    this.objectTypes.putAll(this.accounts);
    this.objectTypes.putAll(this.payments);
    this.objectTypes.putAll(this.disbursements);
    if (this.elements != null) {
      this.objectTypes.putAll(this.elements);
    }
    if (this.contacts != null) {
      this.objectTypes.putAll(contacts);
    }
    if (this.producerManagement != null) {
      this.objectTypes.putAll(this.producerManagement.producers());
      this.objectTypes.putAll(this.producerManagement.producerCodes());
      this.objectTypes.putAll(this.producerManagement.producerLicenses());
      this.objectTypes.putAll(this.producerManagement.producerAppointments());
    }

    log.info("Known object types: {}", this.objectTypes.keySet());
  }

  @Deprecated
  public String getDefaultBillingPlanName() {
    return this.defaultBillingPlanName;
  }

  public BackdatedInstallmentsBilling getDefaultBackdatedInstallmentsBilling() {
    // Provide a default value to avoid breaking existing deployments that don't have this property
    return defaultBackdatedInstallmentsBilling == null
        ? BackdatedInstallmentsBilling.deferDueDate
        : defaultBackdatedInstallmentsBilling;
  }

  private <T> Optional<T> getIgnoreCase(Map<String, T> data, String key) {
    if (data == null) {
      return Optional.empty();
    }
    T value = data.get(key);
    if (value == null) {
      value = data.get(key.toLowerCase());
    }
    return Optional.ofNullable(value);
  }

  public Optional<Product> getProduct(String name) {
    return getIgnoreCase(products, name);
  }

  public Set<String> getAllProductNames() {
    return products.keySet();
  }

  public Product getProductRequired(String name) {
    return getProduct(name)
        .orElseThrow(
            () -> new NoSuchElementException("Product is not found for given name: " + name));
  }

  public Optional<InstallmentPlan> getInstallmentPlan(String name) {
    return getIgnoreCase(installmentPlans, name);
  }

  public Optional<InvoicingPlan> getInvoicingPlan(String name) {
    return getIgnoreCase(invoicingPlans, name);
  }

  @Deprecated
  public Optional<BillingPlan> getBillingPlan(String name) {
    return getIgnoreCase(billingPlans, name);
  }

  public Optional<DelinquencyPlan> getDelinquencyPlan(String name) {
    return getIgnoreCase(delinquencyPlans, name);
  }

  public Optional<AutoRenewalPlan> getAutoRenewalPlan(String name) {
    return getIgnoreCase(autoRenewalPlans, name);
  }

  public Optional<Class<? extends AccountDefinition>> getAccount(String name) {
    return getIgnoreCase(accounts, name);
  }

  public Optional<ShortfallTolerancePlan> getShortfallTolerancePlan(String name) {
    return getIgnoreCase(shortfallTolerancePlans, name);
  }

  public Optional<NumberingPlan> getNumberingPlan(String name) {
    return getIgnoreCase(numberingPlans, name);
  }

  public Optional<ExcessCreditPlan> getExcessCreditPlan(String name) {
    return getIgnoreCase(excessCreditPlans, name);
  }

  public Optional<RetryPlan> getRetryPlan(String name) {
    return getIgnoreCase(retryPlans, name);
  }

  public Optional<String> getRegionNumberingString(String name) {
    return getIgnoreCase(regionNumberingStrings, name);
  }

  public Optional<AutomationPluginDetails> getAutomationPlugin(String name) {
    return getIgnoreCase(automations, name);
  }

  public BillingTrigger getDefaultBillingTrigger() {
    throw new RuntimeException("Invocation of 'getDefaultBillingTrigger' deprecated method");
  }

  @Deprecated
  public String getDefaultWriteOffTolerancePlanName() {
    return defaultWriteOffTolerancePlanName;
  }

  public Optional<Class<? extends TableMetadata>> getTable(String staticName) {
    return getIgnoreCase(tables, staticName);
  }

  public Optional<Class<? extends RangeTableMetadata>> getRangeTable(String staticName) {
    return getIgnoreCase(rangeTables, staticName);
  }

  public Optional<Class<? extends ConstraintTableMetadata>> getConstraintTable(String staticName) {
    return getIgnoreCase(constraintTables, staticName);
  }

  public Optional<Class<? extends CustomerObject>> getSecret(String staticName) {
    return getIgnoreCase(secrets, staticName);
  }

  public Optional<Map<String, PropertyConstraint>> getConstraints(String typeName) {
    return getIgnoreCase(constraints, typeName);
  }

  public Optional<Class<? extends CustomerObject>> getObjectClass(String type) {
    log.debug("Requesting {}", type);
    return getIgnoreCase(objectTypes, type);
  }

  public Optional<Class<? extends CustomerObject>> getContactClass(String type) {
    return getIgnoreCase(contacts, type);
  }

  public Optional<Class<? extends CustomerObject>> getProducer(String type) {
    if (producerManagement == null) {
      return Optional.empty();
    }
    return getIgnoreCase(producerManagement.producers(), type);
  }

  public Optional<Class<? extends CustomerObject>> getProducerCode(String type) {
    if (producerManagement == null) {
      return Optional.empty();
    }
    return getIgnoreCase(producerManagement.producerCodes(), type);
  }

  public Optional<Class<? extends CustomerObject>> getProducerLicense(String type) {
    if (producerManagement == null) {
      return Optional.empty();
    }
    return getIgnoreCase(producerManagement.producerLicenses(), type);
  }

  public Optional<Class<? extends CustomerObject>> getProducerAppointment(String type) {
    if (producerManagement == null) {
      return Optional.empty();
    }
    return getIgnoreCase(producerManagement.producerAppointments(), type);
  }

  public boolean isKnownPlugin(PluginType pluginType) {
    boolean result = knownPlugins.contains(pluginType);
    if (!result) {
      log.warn("Unknown plugin: {}", pluginType);
    }
    return result;
  }

  /**
   * Returns TimeService that is applicable for given product
   *
   * @param product
   * @return
   */
  public TimeService timeService(Product product) {
    return new TimeService(this.getDefaultTimezone(), product.defaultDurationBasis());
  }

  /**
   * Returns TimeService instance for given timezone and duration
   *
   * @param timeZone
   * @param durationBasis
   * @return
   */
  public TimeService timeService(String timeZone, DurationBasis durationBasis) {
    return new TimeService(timeZone, durationBasis);
  }

  /**
   * Returns TimeService instance for default timezone and given duration
   *
   * @param durationBasis
   * @return
   */
  public TimeService timeService(DurationBasis durationBasis) {
    return new TimeService(this.getDefaultTimezone(), durationBasis);
  }

  public TimeService timeService(String productName) {
    return timeService(getProductRequired(productName));
  }

  /**
   * Returns configured resource for given name
   *
   * @param resourceName
   * @return
   */
  public Optional<ResourceDeclaration> getResourceDeclaration(String resourceName) {
    return getIgnoreCase(resourceDeclarations, resourceName);
  }

  /**
   * Returns configured document config for given name
   *
   * @param name
   * @return
   */
  public Optional<DocumentConfig> getDocumentConfig(String name) {
    return Optional.empty();
  }

  public Optional<TemplateSnippetConfig> getTemplateSnippetConfig(String name) {
    return Optional.empty();
  }

  public Optional<ConsolidatedDocumentConfig> getConsolidatedDocumentConfig(String name) {
    return Optional.empty();
  }

  public Optional<MoratoriumConfig> getMoratorium(String name) {
    return Optional.empty();
  }

  public Map<String, MoratoriumConfig> getMoratoriums() {
    return Map.of();
  }

  public boolean hasCustomFont(String customFontName) {
    return false;
  }

  /**
   * Returns a list of configured document configs associated with given product name and enabled
   * for given trigger
   *
   * @param productName
   * @param trigger
   * @return
   */
  public Collection<DocumentConfig> getDocumentConfigsForProduct(
      String productName, DocumentTrigger trigger) {
    return getProductRequired(productName).documents().stream()
        .filter(x -> getConsolidatedDocumentConfig(x).isEmpty())
        .map(
            x ->
                getDocumentConfig(x)
                    .orElseThrow(
                        () -> // Shouldn't really happen unless we screw up the config validation
                        new NoSuchElementException(
                                "DocumentConfig is not found for given name: " + x)))
        .filter(d -> d.trigger().equals(trigger))
        .toList();
  }

  public Collection<ConsolidatedDocumentConfig> getConsolidatedConfigsForProduct(
      String productName, DocumentTrigger trigger) {
    return getProductRequired(productName).documents().stream()
        .filter(x -> getDocumentConfig(x).isEmpty())
        .map(
            x ->
                getConsolidatedDocumentConfig(x)
                    .orElseThrow(
                        () -> // Shouldn't really happen unless we screw up the config validation
                        new NoSuchElementException(
                                "ConsolidatedDocumentConfig is not found for given name: " + x)))
        .filter(d -> d.trigger().equals(trigger))
        .toList();
  }

  /**
   * Retrieves the DocumentConfig for the given invoice document name, if it exists and is within
   * the invoice scope.
   *
   * @param invoiceDocumentName the name of the invoice document to retrieve the configuration for
   * @return an Optional containing the DocumentConfig if found and within the invoice scope,
   *     otherwise an empty Optional
   * @throws NullPointerException if the invoiceDocumentName is null
   */
  public Optional<DocumentConfig> getInvoiceDocumentConfig(String invoiceDocumentName) {
    Objects.requireNonNull(invoiceDocumentName, "invoiceDocumentName must not be null");
    return getDocumentConfig(invoiceDocumentName)
        .filter(documentConfig -> documentConfig.scope() == DocumentScope.invoice);
  }

  public MoneyService moneyService(String currency) {
    return new MoneyService(currency);
  }

  /**
   * @return Optional of the CustomEvent with the given name
   */
  public Optional<CustomEvent> getCustomEvent(String name) {
    return getIgnoreCase(customEvents, name);
  }

  /**
   * @param eventTypeId the String id of the EventType of the CustomEvent
   * @return Optional of the CustomEvent associated with given EventType
   */
  public Optional<CustomEvent> getCustomEventByEventType(String eventTypeId) {
    return getCustomEventByEventType(EventType.of(eventTypeId));
  }

  /**
   * @param eventType the EventType of the CustomEvent
   * @return Optional of the CustomEvent associated with given EventType
   */
  public Optional<CustomEvent> getCustomEventByEventType(EventType eventType) {
    return Optional.ofNullable(getCustomEventsByEventType().get(eventType));
  }

  /**
   * @return a Map of EventType to the associated CustomEvent
   */
  public Map<EventType, CustomEvent> getCustomEventsByEventType() {
    return customEventsByType;
  }

  /**
   * @param name the name of the auxDataSettings record
   * @return Optional of the AuxDataSettings associated with given name
   */
  public Optional<Integer> getAuxDataSettings(String name) {
    return Optional.ofNullable(auxDataSettings.get(name));
  }

  /**
   * Return Numbering plan associated with object type if any
   *
   * @param objectType
   * @return
   */
  public Optional<NumberingPlan> getNumberingPlanByObjectType(String objectType) {
    return getIgnoreCase(typeToNumberingPlan, objectType);
  }

  /**
   * Return ElementSchedule associated with object type if any
   *
   * @param objectType
   * @return
   */
  public Optional<ElementSchedule> getScheduleItemByObjectType(String objectType) {
    return getIgnoreCase(typeToScheduleItems, objectType);
  }

  /**
   * @return Optional of the TenantCustomEvent with the given name
   */
  public Optional<TenantCustomEvent> getTenantCustomEvent(String name) {
    return getIgnoreCase(tenantCustomEvents, name);
  }

  /**
   * @param eventTypeId the String id of the EventType of the CustomEvent
   * @return Optional of the TenantCustomEvent associated with given EventType
   */
  public Optional<TenantCustomEvent> getTenantCustomEventByEventType(String eventTypeId) {
    return getTenantCustomEventByEventType(EventType.of(eventTypeId));
  }

  /**
   * @param eventType the EventType of the CustomEvent
   * @return Optional of the TenantCustomEvent associated with given EventType
   */
  public Optional<TenantCustomEvent> getTenantCustomEventByEventType(EventType eventType) {
    return Optional.ofNullable(getTenantCustomEventsByEventType().get(eventType));
  }

  /**
   * @return a Map of EventType to the associated TenantCustomEvent
   */
  public Map<EventType, TenantCustomEvent> getTenantCustomEventsByEventType() {
    return tenantCustomEventsByType;
  }

  public Optional<ExternalNumberingPlan> getExternalNumberingPlan(String name) {
    return getIgnoreCase(externalNumberingPlans, name);
  }

  /**
   * Return External Numbering plan associated with object type if any
   *
   * @param objectType
   * @return
   */
  public Optional<ExternalNumberingPlan> getExternalNumberingPlanByObjectType(String objectType) {
    return getIgnoreCase(typeToExternalNumberingPlan, objectType);
  }
}
