package com.socotra.coremodel;

import lombok.Builder;

@Builder
public record NegativeInvoiceHandlingDetails(
    SettleNegativeInvoicesOption automaticallySettleNegativeInvoices,
    boolean prioritizeOverlappingCoveragePeriods,
    TargetInvoices targetInvoices,
    TargetInvoicePriority targetInvoicePriority,
    CreditProcessingMode processingMode,
    boolean yieldExcessToCreditBalance)
    implements NegativeInvoiceHandling {}
