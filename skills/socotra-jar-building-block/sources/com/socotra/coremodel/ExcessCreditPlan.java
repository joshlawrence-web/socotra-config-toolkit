package com.socotra.coremodel;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Map;
import java.util.Optional;

public interface ExcessCreditPlan {
  boolean disburseExcess();

  Optional<String> disbursementType();

  Optional<ExcessCreditExcludeDebits> excludeDebits();

  Map<Currency, BigDecimal> disbursementThresholds();

  default Optional<DisbursementState> advanceDisbursementTo() {
    return Optional.of(DisbursementState.executed);
  }

  default Optional<Boolean> autoApplyExcessToInvoicesEnabled() {
    return Optional.of(Boolean.FALSE);
  }

  default Optional<NegativeInvoiceHandling> negativeInvoiceHandling() {
    return Optional.empty();
  }
}
