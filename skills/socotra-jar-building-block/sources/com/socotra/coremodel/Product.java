package com.socotra.coremodel;

import com.socotra.deployment.DeploymentConfig;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public interface Product extends CustomerObject {
  // Default duration in durationBasis units
  int defaultTermDuration();

  String defaultInstallmentPlanName();

  default String defaultDelinquencyPlanName() {
    return "";
  }

  @Deprecated
  default String defaultBillingPlanName() {
    return "";
  }

  default String defaultShortfallTolerancePlanName() {
    return "";
  }

  default String defaultAutoRenewalPlanName() {
    return "";
  }

  default String defaultExcessCreditPlanName() {
    return "";
  }

  // TODO: remove after AUG 2024
  @Deprecated()
  default DurationBasis durationBasis() {
    throw new RuntimeException("Invocation of a deprecated method");
  }

  default DurationBasis defaultDurationBasis() {
    return durationBasis();
  }

  List<String> eligibleAccountTypes();

  default Set<String> eligibleTransactionTypes() {
    return Set.of();
  }

  Set<String> documents();

  default Collection<CustomEvent> scheduledEvents() {
    return List.of();
  }

  default BillingTrigger defaultBillingTrigger() {
    return BillingTrigger.issue;
  }

  // for Quotes and Policies
  default String numberingPlanName() {
    return "";
  }

  default String numberingString() {
    return "";
  }

  default boolean withPrecommitReapplication() {
    return false;
  }

  default boolean withMigrationOnRenewal() {
    return true;
  }

  @Deprecated()
  default QuoteGroupSettings quoteGroupSettings() {
    return null;
  }

  @Deprecated
  default Map<String, Collection<String>> workplans() {
    throw new RuntimeException("Invocation of a deprecated method");
  }

  default Map<String, Collection<String>> workplanTriggers() {
    return Map.of();
  }

  default void validateJurisdiction(
      DeploymentConfig config, String jurisdiction, Consumer<String> errorConsumer) {}

  default Set<Prompt> prompts() {
    return Set.of();
  }

  default String riskAssessmentCriteria() {
    return "";
  }

  default ProducerQualification producerQualification() {
    return ProducerQualification.none;
  }

  default String externalNumberingPlanName() {
    return "";
  }

  default boolean reservedPolicyNumberRequired() {
    return false;
  }
}
