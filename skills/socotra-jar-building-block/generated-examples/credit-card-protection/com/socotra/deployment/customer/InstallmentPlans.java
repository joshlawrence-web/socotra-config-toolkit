package com.socotra.deployment.customer;

import com.socotra.coremodel.*;
import java.util.*;
import java.math.BigDecimal;


public class InstallmentPlans {
    public static final Map<String, InstallmentPlanDetails> PLANS = Map.ofEntries(
        Map.entry("StandardInstallmentPlan",
            InstallmentPlanDetails.builder()
                .cadence(InstallmentCadence.fullPay)
                .anchorMode(InstallmentAnchorMode.termStartDay)
                .generateLeadDays(14)
                .dueLeadDays(0)
                .installmentWeights(List.of())
                .maxInstallmentsPerTerm(1000)
                .autopayLeadDays(BigDecimal.valueOf(1))
            .build()
        )
    );
}
