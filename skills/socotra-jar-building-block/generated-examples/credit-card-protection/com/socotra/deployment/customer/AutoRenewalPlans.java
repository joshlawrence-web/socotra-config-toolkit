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
        )
    );
}
