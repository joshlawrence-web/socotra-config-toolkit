package com.socotra.platform.tools;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class NumberUtils {
  public static final RoundingMode DEFAULT_ROUNDING_MODE = RoundingMode.HALF_EVEN;

  public static BigDecimal trimScale(BigDecimal decimal) {
    return trimScale(decimal, Math.max(0, decimal.scale()));
  }

  public static BigDecimal trimScale(BigDecimal decimal, int maxScale) {
    return trimScale(decimal, maxScale, DEFAULT_ROUNDING_MODE);
  }

  public static BigDecimal trimScale(BigDecimal decimal, int maxScale, RoundingMode roundingMode) {
    if (decimal == null) {
      return null;
    }
    if (decimal.scale() > maxScale) {
      decimal = decimal.setScale(maxScale, roundingMode);
    }
    decimal = decimal.stripTrailingZeros();
    if (decimal.scale() != maxScale && decimal.scale() < 0) {
      decimal = decimal.setScale(Math.min(0, maxScale));
    }
    return decimal;
  }
}
