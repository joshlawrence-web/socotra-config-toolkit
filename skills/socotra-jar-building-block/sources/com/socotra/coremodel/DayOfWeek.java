package com.socotra.coremodel;

import java.time.DateTimeException;
import lombok.Getter;

@Getter
public enum DayOfWeek {
  monday,
  tuesday,
  wednesday,
  thursday,
  friday,
  saturday,
  sunday;

  private static final DayOfWeek[] EnumValues = DayOfWeek.values();

  public static DayOfWeek of(int dayOfWeek) {
    if (dayOfWeek < 0 || dayOfWeek > 6) {
      throw new DateTimeException("Invalid value of dayOfWeek: " + dayOfWeek);
    }
    return EnumValues[dayOfWeek];
  }

  public int getValue() {
    return ordinal() + 1;
  }
}
