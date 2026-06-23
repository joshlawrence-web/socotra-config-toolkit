package com.socotra.coremodel;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import lombok.Builder;

@Builder
public record InvoicingPlanDetails(
    InvoiceFeeHandling invoiceFeeHandling,
    Map<String, BigDecimal> invoiceFeeAmounts,
    ConsolidateInvoicesOnCancellation consolidateInvoicesOnCancellation)
    implements InvoicingPlan {
  public InvoicingPlanDetails {
    invoiceFeeAmounts = Objects.requireNonNullElse(invoiceFeeAmounts, Map.of());
    consolidateInvoicesOnCancellation =
        Objects.requireNonNullElse(
            consolidateInvoicesOnCancellation, ConsolidateInvoicesOnCancellation.all);
  }
}
