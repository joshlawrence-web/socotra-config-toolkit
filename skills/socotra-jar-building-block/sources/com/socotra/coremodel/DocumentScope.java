package com.socotra.coremodel;

public enum DocumentScope {
  transaction,
  policy,
  term,
  segment,
  @Deprecated
  policyUnique,
  @Deprecated
  termUnique,
  invoice
}
