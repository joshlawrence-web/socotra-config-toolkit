package com.socotra.coremodel.constraints;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public interface StaticConstraint<T> {
  boolean isValid(T value);

  T defaultValue();

  default T correct(T value) {
    return isValid(value) ? value : defaultValue();
  }

  default void delegateCorrection(T value, Consumer<StaticConstraint<T>> correctionHandler) {
    if (!isValid(value)) {
      correctionHandler.accept(this);
    }
  }

  default Optional<T> delegatedCorrection(
      T value, Function<StaticConstraint<T>, Optional<T>> correctionHandler) {
    if (isValid(value)) {
      return Optional.ofNullable(value);
    }
    return correctionHandler.apply(this);
  }
}
