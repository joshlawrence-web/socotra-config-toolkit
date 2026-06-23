package com.socotra.coremodel;

import java.util.Optional;

public interface ConstraintTableMetadata extends TableMetadata {
  default Optional<byte[]> columnValuesKey(String column) {
    return Optional.empty();
  }
}
