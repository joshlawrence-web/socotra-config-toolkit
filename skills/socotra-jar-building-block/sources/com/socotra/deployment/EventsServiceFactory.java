package com.socotra.deployment;

import java.util.function.Supplier;

public final class EventsServiceFactory {

  private static Supplier<EventsService> supplier;

  public static synchronized void supplier(Supplier<EventsService> supplier) {
    if (EventsServiceFactory.supplier != null) {
      throw new IllegalArgumentException("Supplier can be initialized once");
    }
    EventsServiceFactory.supplier = supplier;
  }

  public static EventsService get() {
    if (supplier == null) {
      throw new IllegalStateException("EventsServiceFactory is not supported");
    }
    return supplier.get();
  }
}
