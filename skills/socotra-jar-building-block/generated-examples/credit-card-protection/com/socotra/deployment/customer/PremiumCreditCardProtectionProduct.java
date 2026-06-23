package com.socotra.deployment.customer;

import com.socotra.coremodel.*;
import java.util.*;

public record PremiumCreditCardProtectionProduct(List<String> eligibleAccountTypes,
    int defaultTermDuration,
    String defaultInstallmentPlanName,
    String defaultBillingPlanName,
    String defaultDelinquencyPlanName,
    String defaultAutoRenewalPlanName,
    DurationBasis defaultDurationBasis,
    Set<String> documents,
    String defaultShortfallTolerancePlanName,
    BillingTrigger defaultBillingTrigger,
    String numberingPlanName,
    String numberingString) implements Product {

  public static final String TYPE = "PremiumCreditCardProtection";

  public PremiumCreditCardProtectionProduct() {
    this(List.of("BankCustomerAccount"),
         12,
         "Standard",
         "",
         "Standard",
         "Standard",
         DurationBasis.months,
         Set.of(),
         "",
         BillingTrigger.issue,
         "",
         ""
    );
  }

  @Override
  public String type() {
    return TYPE;
  }

}