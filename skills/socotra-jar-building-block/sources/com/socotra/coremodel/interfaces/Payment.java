package com.socotra.coremodel.interfaces;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.socotra.coremodel.*;
import com.socotra.platform.tools.ULID;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface Payment<T> extends CustomerObjectWithData<T> {

  @JsonIgnore(value = false)
  String type();

  ULID locator();

  PaymentState paymentState();

  BigDecimal amount();

  String currency();

  Collection<CreditItem> targets();

  Optional<ULID> accountLocator();

  Optional<ULID> externalCashTransactionLocator();

  default Optional<PaymentMode> paymentMode() {
    return Optional.empty();
  }

  default Optional<ULID> aggregatePaymentLocator() {
    return Optional.empty();
  }

  default Collection<Subpayment> subpayments() {
    return List.of();
  }

  default Optional<Instant> anonymizedAt() {
    return Optional.empty();
  }

  default Optional<Instant> autopayTime() {
    return Optional.empty();
  }

  default Optional<Instant> nextRequestTime() {
    return Optional.empty();
  }

  default Collection<PaymentRequestExecution> paymentExecutionHistory() {
    return List.of();
  }

  default Optional<String> retryPlanName() {
    return Optional.empty();
  }
}
