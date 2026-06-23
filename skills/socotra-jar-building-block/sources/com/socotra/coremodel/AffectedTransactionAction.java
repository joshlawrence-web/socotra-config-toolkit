package com.socotra.coremodel;

public enum AffectedTransactionAction {
  reversed,
  @Deprecated(since = "Jan 2, 2024", forRemoval = true)
  reapplied,
  invalidated
}
