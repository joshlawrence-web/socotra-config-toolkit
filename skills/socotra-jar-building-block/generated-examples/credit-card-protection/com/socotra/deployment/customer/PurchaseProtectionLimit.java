package com.socotra.deployment.customer;

import com.socotra.coremodel.*;
import com.socotra.deployment.DeploymentConfig;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum PurchaseProtectionLimit implements CoverageTerms, Validatable {
  purchase10k("$10,000 per item", BigDecimal.valueOf(10000), ""),
  purchase25k("$25,000 per item", BigDecimal.valueOf(25000), ""),
  purchase5k("$5,000 per item", BigDecimal.valueOf(5000), ""),
  ERROR_VALUE("Invalid input value", BigDecimal.ZERO, "");

  public static final String TYPE = "PurchaseProtectionLimit";

  private final String displayName;
  private final BigDecimal value;
  private final String tag;
  private final static PurchaseProtectionLimit defaultOption = purchase5k;

  PurchaseProtectionLimit(String displayName, BigDecimal value, String tag) {
    this.displayName = displayName;
    this.value = value;
    this.tag = tag;
  }

  public String type() {
    return TYPE;
  }

  @Override
  public Collection<ValidationItem> validate(DeploymentConfig config, ValidatableContext context) {
    if(this == PurchaseProtectionLimit.ERROR_VALUE) {
      return List.of(
          ValidationItem.builder()
              .elementType(TYPE)
              .addError("Invalid input value for PurchaseProtectionLimit")
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

  public static PurchaseProtectionLimit fromMap(Map<String, Object> map) {
    if (map == null) {
      return null;
    }
    Map<String, Object> m = map.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().toLowerCase(), e -> e.getValue()));
    Object value = m.get("purchaseprotectionlimit");
    if (value == null) {
      return null;
    }
    String name = value.toString();
    if (name.isBlank()) {
      return null;
    }
    return Arrays.stream(PurchaseProtectionLimit.values())
      .filter(e -> e.name().toUpperCase().equals(name.toUpperCase()))
      .findFirst()
      .orElse(PurchaseProtectionLimit.ERROR_VALUE);
  }

  public static PurchaseProtectionLimit defaultOption() {
    if(defaultOption == null) {
      throw new IllegalArgumentException("PurchaseProtectionLimit doesn't have default assignment");
    }
    return defaultOption;
  }

}