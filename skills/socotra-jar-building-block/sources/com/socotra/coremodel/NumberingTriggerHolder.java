package com.socotra.coremodel;

public interface NumberingTriggerHolder {
  default NumberingTrigger numberingTrigger() {
    return NumberingTrigger.validation;
  }
}
