package com.socotra.deployment.customer;

import com.socotra.coremodel.*;
import com.socotra.deployment.DeploymentConfig;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum ApplianceLimit implements CoverageTerms, Validatable<ApplianceLimit> {
  applianceLimit_100("100%", BigDecimal.valueOf(1), ""),
  applianceLimit_25("25%", BigDecimal.valueOf(0.25), ""),
  applianceLimit_50("50%", BigDecimal.valueOf(0.5), ""),
  ERROR_VALUE("Invalid input value", BigDecimal.ZERO, "");

  public static final String TYPE = "ApplianceLimit";

  private final String displayName;
  private final BigDecimal value;
  private final String tag;
  private final static ApplianceLimit defaultOption = null;

  ApplianceLimit(String displayName, BigDecimal value, String tag) {
    this.displayName = displayName;
    this.value = value;
    this.tag = tag;
  }

  public String type() {
    return TYPE;
  }

  @Override
  public Collection<ValidationItem> validate(DeploymentConfig config, ValidatableContext context) {
    if(this == ApplianceLimit.ERROR_VALUE) {
      return List.of(
          ValidationItem.builder()
              .elementType(TYPE)
              .addError("Invalid input value for ApplianceLimit")
              .build());
    }
    return List.of();
  }

  public CoverageTermsType coverageType() {
    return CoverageTermsType.deductible;
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

  public static ApplianceLimit fromMap(Map<String, Object> map) {
    if (map == null) {
      return null;
    }
    Map<String, Object> m = map.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().toLowerCase(), e -> e.getValue()));
    Object value = m.get("appliancelimit");
    if (value == null) {
      return null;
    }
    String name = value.toString();
    if (name.isBlank()) {
      return null;
    }
    return Arrays.stream(ApplianceLimit.values())
      .filter(e -> e.name().toUpperCase().equals(name.toUpperCase()))
      .findFirst()
      .orElse(ApplianceLimit.ERROR_VALUE);
  }

  public static ApplianceLimit defaultOption() {
    if(defaultOption == null) {
      throw new IllegalArgumentException("ApplianceLimit doesn't have default assignment");
    }
    return defaultOption;
  }

}