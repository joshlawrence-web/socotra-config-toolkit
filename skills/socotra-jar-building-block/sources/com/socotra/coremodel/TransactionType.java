package com.socotra.coremodel;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface TransactionType {
  @JsonIgnore
  TransactionCategory category();

  @JsonIgnore
  default boolean costBearing() {
    return true;
  }

  default String name() {
    return category().name();
  }
}
