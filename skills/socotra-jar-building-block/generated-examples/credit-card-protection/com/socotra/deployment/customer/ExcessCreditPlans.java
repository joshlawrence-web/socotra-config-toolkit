package com.socotra.deployment.customer;

import com.socotra.coremodel.*;
import java.util.*;
import java.math.BigDecimal;


public class ExcessCreditPlans {
    public static final Map<String, ExcessCreditPlanDetails> PLANS = Map.ofEntries(
        Map.entry("StandardExcessCreditPlan",
            ExcessCreditPlanDetails.builder()
                .disburseExcess(false)
                .disbursementType(Optional.empty())
                .excludeDebits(Optional.empty())
                .disbursementThresholds(
                    Map.of()
                )
            .build()
        )
    );

}
