package com.socotra.deployment.schedules;

import com.socotra.coremodel.Chargeable;
import com.socotra.coremodel.Transaction;
import com.socotra.coremodel.interfaces.Quote;
import com.socotra.platform.tools.ULID;

public abstract class RatingRegistryFactory {
  private static volatile RatingRegistryFactory instance;

  protected RatingRegistryFactory() {
    synchronized (RatingRegistryFactory.class) {
      if (instance != null) {
        throw new IllegalStateException("RatingRegistryFactory is already initialized");
      }
      instance = this;
    }
  }

  public static RatingRegistryFactory getInstance() {
    if (instance == null) {
      throw new IllegalStateException("RatingRegistryFactory is not supported");
    }
    return instance;
  }

  public static RatingRegistry createFor(Quote quote, Chargeable element) {
    return getInstance().createInternal(quote, element.locator());
  }

  public static RatingRegistry createFor(Transaction transaction, Chargeable element) {
    return getInstance().createInternal(transaction, element.locator());
  }

  protected abstract RatingRegistry createInternal(Object ratable, ULID elementLocator);
}
