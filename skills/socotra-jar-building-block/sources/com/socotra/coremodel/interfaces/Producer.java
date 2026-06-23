package com.socotra.coremodel.interfaces;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.socotra.coremodel.CustomerObjectWithData;
import com.socotra.coremodel.ProducerState;
import com.socotra.platform.tools.ULID;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface Producer<T> extends CustomerObjectWithData<T> {
  ULID locator();

  @JsonIgnore(value = false)
  String type();

  ProducerState producerState();

  default Optional<ULID> parentLocator() {
    return Optional.empty();
  }

  Instant createdAt();

  UUID createdBy();
}
