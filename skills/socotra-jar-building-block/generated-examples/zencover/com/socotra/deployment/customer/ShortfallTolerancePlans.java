package com.socotra.deployment.customer;

import com.socotra.coremodel.*;
import java.util.*;
import java.math.BigDecimal;


public class ShortfallTolerancePlans {
    public static final Map<String, ShortfallTolerancePlanDetails> PLANS = Map.ofEntries(
        Map.entry("BasicPlanShortfallTolerancePlan",
            ShortfallTolerancePlanDetails.builder()
                .currencyTolerances(
                    Map.ofEntries(
                        Map.entry("CAD", new BigDecimal("1.35")),
                        Map.entry("USD", new BigDecimal("1.00"))
                    )
                )
            .build()
        )
    );

}
