package com.socotra.coremodel.interfaces;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.socotra.coremodel.CustomerObjectWithData;
import com.socotra.coremodel.ProducerAppointmentState;
import com.socotra.platform.tools.ULID;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProducerAppointment<T> extends CustomerObjectWithData<T> {
  ULID locator();

  @JsonIgnore(value = false)
  String type();

  ProducerAppointmentState producerAppointmentState();

  ULID producerLocator();

  default Optional<String> appointmentNumber() {
    return Optional.empty();
  }

  default Collection<String> producerCodes() {
    return List.of();
  }

  default Collection<String> jurisdictions() {
    return List.of();
  }

  default Collection<String> products() {
    return List.of();
  }

  default Collection<ULID> licenses() {
    return List.of();
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
