package com.socotra.coremodel;

import java.util.Optional;

public interface AutoRenewalPlan {
  boolean generateAutoRenewals();

  Optional<String> renewalTransactionType();

  Optional<Integer> renewalCreateLeadDays();

  Optional<Integer> renewalAcceptLeadDays();

  Optional<Integer> renewalIssueLeadDays();

  Optional<Integer> newTermDuration();
}
