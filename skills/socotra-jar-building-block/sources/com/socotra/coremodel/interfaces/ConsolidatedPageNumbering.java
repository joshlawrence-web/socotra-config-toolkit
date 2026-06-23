package com.socotra.coremodel.interfaces;

public interface ConsolidatedPageNumbering {
  default Boolean enableNumbering() {
    return false;
  }

  default Boolean leadingDocumentPages() {
    return false;
  }

  default Integer xPosition() {
    return 0;
  }

  default Integer yPosition() {
    return 0;
  }
}
