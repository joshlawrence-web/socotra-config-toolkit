package com.socotra.coremodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Optional;

public interface ChargeType {
  @JsonIgnore
  ChargeCategory category();

  String name();

  default ChargeInvoicing invoicing() {
    return ChargeInvoicing.scheduled;
  }

  default ChargeHandling handling() {
    return ChargeHandling.normal;
  }

  default Optional<Boolean> transactionBundlingEnabled() {
    return Optional.of(Boolean.FALSE);
  }

  @JsonIgnore
  default boolean equals(ChargeType other) {
    return category().equals(other.category())
        && name().equals(other.name())
        && invoicing().equals(other.invoicing())
        && handling().equals(other.handling())
        && transactionBundlingEnabled().equals(other.transactionBundlingEnabled());
  }
}
