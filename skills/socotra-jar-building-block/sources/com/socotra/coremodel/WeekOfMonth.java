package com.socotra.coremodel;

import java.time.DateTimeException;
import lombok.Getter;

@Getter
public enum WeekOfMonth {
  none(0),
  first(1),
  second(2),
  third(3),
  fourth(4),
  fifth(5);
  private static final WeekOfMonth[] EnumValues = WeekOfMonth.values();

  private final int ordinal;

  WeekOfMonth(int ordinal) {
    this.ordinal = ordinal;
  }

  public static WeekOfMonth of(int weekOfMonth) {
    if (weekOfMonth < 0 || weekOfMonth > 5) {
      throw new DateTimeException("Invalid value for weekOfMonth: " + weekOfMonth);
    }
    return EnumValues[weekOfMonth];
  }
}
