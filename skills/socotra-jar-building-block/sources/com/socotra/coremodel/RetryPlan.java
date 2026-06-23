package com.socotra.coremodel;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;

@Builder(toBuilder = true)
public record RetryPlan(int numberOfAttempts, List<BigDecimal> hoursBetweenAttempts) {
  public BigDecimal waitTimeForAttempt(int attempt) {
    if (attempt < 1 || attempt > numberOfAttempts) {
      throw new IllegalArgumentException(
          "Attempt number must be between 1 and " + numberOfAttempts);
    }
    int index = Math.min(attempt - 1, hoursBetweenAttempts.size() - 1);

    return hoursBetweenAttempts.get(index);
  }
}
