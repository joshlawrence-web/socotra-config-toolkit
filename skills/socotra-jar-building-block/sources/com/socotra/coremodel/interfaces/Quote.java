package com.socotra.coremodel.interfaces;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.socotra.coremodel.*;
import com.socotra.coremodel.Element;
import com.socotra.coremodel.ValidationResult;
import com.socotra.coremodel.views.Internal;
import com.socotra.deployment.DeploymentConfig;
import com.socotra.deployment.DeploymentFactory;
import com.socotra.deployment.TimeService;
import com.socotra.platform.tools.ULID;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface Quote extends QuoteCore {
  Logger log = LoggerFactory.getLogger(Quote.class);

  QuoteState quoteState();

  ULID accountLocator();

  Optional<String> underwritingStatus();

  @JsonView(Internal.class)
  default Optional<Boolean> checkHold() {
    return Optional.empty();
  }

  default Optional<ULID> policyLocator() {
    return Optional.empty();
  }

  default ULID groupLocator() {
    return null;
  }

  // Backward compatibility. Remove after 01/01/2025
  @JsonIgnore
  default ULID quoteGroupLocator() {
    return groupLocator();
  }

  @JsonView(Internal.class)
  default Optional<byte[]> securityId() {
    return Optional.empty();
  }

  default Optional<String> region() {
    return Optional.empty();
  }

  default Optional<String> quoteNumber() {
    return Optional.empty();
  }

  default Optional<BigDecimal> duration() {
    return Optional.empty();
  }

  default Optional<Instant> acceptedTime() {
    return Optional.empty();
  }

  default Optional<Instant> issuedTime() {
    return Optional.empty();
  }

  default Optional<ULID> quickQuoteLocator() {
    return Optional.empty();
  }

  default Optional<BigDecimal> invoiceFeeAmount() {
    return Optional.empty();
  }

  default Optional<ValidationResult> validationResult() {
    return Optional.empty();
  }

  default Optional<Instant> anonymizedAt() {
    return Optional.empty();
  }

  default Map<String, String> moratoriums() {
    return Map.of();
  }

  default Optional<String> reservedPolicyNumber() {
    return Optional.empty();
  }

  /**
   * Return moratoriums for this quote that are applicable and not opted out.
   *
   * @param factory
   * @return
   */
  default Map<String, MoratoriumConfig> checkForMoratoriums(
      DeploymentFactory factory, Moratoriums moratoriums) {
    DeploymentConfig config = factory.getDeploymentConfig();
    if (config.getMoratoriums().isEmpty() && moratoriums == null) {
      return Map.of(); // Nothing to check
    }
    Map<String, MoratoriumConfig> result = new HashMap<>();
    if (moratoriums != null) {
      result.putAll(moratoriums.checkForMoratoriums((com.socotra.coremodel.Quote) this));
    } else {
      // Legacy moratoriums. We should delete this at some point after deprecation
      CustomerObject customerObject = toCustomerObject(factory);
      if (customerObject instanceof MoratoriumCheck ms) {
        result.putAll(ms.checkForMoratoriums(config));
      }
    }
    return result.entrySet().stream()
        .filter(
            e -> {
              MoratoriumConfig mc = e.getValue();
              if (this.startTime().isPresent() && this.endTime().isPresent()) {
                if (this.startTime().get().isBefore(mc.effectiveTime())
                    && this.endTime().get().isBefore(mc.effectiveTime())) {
                  return false;
                }
              }
              return true;
            })
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  /**
   * @param factory
   * @return
   */
  @SuppressWarnings("unchecked")
  default <T extends CustomerObject> T toCustomerObject(DeploymentFactory factory) {
    Class<?> clazz =
        factory
            .getDeploymentConfig()
            .getObjectClass(this.element().type())
            .orElseThrow(() -> new IllegalArgumentException(this.element().type() + " is unknown"));

    try {
      Method m = clazz.getMethod("from", DeploymentFactory.class, Quote.class);
      return (T) m.invoke(null, factory, this);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  static com.socotra.coremodel.Quote fromCustomerObject(
      DeploymentFactory factory, CustomerObject object, DurationBasis durationBasis) {
    return fromCustomerObject(factory, object, durationBasis, true);
  }

  static com.socotra.coremodel.Quote fromCustomerObject(
      DeploymentFactory factory,
      CustomerObject object,
      DurationBasis durationBasis,
      boolean calculateDuration) {
    if (object instanceof com.socotra.coremodel.interfaces.Quote q) {
      DeploymentConfig config = factory.getDeploymentConfig();
      Product product = config.getProductRequired(q.productName());
      durationBasis =
          durationBasis == null || durationBasis == DurationBasis.none
              ? product.defaultDurationBasis()
              : durationBasis;
      if (object instanceof Elemental el) {
        Element element = el.toElement(factory);
        com.socotra.coremodel.Quote.QuoteBuilder builder =
            com.socotra.coremodel.Quote.copyFrom(q).toBuilder()
                .locator(element.locator())
                .configVersionLocator(object.configLocator())
                .productName(product.type())
                .durationBasis(durationBasis)
                .element(element);
        if (calculateDuration) {
          String timezone = q.timezone().orElse(config.getDefaultTimezone());
          log.info(
              "TimeService will use timezone={} and durationBasis={}", timezone, durationBasis);
          TimeService timeService = new TimeService(timezone, durationBasis);
          // start / end should be filled out at this point
          Instant startTime = q.startTime().orElseThrow();
          builder.duration(
              Optional.of(
                  timeService.calculateDuration(startTime, q.endTime().orElseThrow(), startTime)));
        }
        return builder.build();
      }
    }

    throw new IllegalArgumentException(
        object.type() + " does not implement Elemental or Quote interface");
  }
}
