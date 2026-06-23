package com.socotra.deployment.customer;

import com.socotra.coremodel.*;
import com.socotra.deployment.DeploymentConfig;
import java.util.*;
import java.util.function.Consumer;

public record ZenCoverProduct(List<String> eligibleAccountTypes,
    int defaultTermDuration,
    String defaultInstallmentPlanName,
    String defaultBillingPlanName,
    String defaultDelinquencyPlanName,
    String defaultAutoRenewalPlanName,
    DurationBasis defaultDurationBasis,
    Set<String> documents,
    String defaultShortfallTolerancePlanName,
    String numberingPlanName,
    String numberingString,
    Map<String, Collection<String>> workplanTriggers,
    Set<String> eligibleTransactionTypes,
    String externalNumberingPlanName,
    boolean reservedPolicyNumberRequired) implements Product {

  public static final String TYPE = "ZenCover";

  public ZenCoverProduct() {
    this(List.of("PersonalAccount"),
         12,
         "Monthly12",
         "",
         "Standard",
         "Standard_60_30_1",
         DurationBasis.months,
         Set.of("Forms01"),
         "basicPlan",
         "ZCPolicyNumberingPlan",
         "ZC",
         Map.of(),
         Set.of(),
         "",
         false
    );
  }

  @Override
  public String type() {
    return TYPE;
  }


  @Override
  public void validateJurisdiction(DeploymentConfig config, String jurisdiction, Consumer<String> errorConsumer) {
    if(jurisdiction != null) {
      if(!config.getJurisdictions().contains(jurisdiction)) {
        errorConsumer.accept("'jurisdiction' should be one of " + config.getJurisdictions());
      }
    }
  }
}