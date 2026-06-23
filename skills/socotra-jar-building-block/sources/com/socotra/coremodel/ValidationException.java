package com.socotra.coremodel;

public class ValidationException extends Exception {
  private final ValidationResult validationResult;

  public ValidationException(String message, ValidationResult result) {
    super(message);
    this.validationResult = result;
  }

  public ValidationResult getValidationResult() {
    return validationResult;
  }
}
