package com.socotra.coremodel.constraints;

import java.util.Collection;

public record Required<T>(T defaultValue) implements StaticConstraint<T> {
  public Required() {
    this(null);
  }

  @Override
  public boolean isValid(T value) {
    return switch (value) {
      case null -> false;
      case String str when str.trim().isEmpty() -> false;
      case Collection<?> collection when collection.isEmpty() -> false;
      default -> true;
    };
  }
}
