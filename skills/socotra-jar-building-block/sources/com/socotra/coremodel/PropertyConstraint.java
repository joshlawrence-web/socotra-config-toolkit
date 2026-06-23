package com.socotra.coremodel;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.NonNull;

@Builder
public record PropertyConstraint(
    @NonNull String table, @NonNull String column, Map<String, ConditionValue> where) {
  public PropertyConstraint {
    if (where == null) {
      where = Map.of();
    }
  }

  @Builder
  public record ConditionValue(String key, Collection<String> values) {
    public ConditionValue {
      if ((key == null) == (values == null || values.isEmpty())) {
        // This is enforced by json schema, so should not happen normally at this point
        throw new IllegalArgumentException(
            "PropertyConstraint.ConditionValue must have exactly one of either key or values");
      }
    }

    public static ConditionValue ofKey(String key) {
      return new ConditionValue(key, null);
    }

    public static ConditionValue ofValues(Collection<String> values) {
      return new ConditionValue(null, values);
    }

    // Do not modify, this is used for code generation
    @Override
    public String toString() {
      if (key != null) {
        return "PropertyConstraint.ConditionValue.ofKey(\"" + key + "\")";
      } else {
        return "PropertyConstraint.ConditionValue.ofValues(List.of("
            + values.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(", "))
            + "))";
      }
    }
  }
}
