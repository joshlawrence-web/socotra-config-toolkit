package com.socotra.coremodel.interfaces;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.socotra.coremodel.CustomerObjectWithData;
import com.socotra.coremodel.ProducerLicenseState;
import com.socotra.platform.tools.ULID;
import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface ProducerLicense<T> extends CustomerObjectWithData<T> {
  ULID locator();

  @JsonIgnore(value = false)
  String type();

  ProducerLicenseState producerLicenseState();

  ULID producerLocator();

  default Optional<String> licenseNumber() {
    return Optional.empty();
  }

  default Collection<String> producerCodes() {
    return java.util.List.of();
  }

  default Collection<String> jurisdictions() {
    return java.util.List.of();
  }

  default Collection<String> products() {
    return java.util.List.of();
  }

  default Optional<Instant> effectiveTime() {
    return Optional.empty();
  }

  default Optional<Instant> expirationTime() {
    return Optional.empty();
  }

  Instant createdAt();

  UUID createdBy();
}
