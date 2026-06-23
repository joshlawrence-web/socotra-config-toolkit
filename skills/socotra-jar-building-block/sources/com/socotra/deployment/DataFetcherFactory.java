package com.socotra.deployment;

import java.util.function.Supplier;

public final class DataFetcherFactory {
  private static Supplier<DataFetcher> supplier;

  public static synchronized void supplier(Supplier<DataFetcher> supplier) {
    if (DataFetcherFactory.supplier != null) {
      throw new IllegalArgumentException("Supplier can be initialized once");
    }
    DataFetcherFactory.supplier = supplier;
  }

  public static DataFetcher get() {
    if (supplier == null) {
      throw new IllegalStateException("DataFetcherFactory is not supported");
    }
    return supplier.get();
  }
}
