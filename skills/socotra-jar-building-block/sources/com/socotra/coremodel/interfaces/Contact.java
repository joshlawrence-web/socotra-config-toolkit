package com.socotra.coremodel.interfaces;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.socotra.coremodel.ContactState;
import com.socotra.coremodel.CustomerObjectWithData;
import com.socotra.coremodel.ValidationResult;
import com.socotra.platform.tools.ULID;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface Contact<T> extends CustomerObjectWithData<T> {
  @Override
  @JsonIgnore(value = false)
  String type();

  ULID locator();

  ULID staticLocator();

  ContactState contactState();

  Optional<String> region();

  Instant createdAt();

  UUID createdBy();

  default Optional<Instant> updatedAt() {
    return Optional.empty();
  }

  default Optional<UUID> updatedBy() {
    return Optional.empty();
  }

  default Optional<ValidationResult> validationResult() {
    return Optional.empty();
  }

  default Optional<Instant> anonymizedAt() {
    return Optional.empty();
  }
}
