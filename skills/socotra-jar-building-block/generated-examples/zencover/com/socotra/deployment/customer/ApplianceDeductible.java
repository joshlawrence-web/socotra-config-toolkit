package com.socotra.deployment.customer;

import com.socotra.coremodel.*;
import com.socotra.deployment.DeploymentConfig;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum ApplianceDeductible implements CoverageTerms, Validatable<ApplianceDeductible> {
  applianceDeductible_100("£100", BigDecimal.valueOf(100), ""),
  applianceDeductible_200("£200", BigDecimal.valueOf(200), ""),
  applianceDeductible_50("£50", BigDecimal.valueOf(50), ""),
  ERROR_VALUE("Invalid input value", BigDecimal.ZERO, "");

  public static final String TYPE = "ApplianceDeductible";

  private final String displayName;
  private final BigDecimal value;
  private final String tag;
  private final static ApplianceDeductible defaultOption = null;

  ApplianceDeductible(String displayName, BigDecimal value, String tag) {
    this.displayName = displayName;
    this.value = value;
    this.tag = tag;
  }

  public String type() {
    return TYPE;
  }

  @Override
  public Collection<ValidationItem> validate(DeploymentConfig config, ValidatableContext context) {
    if(this == ApplianceDeductible.ERROR_VALUE) {
      return List.of(
          ValidationItem.builder()
              .elementType(TYPE)
              .addError("Invalid input value for ApplianceDeductible")
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

  public static ApplianceDeductible fromMap(Map<String, Object> map) {
    if (map == null) {
      return null;
    }
    Map<String, Object> m = map.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().toLowerCase(), e -> e.getValue()));
    Object value = m.get("appliancedeductible");
    if (value == null) {
      return null;
    }
    String name = value.toString();
    if (name.isBlank()) {
      return null;
    }
    return Arrays.stream(ApplianceDeductible.values())
      .filter(e -> e.name().toUpperCase().equals(name.toUpperCase()))
      .findFirst()
      .orElse(ApplianceDeductible.ERROR_VALUE);
  }

  public static ApplianceDeductible defaultOption() {
    if(defaultOption == null) {
      throw new IllegalArgumentException("ApplianceDeductible doesn't have default assignment");
    }
    return defaultOption;
  }

}