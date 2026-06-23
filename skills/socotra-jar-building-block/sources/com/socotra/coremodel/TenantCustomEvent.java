package com.socotra.coremodel;

import com.socotra.coremodel.interfaces.EventType;
import java.util.Optional;

public interface TenantCustomEvent extends CustomerObject, EventType {
  EventType eventType();

  default Optional<TenantEventSchedule> schedule() {
    return Optional.empty();
  }

  default String id() {
    return eventType().id();
  }

  default boolean isPersisted() {
    return true;
  }
}
