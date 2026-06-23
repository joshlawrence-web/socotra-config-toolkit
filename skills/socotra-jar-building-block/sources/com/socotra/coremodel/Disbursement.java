package com.socotra.coremodel;

import com.socotra.platform.tools.ULID;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface Disbursement<T extends CustomerObject>
    extends CustomerObjectWithData<T>, NumberingObject {
  ULID locator();

  DisbursementState state();

  BigDecimal amount();

  String currency();

  List<CreditItem> sources();

  Optional<ULID> accountLocator();

  Optional<ULID> financialInstrumentLocator();

  Optional<ExternalCashTransactionMethod> transactionMethod();

  Optional<String> transactionNumber();

  @Override
  T data();

  default Optional<String> disbursementNumber() {
    return Optional.empty();
  }
}
