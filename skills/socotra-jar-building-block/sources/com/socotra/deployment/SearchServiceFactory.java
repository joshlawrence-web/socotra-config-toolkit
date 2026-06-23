package com.socotra.deployment;

import java.util.function.Supplier;

public class SearchServiceFactory {
  private static Supplier<SearchService> supplier;

  public static synchronized void supplier(Supplier<SearchService> supplier) {
    if (SearchServiceFactory.supplier != null) {
      throw new IllegalArgumentException("Supplier can be initialized once");
    }
    SearchServiceFactory.supplier = supplier;
  }

  public static SearchService get() {
    if (supplier == null) {
      throw new IllegalStateException("SearchServiceFactory is not supported");
    }
    return supplier.get();
  }
}
