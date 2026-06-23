package com.socotra.deployment.customer;

import com.socotra.coremodel.*;
import java.util.*;
import java.math.BigDecimal;


public class NumberingPlans {
    public static final Map<String, NumberingPlanDetails> PLANS = Map.ofEntries(
        Map.entry("ZCInvoiceNumberingPlanNumberingPlan",
            NumberingPlanDetails.builder()
                .name("ZCInvoiceNumberingPlan")
                .initialCoreNumber("000000001")
                .format("\\I\\N\\V-########-\\D\\G")
                .copyFromQuote(false)
                .termNumberFormat("")
                .coreNumberFormat("########")
                .placeholders(Set.of())
                .quoteNumberFormat("")
                .initialQuoteCoreNumber("")
                .quoteCoreNumberFormat("")
                .quotePlaceholders(Set.of())
                .productScopes(Set.of(
                        NumberingProductScope.policy,
                        NumberingProductScope.quote
                    )
                )
            .build()
        ),
        Map.entry("ZCPolicyNumberingPlanNumberingPlan",
            NumberingPlanDetails.builder()
                .name("ZCPolicyNumberingPlan")
                .initialCoreNumber("000000001")
                .format("\\D\\G-########")
                .copyFromQuote(true)
                .termNumberFormat("{policyNumber}-{termNumberPlusOne}")
                .coreNumberFormat("########")
                .placeholders(Set.of())
                .quoteNumberFormat("")
                .initialQuoteCoreNumber("")
                .quoteCoreNumberFormat("")
                .quotePlaceholders(Set.of())
                .productScopes(Set.of(
                        NumberingProductScope.policy,
                        NumberingProductScope.quote
                    )
                )
            .build()
        )
    );

}
