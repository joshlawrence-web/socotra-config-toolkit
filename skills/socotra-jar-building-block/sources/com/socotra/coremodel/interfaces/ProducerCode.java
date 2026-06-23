package com.socotra.coremodel.interfaces;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.socotra.coremodel.CustomerObjectWithData;
import com.socotra.coremodel.NumberingObject;
import com.socotra.coremodel.ProducerCodeState;
import com.socotra.platform.tools.ULID;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface ProducerCode<T> extends CustomerObjectWithData<T>, NumberingObject {
  ULID locator();

  @JsonIgnore(value = false)
  String type();

  ULID producerLocator();

  ProducerCodeState producerCodeState();

  default Optional<String> code() {
    return Optional.empty();
  }

  Instant createdAt();

  UUID createdBy();

  default Optional<String> numberingString() {
    return Optional.empty();
  }
}
