package com.socotra.coremodel;

import java.util.Collection;
import java.util.List;
import lombok.Builder;

@Builder
public record MoratoriumProductRule(
    String product, MoratoriumRuleOperator operator, Collection<MoratoriumRule> rules) {
  public MoratoriumProductRule {
    rules = rules == null ? List.of() : rules;
  }
}
