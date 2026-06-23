package com.socotra.coremodel.interfaces;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.socotra.coremodel.ValidationItem;
import java.util.Collection;

public interface ValidationResult {
  Collection<ValidationItem> validationItems();

  @JsonProperty
  default boolean success() {
    return validationItems().isEmpty();
  }
}
