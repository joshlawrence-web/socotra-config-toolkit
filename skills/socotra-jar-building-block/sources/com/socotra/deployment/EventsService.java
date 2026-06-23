package com.socotra.deployment;

import com.socotra.coremodel.interfaces.EventType;
import java.util.Map;

public interface EventsService {

  static EventsService getInstance() {
    return EventsServiceFactory.get();
  }

  void createEvent(EventType eventType, Object eventData);

  default void createEvent(EventType eventType) {
    createEvent(eventType, Map.of());
  }
}
