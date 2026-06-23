package com.socotra.deployment.workmanagement;

import java.math.BigDecimal;
import java.util.Optional;
import lombok.Builder;
import lombok.NonNull;

@Builder
public record TasksConfig(
    @NonNull String category,
    Optional<BigDecimal> defaultDeadlineDays,
    Boolean blocksUnderwriting,
    Optional<String> numberingPlan,
    Optional<String> numberingString) {
  public TasksConfig {
    if (defaultDeadlineDays == null) {
      defaultDeadlineDays = Optional.empty();
    }
    if (blocksUnderwriting == null) {
      blocksUnderwriting = false;
    }
    if (numberingPlan == null) {
      numberingPlan = Optional.empty();
    }
    if (numberingString == null) {
      numberingString = Optional.empty();
    }
  }

  public TasksConfig(
      String category, Optional<BigDecimal> defaultDeadlineDays, Boolean blocksUnderwriting) {
    this(category, defaultDeadlineDays, blocksUnderwriting, Optional.empty(), Optional.empty());
  }
}
