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
                .advanceDisbursementTo(Optional.of(DisbursementState.executed))
                .autoApplyExcessToInvoicesEnabled(Optional.of(false))
                .negativeInvoiceHandling(Optional.of(
                    NegativeInvoiceHandlingDetails.builder()
                        .automaticallySettleNegativeInvoices(SettleNegativeInvoicesOption.toCreditBalance)
                        .prioritizeOverlappingCoveragePeriods(true)
                        .targetInvoices(TargetInvoices.allOpenInvoices)
                        .targetInvoicePriority(TargetInvoicePriority.earliestFirst)
                        .processingMode(CreditProcessingMode.accountLevel)
                        .yieldExcessToCreditBalance(true)
                    .build()
                )
            )
            .build()
        )
    );

}
