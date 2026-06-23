package com.socotra.coremodel.constraints;

import java.util.regex.Pattern;

public record Regex(Pattern pattern) implements StaticConstraint<String> {

  public Regex(String regex) {
    this(Pattern.compile(regex));
  }

  @Override
  public boolean isValid(String value) {
    if (value == null) {
      return true;
    }
    return pattern.matcher(value).matches();
  }

  @Override
  public String defaultValue() {
    return null;
  }
}
