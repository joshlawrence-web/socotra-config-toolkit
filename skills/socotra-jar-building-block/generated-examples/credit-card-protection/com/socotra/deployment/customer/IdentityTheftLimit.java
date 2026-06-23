package com.socotra.deployment.customer;

import com.socotra.coremodel.*;
import com.socotra.deployment.DeploymentConfig;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum IdentityTheftLimit implements CoverageTerms, Validatable {
  identity100k("$100,000", BigDecimal.valueOf(100000), ""),
  identity25k("$25,000", BigDecimal.valueOf(25000), ""),
  identity50k("$50,000", BigDecimal.valueOf(50000), ""),
  ERROR_VALUE("Invalid input value", BigDecimal.ZERO, "");

  public static final String TYPE = "IdentityTheftLimit";

  private final String displayName;
  private final BigDecimal value;
  private final String tag;
  private final static IdentityTheftLimit defaultOption = identity25k;

  IdentityTheftLimit(String displayName, BigDecimal value, String tag) {
    this.displayName = displayName;
    this.value = value;
    this.tag = tag;
  }

  public String type() {
    return TYPE;
  }

  @Override
  public Collection<ValidationItem> validate(DeploymentConfig config, ValidatableContext context) {
    if(this == IdentityTheftLimit.ERROR_VALUE) {
      return List.of(
          ValidationItem.builder()
              .elementType(TYPE)
              .addError("Invalid input value for IdentityTheftLimit")
              .build());
    }
    return List.of();
  }

  public CoverageTermsType coverageType() {
    return CoverageTermsType.limit;
  }

  public boolean isDefault() {
    return this == defaultOption;
  }

  public String displayName() {
    return this.displayName;
  }

  public BigDecimal value() {
    return value;
  }

  public String tag() {
    return tag;
  }

  public static IdentityTheftLimit fromMap(Map<String, Object> map) {
    if (map == null) {
      return null;
    }
    Map<String, Object> m = map.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().toLowerCase(), e -> e.getValue()));
    Object value = m.get("identitytheftlimit");
    if (value == null) {
      return null;
    }
    String name = value.toString();
    if (name.isBlank()) {
      return null;
    }
    return Arrays.stream(IdentityTheftLimit.values())
      .filter(e -> e.name().toUpperCase().equals(name.toUpperCase()))
      .findFirst()
      .orElse(IdentityTheftLimit.ERROR_VALUE);
  }

  public static IdentityTheftLimit defaultOption() {
    if(defaultOption == null) {
      throw new IllegalArgumentException("IdentityTheftLimit doesn't have default assignment");
    }
    return defaultOption;
  }

}