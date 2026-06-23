package com.socotra.coremodel.interfaces;

import com.fasterxml.jackson.annotation.JsonView;
import com.socotra.coremodel.*;
import com.socotra.coremodel.views.Internal;
import com.socotra.deployment.DataFetcher;
import com.socotra.deployment.DataFetcherFactory;
import com.socotra.deployment.DeploymentConfig;
import com.socotra.deployment.DeploymentFactory;
import com.socotra.platform.tools.ULID;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface Policy {
  Logger log = LoggerFactory.getLogger(Policy.class);

  ULID locator();

  ULID accountLocator();

  ULID issuedTransactionLocator();

  Collection<ULID> branchHeadTransactionLocators();

  String productName();

  String timezone();

  String currency();

  DurationBasis durationBasis();

  Instant createdAt();

  UUID createdBy();

  Optional<String> delinquencyPlanName();

  default BillingTrigger billingTrigger() {
    return BillingTrigger.issue;
  }

  Optional<String> autoRenewalPlanName();

  Instant startTime();

  Instant endTime();

  ULID latestTermLocator();

  ULID latestSegmentLocator();

  BillingLevel billingLevel();

  Optional<String> jurisdiction();

  Optional<String> producerCode();

  Optional<String> producerCodeOfRecord();

  @JsonView(Internal.class)
  default Optional<Boolean> checkHold() {
    return Optional.empty();
  }

  @JsonView(Internal.class)
  default Optional<byte[]> securityId() {
    return Optional.empty();
  }

  default Optional<String> region() {
    return Optional.empty();
  }

  default Optional<String> policyNumber() {
    return Optional.empty();
  }

  default Collection<ContactRoles> contacts() {
    return List.of();
  }

  default Collection<PolicyStatus> statuses() {
    return List.of();
  }

  default Optional<BigDecimal> invoiceFeeAmount() {
    return Optional.empty();
  }

  default Optional<Instant> anonymizedAt() {
    return Optional.empty();
  }

  default Optional<Instant> coverageEndTime() {
    return Optional.empty();
  }

  default Map<String, String> moratoriumElections() {
    return Map.of();
  }

  default Optional<java.lang.Boolean> migrateOnRenewal() {
    return Optional.empty();
  }

  /**
   * Check for moratoriums on the policy. The method will resolve the segment starting from provided
   * transactionLocator
   *
   * @param factory
   * @param transactionLocator
   * @return
   */
  default Map<String, MoratoriumConfig> checkForMoratoriums(
      DeploymentFactory factory, Moratoriums moratoriums, ULID transactionLocator) {
    return checkForMoratoriums(factory, moratoriums, transactionLocator, false, false);
  }

  /**
   * Check for moratoriums on the policy. The method will resolve the segment starting from provided
   * transactionLocator
   *
   * @param factory
   * @param transactionLocator
   * @param ignoreElections if true, elections will not be applied to the moratoriums
   * @param ignoreMoratoriumEffectiveTime if true, moratorium.effectiveTime is ignored
   * @return a map of moratoriums that are applicable to the policy
   */
  default Map<String, MoratoriumConfig> checkForMoratoriums(
      DeploymentFactory factory,
      Moratoriums moratoriums,
      ULID transactionLocator,
      boolean ignoreElections,
      boolean ignoreMoratoriumEffectiveTime) {
    DeploymentConfig deploymentConfig = factory.getDeploymentConfig();
    if (deploymentConfig.getMoratoriums().isEmpty() && moratoriums == null) {
      return Map.of(); // Nothing to check if no moratoriums are configured
    }

    Segment segment;
    DataFetcher dataFetcher = DataFetcherFactory.get();
    while (true) {
      segment = dataFetcher.getSegmentByTransaction(transactionLocator);
      if (segment != null) {
        break;
      }
      Transaction transaction = dataFetcher.getTransaction(transactionLocator);
      if (transaction.baseTransactionLocator().isEmpty()) {
        break;
      }
      transactionLocator = transaction.baseTransactionLocator().get();
    }
    if (segment == null) {
      throw new IllegalArgumentException("Cannot locate a segment for policy " + this.locator());
    }

    Map<String, MoratoriumConfig> policyMoratoriumsConfigs;
    Map<String, MoratoriumConfig> deployedMoratoriumsConfigs =
        moratoriums != null
            ? moratoriums.getMoratoriumConfigs()
            : deploymentConfig.getMoratoriums();

    if (moratoriums != null) {
      log.info("Checking moratoriums for policy[{}:{}]", this.locator(), transactionLocator);
      policyMoratoriumsConfigs =
          moratoriums.checkForMoratoriums(
              (com.socotra.coremodel.Policy) this, (com.socotra.coremodel.Segment) segment);
    } else {
      // Legacy moratoriums. We should delete this at some point after deprecation
      log.info(
          "Checking moratoriums for policy[{}:{}] using legacy method",
          this.locator(),
          transactionLocator);
      CustomerObject customerObject = segment.toCustomerObject(factory);
      if (customerObject instanceof MoratoriumCheck ms) {
        policyMoratoriumsConfigs = ms.checkForMoratoriums(deploymentConfig);
      } else {
        policyMoratoriumsConfigs = Map.of();
      }
    }

    if (policyMoratoriumsConfigs.isEmpty()) {
      log.info("No moratoriums found for policy[{}:{}]", this.locator(), transactionLocator);
      return Map.of();
    } else {
      // List of moratoriums based on selection criteria using data-extension fields
      log.info(
          "Policy[{}:{}] is potentially eligible for moratoriums: {}",
          this.locator(),
          transactionLocator,
          policyMoratoriumsConfigs.keySet());
    }

    // Filter out moratoriums based on Policy creation time and Moratorium effective/end time
    Instant now = Instant.now();
    Map<String, MoratoriumConfig> result =
        new HashMap<>(
            policyMoratoriumsConfigs.entrySet().stream()
                .filter(
                    e -> {
                      MoratoriumConfig mc = e.getValue();
                      if (!ignoreElections
                          && mc.applicationMode().equals(MoratoriumApplicationMode.optIn)) {
                        return false; // Skip opt-in moratoriums since they should be elected
                      }
                      if (!ignoreMoratoriumEffectiveTime) {
                        if (mc.effectiveTime().isAfter(now)
                            || (mc.endTime().isPresent() && mc.endTime().get().isBefore(now))) {
                          return false;
                        }
                      }
                      return !this.createdAt().isAfter(mc.effectiveTime())
                          || mc.effectiveTimeWaived();
                    })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

    if (!ignoreElections) {
      // Apply Policy moratorium elections
      log.info(
          "Policy[{}] has moratoriums elected: {}", this.locator(), this.moratoriumElections());
      this.moratoriumElections()
          .forEach(
              (k, v) -> {
                if (MoratoriumElection.valueOf(v) == MoratoriumElection.optOut) {
                  result.remove(k);
                } else if (MoratoriumElection.valueOf(v) == MoratoriumElection.optIn) {
                  MoratoriumConfig c = deployedMoratoriumsConfigs.get(k);
                  if (c != null) {
                    result.put(k, c);
                  }
                }
              });
    }
    log.info(
        "Policy[{}:{}] is eligible for moratoriums: {}; ignoreElections={}",
        this.locator(),
        transactionLocator,
        result.keySet(),
        ignoreElections);
    return result;
  }

  /**
   * Check for moratoriums on the policy. The method will resolve the segment starting from the
   * issued transaction locator
   *
   * @param factory
   * @return
   */
  default Map<String, MoratoriumConfig> checkForMoratoriums(
      DeploymentFactory factory, Moratoriums moratoriums) {
    return checkForMoratoriums(factory, moratoriums, this.issuedTransactionLocator());
  }
}
