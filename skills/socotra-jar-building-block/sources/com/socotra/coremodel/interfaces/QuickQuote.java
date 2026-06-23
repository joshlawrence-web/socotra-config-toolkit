package com.socotra.coremodel.interfaces;

import com.socotra.coremodel.*;
import com.socotra.coremodel.ValidationResult;
import com.socotra.deployment.DeploymentFactory;
import com.socotra.platform.tools.ULID;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

public interface QuickQuote extends QuoteCore {

  default ULID groupLocator() {
    return locator();
  }

  default Optional<BigDecimal> duration() {
    return Optional.empty();
  }

  Optional<ULID> accountLocator();

  default Optional<ValidationResult> validationResult() {
    return Optional.empty();
  }

  default QuickQuoteState quickQuoteState() {
    return state();
  }

  default Optional<Instant> anonymizedAt() {
    return Optional.empty();
  }

  // TODO: remove after Sept 2024
  @Deprecated
  default QuickQuoteState state() {
    return quickQuoteState();
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
      Method m = clazz.getMethod("from", DeploymentFactory.class, QuickQuote.class);
      return (T) m.invoke(null, factory, this);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
