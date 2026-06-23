package com.socotra.coremodel;

public enum DocumentTrigger {
  /*
   * Order is important as triggers are compared
   * by ordinal to find out latest happened
   * */
  validated,
  priced,
  accepted,
  underwritten,
  issued,
  generated;

  public boolean isBefore(DocumentTrigger other) {
    return this.ordinal() <= other.ordinal();
  }
}
