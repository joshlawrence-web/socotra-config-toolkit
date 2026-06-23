package com.socotra.coremodel;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface ReversalType {
  @JsonIgnore
  ReversalCreditType creditType();

  String name();
}
