package com.socotra.coremodel;

public enum DurationBasis {
  none(0),
  years(100),
  months(200),
  monthsE360(300),
  weeks(400),
  days(500),
  hours(700);

  private final int value;

  private DurationBasis(int value) {
    this.value = value;
  }

  public int toInt() {
    return this.value;
  }

  public static DurationBasis fromInt(int value) {
    switch (value) {
      case 0:
        return none;
      case 100:
        return years;
      case 200:
        return months;
      case 300:
        return monthsE360;
      case 400:
        return weeks;
      case 500:
        return days;
      case 700:
        return hours;
      default:
        throw new IllegalArgumentException(value + " doesn't belong to DurationBasis");
    }
  }
}
