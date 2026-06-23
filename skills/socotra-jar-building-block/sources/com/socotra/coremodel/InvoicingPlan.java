package com.socotra.coremodel;

import java.math.BigDecimal;
import java.util.Map;

public interface InvoicingPlan {
  default InvoiceFeeHandling invoiceFeeHandling() {
    return InvoiceFeeHandling.max;
  }

  default Map<String, BigDecimal> invoiceFeeAmounts() {
    return Map.of();
  }

  default ConsolidateInvoicesOnCancellation consolidateInvoicesOnCancellation() {
    return ConsolidateInvoicesOnCancellation.all;
  }
}
