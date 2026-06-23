package com.socotra.coremodel;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.socotra.platform.tools.ULID;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface Payment<T extends CustomerObject>
    extends CustomerObjectWithData<T>, NumberingObject {
  ULID locator();

  @Deprecated
  default PaymentState state() {
    return paymentState();
  }

  @JsonAlias("state")
  PaymentState paymentState();

  @Override
  T data();

  BigDecimal amount();

  String currency();

  List<CreditItem> targets();

  Optional<ULID> accountLocator();

  Optional<ULID> financialInstrumentLocator();

  Optional<ExternalCashTransactionMethod> transactionMethod();

  Optional<String> transactionNumber();

  default Optional<PaymentMode> paymentMode() {
    return Optional.empty();
  }

  default Optional<ULID> aggregatePaymentLocator() {
    return Optional.empty();
  }
}
