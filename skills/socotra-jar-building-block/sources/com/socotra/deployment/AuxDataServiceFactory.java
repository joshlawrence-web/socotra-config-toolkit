package com.socotra.deployment;

import java.util.function.Supplier;

public class AuxDataServiceFactory {
  private static Supplier<AuxDataService> supplier;

  public static synchronized void supplier(Supplier<AuxDataService> supplier) {
    if (AuxDataServiceFactory.supplier != null) {
      throw new IllegalArgumentException("Supplier can be initialized once");
    }
    AuxDataServiceFactory.supplier = supplier;
  }

  public static AuxDataService get() {
    if (supplier == null) {
      throw new IllegalStateException("AuxDataServiceFactory is not supported");
    }
    return supplier.get();
  }
}
