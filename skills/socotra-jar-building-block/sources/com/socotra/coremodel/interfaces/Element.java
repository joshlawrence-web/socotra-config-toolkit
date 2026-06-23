package com.socotra.coremodel.interfaces;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.socotra.coremodel.*;
import com.socotra.coremodel.views.Internal;
import com.socotra.deployment.DeploymentConfig;
import com.socotra.platform.tools.ULID;
import java.time.Instant;
import java.util.*;

public interface Element<T> extends Comparable<Element<T>>, Validatable, CustomerDataHolder {
  @JsonView({Internal.class})
  UUID tenantLocator();

  ULID locator();

  @JsonIgnore(false)
  String type();

  @JsonView({Internal.class})
  ULID rootLocator();

  ULID parentLocator();

  Map<String, Object> coverageTerms();

  Collection<T> elements();

  ULID staticLocator();

  default Optional<Instant> originalEffectiveTime() {
    return Optional.empty();
  }

  default Optional<ElementCategory> category() {
    return Optional.empty();
  }

  default int compareTo(Element o) {
    return locator().compareTo(o.locator());
  }

  @Deprecated(since = "2024-12-04")
  default Optional<Collection<ValidationItem>> validate(DeploymentConfig config) {
    return Optional.empty();
  }

  /**
   * Validates Config has Element class corresponding to give type. The method will call
   * validateType method on all sub-elements
   *
   * @param config deployment config use to get available types
   * @return list of validation errors or empty if no errors
   */
  @SuppressWarnings("unchecked")
  default Collection<ValidationItem> validateType(DeploymentConfig config) {
    Collection<ValidationItem> result = new ArrayList<>();
    if (config.getObjectClass(type()).isEmpty()) {
      result.add(
          ValidationItem.builder()
              .locator(locator())
              .elementType(type())
              .addError("Unknown object type")
              .build());
    }
    elements().forEach(e -> result.addAll(((Element<T>) e).validateType(config)));
    return result;
  }
}
