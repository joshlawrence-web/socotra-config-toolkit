package com.socotra.coremodel;

import java.math.BigDecimal;
import java.util.function.Consumer;

public interface RangeTableMetadata extends TableMetadata {
  BigDecimal rangeStart();

  BigDecimal rangeEnd();

  String rangeStartName();

  String rangeEndName();

  default void validateRange(Consumer<String> errorConsumer) {
    if (rangeStartName().equals(rangeEndName())) {
      if (rangeStart() == null) {
        errorConsumer.accept("Invalid range: " + rangeStartName() + " cannot be null");
      }
      return;
    }
    if (rangeStart() == null || rangeEnd() == null) {
      errorConsumer.accept(
          "Invalid range: range bounds cannot be null: "
              + rangeStartName()
              + ": "
              + rangeStart()
              + ", "
              + rangeEndName()
              + ": "
              + rangeEnd());
      return;
    }
    if (rangeStart().compareTo(rangeEnd()) > 0) {
      errorConsumer.accept(
          "Invalid range: start "
              + rangeStartName()
              + ": "
              + rangeStart()
              + " is greater than end "
              + rangeEndName()
              + ": "
              + rangeEnd());
    }
  }
}
