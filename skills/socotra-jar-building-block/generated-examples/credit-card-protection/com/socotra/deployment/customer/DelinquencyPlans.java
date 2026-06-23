package com.socotra.deployment.customer;

import com.socotra.coremodel.*;
import java.util.*;
import java.math.BigDecimal;


public class DelinquencyPlans {
    public static final Map<String, DelinquencyPlanDetails> PLANS = Map.ofEntries(
        Map.entry("StandardDelinquencyPlan",
            DelinquencyPlanDetails.builder()
                .gracePeriodDays(7)
                .lapseTransactionType("cancellation")
                .advanceLapseTo(LapseAdvanceState.issued)
                .delinquencyLevel(DelinquencyLevel.policy)
                .events(
                    Map.ofEntries(
                    )
                )
            .build()
        )
    );
}
