package com.socotra.coremodel.constraints;

import java.util.Set;

public record Options<T>(Set<T> options, T defaultValue) implements StaticConstraint<T> {
  public Options(Set<T> options) {
    this(options, null);
  }

  @Override
  public boolean isValid(T value) {
    if (value == null) {
      return true;
    }
    return options.contains(value);
  }
}
