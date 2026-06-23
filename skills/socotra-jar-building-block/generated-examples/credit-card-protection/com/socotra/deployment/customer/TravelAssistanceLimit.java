package com.socotra.deployment.customer;

import com.socotra.coremodel.*;
import com.socotra.deployment.DeploymentConfig;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum TravelAssistanceLimit implements CoverageTerms, Validatable {
  travel10k("$10,000", BigDecimal.valueOf(10000), ""),
  travel25k("$25,000", BigDecimal.valueOf(25000), ""),
  travel50k("$50,000", BigDecimal.valueOf(50000), ""),
  ERROR_VALUE("Invalid input value", BigDecimal.ZERO, "");

  public static final String TYPE = "TravelAssistanceLimit";

  private final String displayName;
  private final BigDecimal value;
  private final String tag;
  private final static TravelAssistanceLimit defaultOption = travel10k;

  TravelAssistanceLimit(String displayName, BigDecimal value, String tag) {
    this.displayName = displayName;
    this.value = value;
    this.tag = tag;
  }

  public String type() {
    return TYPE;
  }

  @Override
  public Collection<ValidationItem> validate(DeploymentConfig config, ValidatableContext context) {
    if(this == TravelAssistanceLimit.ERROR_VALUE) {
      return List.of(
          ValidationItem.builder()
              .elementType(TYPE)
              .addError("Invalid input value for TravelAssistanceLimit")
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

  public static TravelAssistanceLimit fromMap(Map<String, Object> map) {
    if (map == null) {
      return null;
    }
    Map<String, Object> m = map.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().toLowerCase(), e -> e.getValue()));
    Object value = m.get("travelassistancelimit");
    if (value == null) {
      return null;
    }
    String name = value.toString();
    if (name.isBlank()) {
      return null;
    }
    return Arrays.stream(TravelAssistanceLimit.values())
      .filter(e -> e.name().toUpperCase().equals(name.toUpperCase()))
      .findFirst()
      .orElse(TravelAssistanceLimit.ERROR_VALUE);
  }

  public static TravelAssistanceLimit defaultOption() {
    if(defaultOption == null) {
      throw new IllegalArgumentException("TravelAssistanceLimit doesn't have default assignment");
    }
    return defaultOption;
  }

}