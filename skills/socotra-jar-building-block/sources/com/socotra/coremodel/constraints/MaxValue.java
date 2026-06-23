package com.socotra.coremodel.constraints;

public record MaxValue<T extends Comparable<? super T>>(T maxValue) implements StaticConstraint<T> {
  @Override
  public boolean isValid(T value) {
    if (value == null) {
      return true;
    }
    return value.compareTo(maxValue) <= 0;
  }

  @Override
  public T defaultValue() {
    return maxValue;
  }
}
