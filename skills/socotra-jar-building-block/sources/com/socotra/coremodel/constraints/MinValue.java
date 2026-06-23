package com.socotra.coremodel.constraints;

public record MinValue<T extends Comparable<? super T>>(T minValue) implements StaticConstraint<T> {
  @Override
  public boolean isValid(T value) {
    if (value == null) {
      return true;
    }
    return value.compareTo(minValue) >= 0;
  }

  @Override
  public T defaultValue() {
    return minValue;
  }
}
