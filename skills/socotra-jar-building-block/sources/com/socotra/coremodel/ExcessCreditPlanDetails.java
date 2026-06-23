package com.socotra.coremodel;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.Builder;

@Builder
public record ExcessCreditPlanDetails(
    boolean disburseExcess,
    Optional<String> disbursementType,
    Optional<ExcessCreditExcludeDebits> excludeDebits,
    Map<Currency, BigDecimal> disbursementThresholds,
    Optional<DisbursementState> advanceDisbursementTo,
    Optional<Boolean> autoApplyExcessToInvoicesEnabled,
    Optional<NegativeInvoiceHandling> negativeInvoiceHandling)
    implements ExcessCreditPlan {

  public ExcessCreditPlanDetails {
    advanceDisbursementTo = Objects.requireNonNullElse(advanceDisbursementTo, Optional.empty());
    autoApplyExcessToInvoicesEnabled =
        Objects.requireNonNullElse(autoApplyExcessToInvoicesEnabled, Optional.empty());
    negativeInvoiceHandling = Objects.requireNonNullElse(negativeInvoiceHandling, Optional.empty());
  }
}
