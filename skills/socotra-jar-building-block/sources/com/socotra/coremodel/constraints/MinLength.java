package com.socotra.coremodel.constraints;

public record MinLength(int minLength) implements StaticConstraint<String> {
  @Override
  public boolean isValid(String value) {
    if (value == null) {
      return true;
    }
    return value.length() >= minLength;
  }

  @Override
  public String defaultValue() {
    return null;
  }
}
