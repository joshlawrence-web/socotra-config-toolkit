package com.socotra.deployment.customer;

import com.socotra.coremodel.*;
import com.socotra.deployment.DeploymentConfig;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum PremiumDeductible implements CoverageTerms, Validatable {
  premium100("$100", BigDecimal.valueOf(100), ""),
  premium25("$25", BigDecimal.valueOf(25), ""),
  premium50("$50", BigDecimal.valueOf(50), ""),
  ERROR_VALUE("Invalid input value", BigDecimal.ZERO, "");

  public static final String TYPE = "PremiumDeductible";

  private final String displayName;
  private final BigDecimal value;
  private final String tag;
  private final static PremiumDeductible defaultOption = premium50;

  PremiumDeductible(String displayName, BigDecimal value, String tag) {
    this.displayName = displayName;
    this.value = value;
    this.tag = tag;
  }

  public String type() {
    return TYPE;
  }

  @Override
  public Collection<ValidationItem> validate(DeploymentConfig config, ValidatableContext context) {
    if(this == PremiumDeductible.ERROR_VALUE) {
      return List.of(
          ValidationItem.builder()
              .elementType(TYPE)
              .addError("Invalid input value for PremiumDeductible")
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

  public static PremiumDeductible fromMap(Map<String, Object> map) {
    if (map == null) {
      return null;
    }
    Map<String, Object> m = map.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().toLowerCase(), e -> e.getValue()));
    Object value = m.get("premiumdeductible");
    if (value == null) {
      return null;
    }
    String name = value.toString();
    if (name.isBlank()) {
      return null;
    }
    return Arrays.stream(PremiumDeductible.values())
      .filter(e -> e.name().toUpperCase().equals(name.toUpperCase()))
      .findFirst()
      .orElse(PremiumDeductible.ERROR_VALUE);
  }

  public static PremiumDeductible defaultOption() {
    if(defaultOption == null) {
      throw new IllegalArgumentException("PremiumDeductible doesn't have default assignment");
    }
    return defaultOption;
  }

}