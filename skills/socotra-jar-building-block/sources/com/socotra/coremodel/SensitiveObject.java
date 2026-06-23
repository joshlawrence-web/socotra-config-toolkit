package com.socotra.coremodel;

/** SensitiveObject is an interface that represents an object that may contain sensitive data. */
public interface SensitiveObject {
  default SensitiveObject maskData(DataMaskingLevel level) {
    return this;
  }

  default SensitiveObject anonymizeData() {
    return this;
  }
}
