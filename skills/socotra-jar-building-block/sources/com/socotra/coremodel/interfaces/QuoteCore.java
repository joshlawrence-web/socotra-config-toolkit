package com.socotra.coremodel.interfaces;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.socotra.coremodel.*;
import com.socotra.coremodel.Element;
import com.socotra.deployment.DeploymentFactory;
import com.socotra.platform.tools.ULID;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuoteCore {
  @JsonIgnore
  String type();

  ULID locator();

  String productName();

  Optional<Instant> startTime();

  Optional<Instant> endTime();

  Optional<String> timezone();

  Optional<String> currency();

  Optional<Instant> expirationTime();

  default Optional<UUID> createdBy() {
    return Optional.empty();
  }

  default Optional<Instant> createdAt() {
    return Optional.empty();
  }

  Element element();

  default Collection<ContactRoles> contacts() {
    return List.of();
  }

  default Optional<Preferences> preferences() {
    return Optional.empty();
  }

  default Optional<BillingTrigger> billingTrigger() {
    return Optional.empty();
  }

  default Optional<String> delinquencyPlanName() {
    return Optional.empty();
  }

  default Optional<String> autoRenewalPlanName() {
    return Optional.empty();
  }

  default BillingLevel billingLevel() {
    return BillingLevel.inherit;
  }

  default Optional<DurationBasis> durationBasis() {
    return Optional.empty();
  }

  default Optional<String> jurisdiction() {
    return Optional.empty();
  }

  default Optional<String> producerCode() {
    return Optional.empty();
  }

  default Optional<ULID> configVersionLocator() {
    if (this instanceof CustomerObject co) {
      return Optional.ofNullable(co.configLocator());
    }
    return Optional.empty();
  }

  <T extends CustomerObject> T toCustomerObject(DeploymentFactory factory);

  default CustomerObject maskData(DataMaskingLevel level) {
    throw new UnsupportedOperationException("maskData is not supported for QuoteCore");
  }

  default QuoteCore anonymizeData() {
    throw new UnsupportedOperationException("anonymizeData is not supported for QuoteCore");
  }
}
