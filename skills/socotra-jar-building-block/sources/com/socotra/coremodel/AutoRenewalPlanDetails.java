package com.socotra.coremodel;

import java.util.Optional;
import lombok.Builder;

@Builder
public record AutoRenewalPlanDetails(
    boolean generateAutoRenewals,
    Optional<String> renewalTransactionType,
    Optional<Integer> renewalCreateLeadDays,
    Optional<Integer> renewalAcceptLeadDays,
    Optional<Integer> renewalIssueLeadDays,
    Optional<Integer> newTermDuration)
    implements AutoRenewalPlan {}
