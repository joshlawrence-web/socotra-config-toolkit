package com.socotra.deployment.workmanagement;

import java.util.function.BiPredicate;
import lombok.NonNull;

public record QualificationRequirement(
    @NonNull String category,
    @NonNull String level,
    @NonNull BiPredicate<Integer, Integer> validator) {
  public static QualificationRequirement minimumOf(String category, String level) {
    return new QualificationRequirement(
        category, level, (required, provided) -> provided.compareTo(required) >= 0);
  }

  public static QualificationRequirement exactOf(String category, String level) {
    return new QualificationRequirement(
        category, level, (required, provided) -> provided.compareTo(required) == 0);
  }
}
