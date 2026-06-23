package com.socotra.coremodel;

import com.socotra.coremodel.interfaces.EventType;
import java.util.Optional;

public interface CustomEvent extends CustomerObject, EventType {
  EventType eventType();

  default Optional<EventSchedule> schedule() {
    return Optional.empty();
  }

  default String id() {
    return eventType().id();
  }
}
