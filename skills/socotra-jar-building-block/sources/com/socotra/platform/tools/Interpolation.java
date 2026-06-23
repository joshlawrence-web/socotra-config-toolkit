package com.socotra.platform.tools;

import java.math.BigDecimal;
import java.math.RoundingMode;

public enum Interpolation {
  linear,
  stepUp,
  stepDown;

  public BigDecimal apply(
      BigDecimal x1, BigDecimal y1, BigDecimal x2, BigDecimal y2, BigDecimal x) {
    if (x1.compareTo(x2) <= 0) {
      return applyCanonical(x1, y1, x2, y2, x);
    } else {
      return applyCanonical(x2, y2, x1, y1, x);
    }
  }

  private BigDecimal applyCanonical(
      BigDecimal x1, BigDecimal y1, BigDecimal x2, BigDecimal y2, BigDecimal x) {
    return switch (this) {
      case linear -> linear(x1, y1, x2, y2, x);
      case stepUp -> x.compareTo(x1) > 0 ? y2 : y1;
      case stepDown -> x.compareTo(x2) < 0 ? y1 : y2;
    };
  }

  private BigDecimal linear(
      BigDecimal x1, BigDecimal y1, BigDecimal x2, BigDecimal y2, BigDecimal x) {
    if (x2.compareTo(x1) == 0) {
      return y1;
    }
    return y1.add(
        y2.subtract(y1).multiply(x.subtract(x1)).divide(x2.subtract(x1), RoundingMode.HALF_EVEN));
  }
}
