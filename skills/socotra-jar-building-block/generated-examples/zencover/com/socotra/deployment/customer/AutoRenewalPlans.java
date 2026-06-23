package com.socotra.deployment.customer;

import com.socotra.coremodel.*;
import java.util.*;


public class AutoRenewalPlans {
    public static final Map<String, AutoRenewalPlanDetails> PLANS = Map.ofEntries(
        Map.entry("StandardAutoRenewalPlan",
            AutoRenewalPlanDetails.builder()
                .generateAutoRenewals(true)
                .renewalTransactionType(Optional.empty())
                .renewalCreateLeadDays(Optional.empty())
                .renewalAcceptLeadDays(Optional.empty())
                .renewalIssueLeadDays(Optional.empty())
                .newTermDuration(Optional.empty())
            .build()
        ),
        Map.entry("Standard_60_30_1AutoRenewalPlan",
            AutoRenewalPlanDetails.builder()
                .generateAutoRenewals(true)
                .renewalTransactionType(Optional.of("StandardRenewal"))
                .renewalCreateLeadDays(Optional.of(60))
                .renewalAcceptLeadDays(Optional.of(30))
                .renewalIssueLeadDays(Optional.of(1))
                .newTermDuration(Optional.of(1))
            .build()
        )
    );
}
