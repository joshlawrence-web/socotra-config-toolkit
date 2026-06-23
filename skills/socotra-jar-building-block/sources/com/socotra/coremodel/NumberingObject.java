package com.socotra.coremodel;

import java.util.Optional;

public interface NumberingObject extends NumberingTriggerHolder {
  default Optional<String> numberingPlanName() {
    return Optional.empty();
  }
}
