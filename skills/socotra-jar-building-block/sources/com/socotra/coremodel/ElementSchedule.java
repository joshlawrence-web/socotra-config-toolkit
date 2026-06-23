package com.socotra.coremodel;

import com.socotra.coremodel.interfaces.ScheduleItem;

public record ElementSchedule(
    String type,
    Class<? extends ScheduleItem<?>> itemClass,
    Class<? extends CustomerObject> itemDataClass,
    boolean complexData,
    boolean resetOnRenewal,
    int maxValidationErrors) {

  // Backward compatibility constructor for existing configurations
  public ElementSchedule(
      String type,
      Class<? extends ScheduleItem<?>> itemClass,
      Class<? extends CustomerObject> itemDataClass,
      boolean complexData,
      int maxValidationErrors) {
    this(type, itemClass, itemDataClass, complexData, false, maxValidationErrors);
  }
}
