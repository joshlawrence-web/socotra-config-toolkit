package com.socotra.coremodel;

import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record BillingHoldScope(
    Boolean policyInvoicingHold,
    Boolean policyDelinquencyHold,
    Boolean autopayHold,
    BigDecimal deferredInvoiceDueOffsetDays,
    String displayName) {

  public BillingHoldScope {
    policyInvoicingHold = policyInvoicingHold != null && policyInvoicingHold;
    policyDelinquencyHold = policyDelinquencyHold != null && policyDelinquencyHold;
    autopayHold = autopayHold != null && autopayHold;
  }
}
