package com.socotra.coremodel;

import java.util.Set;
import lombok.Builder;

@Builder
public record PolicyHoldScope(
    Set<TransactionCategory> transactionCategory,
    Set<String> transactionType,
    boolean allowStaticData) {
  public PolicyHoldScope {
    transactionCategory = transactionCategory == null ? Set.of() : Set.copyOf(transactionCategory);
    transactionType = transactionType == null ? Set.of() : Set.copyOf(transactionType);
    if (transactionCategory.isEmpty() && transactionType.isEmpty() && !allowStaticData) {
      throw new IllegalArgumentException("at least one property must be set");
    }
  }
}
