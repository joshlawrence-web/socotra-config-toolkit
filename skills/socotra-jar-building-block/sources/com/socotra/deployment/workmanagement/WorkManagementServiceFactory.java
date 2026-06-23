package com.socotra.deployment.workmanagement;

import java.util.function.Supplier;

public class WorkManagementServiceFactory {
  private static Supplier<WorkManagementService> supplier;

  public static synchronized void supplier(Supplier<WorkManagementService> supplier) {
    if (WorkManagementServiceFactory.supplier != null) {
      throw new IllegalArgumentException("Supplier can be initialized once");
    }
    WorkManagementServiceFactory.supplier = supplier;
  }

  public static WorkManagementService get() {
    if (supplier == null) {
      throw new IllegalStateException("WorkManagementServiceFactory is not supported");
    }
    return supplier.get();
  }
}
