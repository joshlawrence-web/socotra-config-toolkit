package com.socotra.deployment.schedules;

import com.socotra.coremodel.Transaction;
import com.socotra.coremodel.interfaces.Quote;
import com.socotra.platform.tools.ULID;

public final class SchedulesFactory {
  private static ScheduleSupplier scheduleSupplier;

  public static Schedule getQuoteSchedule(Quote quote, ULID staticElementLocator) {
    return scheduleSupplier.get(quote.locator(), staticElementLocator);
  }

  public static Schedule getTransactionSchedule(
      Transaction transaction, ULID staticElementLocator) {
    return scheduleSupplier.get(transaction.locator(), staticElementLocator);
  }

  public static void setScheduleSupplier(ScheduleSupplier scheduleSupplier) {
    SchedulesFactory.scheduleSupplier = scheduleSupplier;
  }

  @FunctionalInterface
  public interface ScheduleSupplier {
    Schedule get(ULID quoteOrTransactionLocator, ULID staticElementLocator);
  }
}
