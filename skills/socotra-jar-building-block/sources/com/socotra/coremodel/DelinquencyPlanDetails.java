package com.socotra.coremodel;

import java.util.Map;
import lombok.Builder;

@Builder
public record DelinquencyPlanDetails(
    Integer gracePeriodDays,
    String lapseTransactionType,
    LapseAdvanceState advanceLapseTo,
    DelinquencyLevel delinquencyLevel,
    Map<String, DelinquencyEventConfiguration> events)
    implements DelinquencyPlan {}
