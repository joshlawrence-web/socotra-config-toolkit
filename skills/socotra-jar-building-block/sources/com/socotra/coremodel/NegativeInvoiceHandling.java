package com.socotra.coremodel;

public interface NegativeInvoiceHandling {
  default SettleNegativeInvoicesOption automaticallySettleNegativeInvoices() {
    return SettleNegativeInvoicesOption.toCreditBalance;
  }

  default boolean prioritizeOverlappingCoveragePeriods() {
    return true;
  }

  default TargetInvoices targetInvoices() {
    return TargetInvoices.allOpenInvoices;
  }

  default TargetInvoicePriority targetInvoicePriority() {
    return TargetInvoicePriority.smallestFirst;
  }

  default CreditProcessingMode processingMode() {
    return CreditProcessingMode.accountLevel;
  }

  default boolean yieldExcessToCreditBalance() {
    return true;
  }
}
