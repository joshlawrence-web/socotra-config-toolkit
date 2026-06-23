package com.socotra.coremodel;

import lombok.Getter;

public enum ChargeCategory {
  none(false),
  premium(true),
  tax(true),
  fee(true),
  credit(true),
  invoiceFee(true),
  cededPremium(false),
  nonFinancial(false),
  surcharge(true);

  @Getter private final boolean isPayable;

  ChargeCategory(boolean isPayable) {
    this.isPayable = isPayable;
  }
}
