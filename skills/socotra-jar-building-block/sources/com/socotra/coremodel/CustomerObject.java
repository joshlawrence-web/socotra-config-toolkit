package com.socotra.coremodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.socotra.deployment.DeploymentConfig;
import com.socotra.deployment.DeploymentFactory;
import com.socotra.platform.tools.ULID;
import java.lang.reflect.Method;
import java.time.Instant;

/** Customer's defined data structure */
public interface CustomerObject {
  @JsonIgnore
  String type();

  default ULID configLocator() {
    try {
      Class<?> customerDeploymentClass =
          this.getClass().getClassLoader().loadClass(DeploymentFactory.DEPLOYMENT_FACTORY_CLASS);
      Method method = customerDeploymentClass.getMethod("staticVersionLocator");
      return (ULID) method.invoke(null);
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Apply default settings defined in the configuration
   *
   * @return modified customer object
   */
  @SuppressWarnings("unchecked")
  default <T extends CustomerObject> T applyDefaults(DeploymentConfig config) {
    return (T) this;
  }

  /**
   * Apply availability settings defined in the configuration
   *
   * @return modified customer object
   */
  @SuppressWarnings("unchecked")
  default <T extends CustomerObject> T applyAvailabilityRemovals(Instant referenceDate) {
    return (T) this;
  }
}
