package com.socotra.coremodel;

public enum DocumentFormat {
  text("text/plain"),
  html("text/html"),
  pdf("application/pdf"),
  jpg("image/jpeg"),
  jpeg("image/jpeg"),
  doc("application/msword"),
  docx("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
  xls("application/vnd.ms-excel"),
  xlsx("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
  csv("text/csv"),
  txt("text/plain"),
  zip("application/zip"),
  eml("message/rfc822"), // according to https://mimetype.io/message/rfc822
  msg("application/vnd.ms-outlook"); // according to https://www.whatisfileextension.com/msg/

  private final String mimeType;

  DocumentFormat(String mimeType) {
    this.mimeType = mimeType;
  }

  public String getMimeType() {
    return mimeType;
  }
}
