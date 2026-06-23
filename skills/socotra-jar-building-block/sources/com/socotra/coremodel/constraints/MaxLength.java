package com.socotra.coremodel.constraints;

public record MaxLength(int maxLength) implements StaticConstraint<String> {
  @Override
  public boolean isValid(String value) {
    if (value == null) {
      return true;
    }
    return value.length() <= maxLength;
  }

  @Override
  public String defaultValue() {
    return null;
  }
}
