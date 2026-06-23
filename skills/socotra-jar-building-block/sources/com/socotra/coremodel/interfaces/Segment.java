package com.socotra.coremodel.interfaces;

import com.socotra.coremodel.*;
import com.socotra.coremodel.Element;
import com.socotra.deployment.DeploymentFactory;
import com.socotra.platform.tools.ULID;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

public interface Segment {
  ULID locator();

  ULID transactionLocator();

  SegmentType segmentType();

  Instant startTime();

  Instant endTime();

  Element element();

  BigDecimal duration();

  Optional<ULID> basedOn();

  default Optional<ULID> configVersionLocator() {
    if (this instanceof CustomerObject co) {
      return Optional.ofNullable(co.configLocator());
    }
    return Optional.empty();
  }

  default CustomerObject maskData(DataMaskingLevel level) {
    throw new UnsupportedOperationException("maskData is not supported for Segment");
  }

  default CustomerObject anonymizeData() {
    throw new UnsupportedOperationException("anonymizeData is not supported for Segment");
  }

  default Optional<Instant> anonymizedAt() {
    return Optional.empty();
  }

  default Optional<ProducerInfo> producerInfo() {
    return Optional.empty();
  }

  /**
   * Deserialize Segment into Customer's defined object
   *
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
      Method m = clazz.getMethod("from", DeploymentFactory.class, Segment.class);
      return (T) m.invoke(null, factory, this);
    } catch (Exception var4) {
      throw new RuntimeException(var4);
    }
  }
}
