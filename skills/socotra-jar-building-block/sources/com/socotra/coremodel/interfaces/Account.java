package com.socotra.coremodel.interfaces;

import com.fasterxml.jackson.annotation.JsonView;
import com.socotra.coremodel.*;
import com.socotra.coremodel.views.Internal;
import com.socotra.deployment.DeploymentFactory;
import com.socotra.platform.tools.ULID;
import java.time.Instant;
import java.util.*;

public interface Account extends CustomerDataHolder {
  ULID locator();

  String type();

  Map<String, Object> data();

  Instant createdAt();

  UUID createdBy();

  @JsonView(Internal.class)
  default Optional<byte[]> securityId() {
    return Optional.empty();
  }

  default Optional<String> region() {
    return Optional.empty();
  }

  default Optional<String> delinquencyPlanName() {
    return Optional.empty();
  }

  default Optional<String> shortfallTolerancePlanName() {
    return Optional.empty();
  }

  default Optional<Preferences> preferences() {
    return Optional.empty();
  }

  default Optional<String> autoRenewalPlanName() {
    return Optional.empty();
  }

  default Optional<String> excessCreditPlanName() {
    return Optional.empty();
  }

  default Optional<String> invoicingPlanName() {
    return Optional.empty();
  }

  default Optional<String> paymentExecutionRetryPlanName() {
    return Optional.empty();
  }

  default BillingLevel billingLevel() {
    return BillingLevel.policy;
  }

  default Optional<String> invoiceDocument() {
    return Optional.empty();
  }

  AccountState accountState();

  default Optional<String> accountNumber() {
    return Optional.empty();
  }

  default Optional<String> timezone() {
    return Optional.empty();
  }

  Collection<ContactRoles> contacts();

  default Optional<Instant> anonymizedAt() {
    return Optional.empty();
  }

  default Optional<ULID> configVersionLocator() {
    return Optional.empty();
  }

  @Override
  @SuppressWarnings("unchecked")
  default <T> T resolve(DeploymentFactory factory) {
    return toCustomerObject(factory);
  }
}
