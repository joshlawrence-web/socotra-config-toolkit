package com.socotra.coremodel;

import java.util.*;
import lombok.Builder;

@Builder
public record MoratoriumPolicyMatchCriteria(
    Map<String, Set<String>> criteriaValues, Map<String, MoratoriumProductRule> productRule) {
  public MoratoriumPolicyMatchCriteria {
    productRule = productRule == null ? Map.of() : productRule;
  }
}
