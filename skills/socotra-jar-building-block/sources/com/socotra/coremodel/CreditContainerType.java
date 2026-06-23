package com.socotra.coremodel;

import java.util.Set;

public enum CreditContainerType {
  invoice,
  account,
  subpayment,
  invoiceItem;
  // for future support: policy, term, etc

  public static final Set<CreditContainerType> supportedPaymentContainerTypes = Set.of(invoice);
  public static final Set<CreditContainerType> supportedDisbursementContainerTypes =
      Set.of(account);
  public static final Set<CreditContainerType> supportedCreditDistributionContainerTypes =
      Set.of(account, invoice);
}
