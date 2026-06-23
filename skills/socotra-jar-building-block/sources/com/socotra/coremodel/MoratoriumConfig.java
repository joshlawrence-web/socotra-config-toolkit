package com.socotra.coremodel;

import java.time.Instant;
import java.util.Optional;
import lombok.Builder;

@Builder
public record MoratoriumConfig(
    String type,
    MoratoriumApplicationMode applicationMode,
    Instant effectiveTime,
    MoratoriumPolicyMatchCriteria policyMatchCriteria,
    Boolean effectiveTimeWaived,
    Optional<String> description,
    Optional<Instant> endTime,
    Optional<PolicyHoldScope> policyHoldScope,
    Optional<BillingHoldScope> billingHoldScope,
    Optional<String> policyCriteriaSql) {

  public MoratoriumConfig {
    effectiveTimeWaived = effectiveTimeWaived != null && effectiveTimeWaived;
    applicationMode =
        applicationMode == null ? MoratoriumApplicationMode.mandatory : applicationMode;
    endTime = endTime != null ? endTime : Optional.empty();
    description = description != null ? description : Optional.empty();
    policyHoldScope = policyHoldScope != null ? policyHoldScope : Optional.empty();
    billingHoldScope = billingHoldScope != null ? billingHoldScope : Optional.empty();
    policyCriteriaSql = policyCriteriaSql != null ? policyCriteriaSql : Optional.empty();
  }

  public static class MoratoriumConfigBuilder {
    public MoratoriumConfigBuilder policyHoldScope(PolicyHoldScope policyHoldScope) {
      this.policyHoldScope = Optional.ofNullable(policyHoldScope);
      return this;
    }

    public MoratoriumConfigBuilder billingHoldScope(BillingHoldScope billingHoldScope) {
      this.billingHoldScope = Optional.ofNullable(billingHoldScope);
      return this;
    }

    public MoratoriumConfigBuilder endTime(Instant endTime) {
      this.endTime = Optional.ofNullable(endTime);
      return this;
    }

    // Backward compatibility for effectiveTimeWaived to avoid already deployed configs by QA
    // TODO - Remove this method in future releases after the moratorium feature is released to GA
    // (Sometime in September 2025)
    public MoratoriumConfigBuilder waiveEffectiveTime(Boolean waiveEffectiveTime) {
      this.effectiveTimeWaived = waiveEffectiveTime;
      return this;
    }
  }
}
