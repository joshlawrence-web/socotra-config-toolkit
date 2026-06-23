package com.socotra.coremodel;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class UserInputException extends Exception {
  private final Collection<String> errors;

  public UserInputException(String message, Collection<String> errors) {
    super(message);
    if (errors == null) {
      errors = List.of();
    }
    this.errors = List.copyOf(errors);
  }

  public Collection<String> getErrors() {
    return errors;
  }

  public String getErrorsAsString() {
    return errors.stream().sorted().collect(Collectors.joining(", "));
  }
}
