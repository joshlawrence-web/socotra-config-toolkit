package com.socotra.coremodel;

public enum UnderwritingLevel {
  none("none"),
  info("none"),
  block("blocked"),
  decline("declined"),
  reject("rejected"),
  approve("approved");

  public String getStatusName() {
    return statusName;
  }

  private final String statusName;

  UnderwritingLevel(String statusName) {
    this.statusName = statusName;
  }
}
