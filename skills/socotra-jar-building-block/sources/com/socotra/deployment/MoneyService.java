package com.socotra.deployment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

public final class MoneyService {
  private final int defaultFractionDigits;

  public MoneyService(String currency) {
    this.defaultFractionDigits = Currency.getInstance(currency).getDefaultFractionDigits();
  }

  public BigDecimal toMoney(BigDecimal amount) {
    return amount.setScale(defaultFractionDigits, RoundingMode.HALF_EVEN);
  }

  public BigDecimal getAdjustedRateForWholeUnitAmount(BigDecimal amount, BigDecimal duration) {
    BigDecimal tmp = amount.multiply(duration).setScale(0, RoundingMode.HALF_EVEN);
    return tmp.divide(duration, RoundingMode.HALF_EVEN);
  }

  public BigDecimal getRateForTargetAmount(BigDecimal amount, BigDecimal duration) {
    return amount.divide(duration, RoundingMode.HALF_EVEN);
  }
}
