package com.socotra.coremodel;

import java.util.Map;

public interface DelinquencyPlan {
  Integer gracePeriodDays();

  String lapseTransactionType();

  LapseAdvanceState advanceLapseTo();

  default DelinquencyLevel delinquencyLevel() {
    return DelinquencyLevel.policy;
  }

  default Map<String, DelinquencyEventConfiguration> events() {
    return Map.of();
  }
}
