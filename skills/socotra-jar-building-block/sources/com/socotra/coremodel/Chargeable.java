package com.socotra.coremodel;

import com.socotra.platform.tools.ULID;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface Chargeable {
  BigDecimal MAX_RATE = new BigDecimal("999999999.9999999999");
  BigDecimal MAX_AMOUNT = new BigDecimal("9999999999999999.999");

  ULID locator();

  Collection<? extends ChargeType> charges();

  void consumeChargeableElements(Consumer<Chargeable> consumer);

  default ValidationResult validateRatingSet(RatingSet ratingSet) {
    ValidationResult.ValidationResultBuilder builder = ValidationResult.builder();
    validateRatingSet(
        ratingSet.ratingItems().stream()
            .collect(
                Collectors.groupingBy(
                    RatingItem::elementLocator,
                    Collectors.mapping(Function.identity(), Collectors.toList()))),
        builder::addValidationItem);
    Set<ULID> validUlids = new HashSet<>();
    Stack<Chargeable> toProcess = new Stack<>();
    toProcess.push(this);
    while (!toProcess.isEmpty()) {
      Chargeable chargeable = toProcess.pop();
      validUlids.add(chargeable.locator());
      chargeable.consumeChargeableElements(toProcess::push);
    }
    ratingSet.ratingItems().stream()
        .filter(ri -> !validUlids.contains(ri.elementLocator()))
        .map(
            ri ->
                ValidationItem.builder()
                    .locator(ri.elementLocator())
                    .addError(
                        "unexpected '"
                            + ri.chargeType().name()
                            + "' charge for element ["
                            + ri.elementLocator()
                            + "]")
                    .build())
        .forEach(builder::addValidationItem);
    return builder.build();
  }

  default void validateRatingSet(
      Map<ULID, List<RatingItem>> ratingMap, Consumer<ValidationItem> errorConsumer) {
    validateRatingItems(ratingMap.get(locator()), errorConsumer);
    consumeChargeableElements(e -> e.validateRatingSet(ratingMap, errorConsumer));
  }

  default void validateRatingItems(
      Collection<RatingItem> ratingItems, Consumer<ValidationItem> errorConsumer) {
    if (ratingItems == null) {
      ratingItems = List.of();
    }
    Set<String> expectedCharges =
        charges().stream().map(ChargeType::name).collect(Collectors.toSet());
    Set<String> actualCharges =
        ratingItems.stream()
            .filter(ri -> ri.elementLocator().equals(locator()))
            .map(RatingItem::chargeType)
            .map(ChargeType::name)
            .collect(Collectors.toSet());
    Collection<String> unexpected =
        actualCharges.stream().filter(s -> !expectedCharges.contains(s)).toList();
    if (!unexpected.isEmpty()) {
      errorConsumer.accept(
          ValidationItem.builder()
              .locator(locator())
              .addError(
                  "unexpected charges for "
                      + this.getClass().getSimpleName()
                      + "["
                      + locator()
                      + "]: ["
                      + String.join(", ", unexpected)
                      + "]")
              .build());
    }
    ratingItems.stream()
        .filter(
            ri ->
                ri.rate().isPresent() || ri.referenceRate().isPresent() || ri.amount().isPresent())
        .forEach(
            ri -> {
              if (ri.rate().isPresent() && MAX_RATE.compareTo(ri.rate().get()) < 0) {
                errorConsumer.accept(
                    ValidationItem.builder()
                        .locator(locator())
                        .addError(
                            "rate for charge '"
                                + ri.chargeType().name()
                                + "' exceeds maximum allowed value of "
                                + MAX_RATE)
                        .build());
              }
              if (ri.referenceRate().isPresent()
                  && MAX_RATE.compareTo(ri.referenceRate().get()) < 0) {
                errorConsumer.accept(
                    ValidationItem.builder()
                        .locator(locator())
                        .addError(
                            "referenceRate for charge '"
                                + ri.chargeType().name()
                                + "' exceeds maximum allowed value of "
                                + MAX_RATE)
                        .build());
              }
              if (ri.amount().isPresent() && MAX_AMOUNT.compareTo(ri.amount().get()) < 0) {
                errorConsumer.accept(
                    ValidationItem.builder()
                        .locator(locator())
                        .addError(
                            "amount for charge '"
                                + ri.chargeType().name()
                                + "' exceeds maximum allowed value of "
                                + MAX_AMOUNT)
                        .build());
              }
            });
  }
}
