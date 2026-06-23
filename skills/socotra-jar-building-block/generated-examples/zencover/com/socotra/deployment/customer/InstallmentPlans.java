package com.socotra.deployment.customer;

import com.socotra.coremodel.*;
import java.util.*;
import java.math.BigDecimal;


public class InstallmentPlans {
    public static final Map<String, InstallmentPlanDetails> PLANS = Map.ofEntries(
        Map.entry("Monthly12InstallmentPlan",
            InstallmentPlanDetails.builder()
                .cadence(InstallmentCadence.monthly)
                .anchorMode(InstallmentAnchorMode.termStartDay)
                .generateLeadDays(0)
                .dueLeadDays(0)
                .installmentWeights(List.of())
                .maxInstallmentsPerTerm(12)
                .autopayLeadDays(BigDecimal.valueOf(1))
            .build()
        ),
        Map.entry("QuarterlyInstallmentPlan",
            InstallmentPlanDetails.builder()
                .cadence(InstallmentCadence.quarterly)
                .anchorMode(InstallmentAnchorMode.termStartDay)
                .generateLeadDays(0)
                .dueLeadDays(0)
                .installmentWeights(List.of())
                .maxInstallmentsPerTerm(4)
                .autopayLeadDays(BigDecimal.valueOf(1))
            .build()
        ),
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
        ),
        Map.entry("FullPayInstallmentPlan",
            InstallmentPlanDetails.builder()
                .cadence(InstallmentCadence.fullPay)
                .anchorMode(InstallmentAnchorMode.termStartDay)
                .generateLeadDays(10)
                .dueLeadDays(7)
                .installmentWeights(List.of())
                .maxInstallmentsPerTerm(1000)
                .autopayLeadDays(BigDecimal.valueOf(1))
            .build()
        )
    );
}
