package com.socotra.coremodel;

import com.socotra.coremodel.interfaces.QuoteCore;
import java.time.Instant;
import java.util.function.Function;

public enum SelectionTimeBasis {
  policyStartTime(SelectionTimeBasis::policyStartTime),
  termStartTime(SelectionTimeBasis::termStartTime),
  transactionEffectiveTime(SelectionTimeBasis::transactionEffectiveTime),
  currentTime(SelectionTimeBasis::now);

  private final Function<Object, Instant> function;

  SelectionTimeBasis(Function<Object, Instant> function) {
    this.function = function;
  }

  public Instant selectionTime(Object referenceObject) {
    if (referenceObject instanceof Instant instant) {
      return instant;
    }
    return function.apply(referenceObject);
  }

  private static Instant now(Object referenceObject) {
    return Instant.now();
  }

  private static Instant policyStartTime(Object referenceObject) {
    // policy start time is effective time of issuance transaction
    if (referenceObject instanceof Transaction t) {
      if (t.transactionCategory() != TransactionCategory.issuance) {
        throw new IllegalArgumentException(
            "resource selection by policyStartTime is not supported for non-issuance transaction "
                + t.locator());
      }
      return t.effectiveTime();
    }
    if (referenceObject instanceof QuoteCore q) {
      return startTimeRequired(q);
    }
    if (referenceObject instanceof com.socotra.coremodel.interfaces.Policy policy) {
      return policy.startTime();
    }
    throw new UnsupportedOperationException(
        "resource selection by policyStartTime is not supported for "
            + referenceObject.getClass().getSimpleName());
  }

  private static Instant termStartTime(Object referenceObject) {
    if (referenceObject instanceof Transaction transaction) {
      return switch (transaction.transactionCategory()) {
        case issuance, renewal -> transaction.effectiveTime();
        default ->
            throw new IllegalArgumentException(
                "resource selection by termStartTime is not supported for transactions of category"
                    + transaction.transactionCategory().name());
      };
    }
    if (referenceObject instanceof Term term) {
      return term.startTime();
    }
    if (referenceObject instanceof QuoteCore q) {
      return startTimeRequired(q);
    }
    if (referenceObject instanceof Policy policy) {
      return policy.startTime();
    }
    throw new UnsupportedOperationException(
        "resource selection by termStartTime is not supported for "
            + referenceObject.getClass().getSimpleName());
  }

  private static Instant transactionEffectiveTime(Object referenceObject) {
    if (referenceObject instanceof Transaction t) {
      return t.effectiveTime();
    }
    if (referenceObject instanceof QuoteCore q) {
      return startTimeRequired(q);
    }
    throw new UnsupportedOperationException(
        "resource selection by transactionEffectiveTime is not supported for "
            + referenceObject.getClass().getSimpleName());
  }

  private static Instant startTimeRequired(QuoteCore quote) {
    return quote
        .startTime()
        .orElseThrow(
            () ->
                new IllegalArgumentException("quote startTime is required for resource selection"));
  }
}
