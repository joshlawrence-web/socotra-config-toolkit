package com.socotra.deployment.customer;

import com.socotra.coremodel.*;
import java.math.BigDecimal;
import java.util.*;


public class RetryPlans {
    public static final Map<String, RetryPlan> PLANS = Map.ofEntries(
        Map.entry("StandardRetryPlan",
            RetryPlan.builder()
                .numberOfAttempts(0)
                .hoursBetweenAttempts(List.of())
                .build()
        )
    );
}
