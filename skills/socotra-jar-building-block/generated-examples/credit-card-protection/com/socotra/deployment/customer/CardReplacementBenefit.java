package com.socotra.deployment.customer;

import com.socotra.coremodel.*;
import com.socotra.deployment.DeploymentConfig;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum CardReplacementBenefit implements CoverageTerms, Validatable {
  expedited("Expedited Replacement", BigDecimal.valueOf(2), ""),
  global("Global Emergency Replacement", BigDecimal.valueOf(3), ""),
  standard("Standard Replacement", BigDecimal.valueOf(1), ""),
  ERROR_VALUE("Invalid input value", BigDecimal.ZERO, "");

  public static final String TYPE = "CardReplacementBenefit";

  private final String displayName;
  private final BigDecimal value;
  private final String tag;
  private final static CardReplacementBenefit defaultOption = standard;

  CardReplacementBenefit(String displayName, BigDecimal value, String tag) {
    this.displayName = displayName;
    this.value = value;
    this.tag = tag;
  }

  public String type() {
    return TYPE;
  }

  @Override
  public Collection<ValidationItem> validate(DeploymentConfig config, ValidatableContext context) {
    if(this == CardReplacementBenefit.ERROR_VALUE) {
      return List.of(
          ValidationItem.builder()
              .elementType(TYPE)
              .addError("Invalid input value for CardReplacementBenefit")
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

  public static CardReplacementBenefit fromMap(Map<String, Object> map) {
    if (map == null) {
      return null;
    }
    Map<String, Object> m = map.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().toLowerCase(), e -> e.getValue()));
    Object value = m.get("cardreplacementbenefit");
    if (value == null) {
      return null;
    }
    String name = value.toString();
    if (name.isBlank()) {
      return null;
    }
    return Arrays.stream(CardReplacementBenefit.values())
      .filter(e -> e.name().toUpperCase().equals(name.toUpperCase()))
      .findFirst()
      .orElse(CardReplacementBenefit.ERROR_VALUE);
  }

  public static CardReplacementBenefit defaultOption() {
    if(defaultOption == null) {
      throw new IllegalArgumentException("CardReplacementBenefit doesn't have default assignment");
    }
    return defaultOption;
  }

}