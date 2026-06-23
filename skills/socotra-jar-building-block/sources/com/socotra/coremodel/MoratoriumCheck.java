package com.socotra.coremodel;

import com.socotra.deployment.DeploymentConfig;
import java.util.Map;

/** Objects implementing this interface can be a subject for moratoriums (limiting changes) */
public interface MoratoriumCheck {
  /**
   * Checks if the object is subject to any moratoriums.
   *
   * @return a map of {@link MoratoriumConfig} that apply to this object, or an empty collection if
   *     no moratoriums apply.
   */
  default Map<String, MoratoriumConfig> checkForMoratoriums(DeploymentConfig deploymentConfig) {
    return Map.of();
  }
}
