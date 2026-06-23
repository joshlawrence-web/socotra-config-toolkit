package com.socotra.coremodel;

/** For data fetcher search requests to billing service */
public enum BillingReferenceType {
  invoice,
  invoiceDetail,
  payment,
  delinquencyEvents,
  installment,
  installmentLattice,
}
