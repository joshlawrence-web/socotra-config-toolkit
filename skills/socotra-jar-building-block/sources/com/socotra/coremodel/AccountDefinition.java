package com.socotra.coremodel;

import com.socotra.platform.tools.ULID;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AccountDefinition<T extends CustomerObject> extends CustomerObjectWithData<T> {
  ULID locator();

  // TODO - There are a casting issue in policy service while doing Account validation
  // Need to investigate more.
  T data();

  default Optional<String> delinquencyPlanName() {
    return Optional.empty();
  }

  default Optional<String> autoRenewalPlanName() {
    return Optional.empty();
  }

  default Optional<String> excessCreditPlanName() {
    return Optional.empty();
  }

  default Optional<String> shortfallTolerancePlanName() {
    return Optional.empty();
  }

  default Optional<Preferences> preferences() {
    return Optional.empty();
  }

  default BillingLevel billingLevel() {
    return BillingLevel.policy;
  }

  default Optional<String> region() {
    return Optional.empty();
  }

  default Optional<String> invoiceDocument() {
    return Optional.empty();
  }

  default Optional<String> numberingPlanName() {
    return Optional.empty();
  }

  default Optional<String> invoiceNumberingPlanName() {
    return Optional.empty();
  }

  default Optional<String> paymentExecutionRetryPlanName() {
    return Optional.empty();
  }

  default Optional<String> accountNumber() {
    return Optional.empty();
  }

  default Optional<Instant> anonymizedAt() {
    return Optional.empty();
  }

  default Optional<String> timezone() {
    return Optional.empty();
  }

  default Collection<ContactRoles> contacts() {
    return List.of();
  }

  default Optional<String> invoicingPlanName() {
    return Optional.empty();
  }
}
