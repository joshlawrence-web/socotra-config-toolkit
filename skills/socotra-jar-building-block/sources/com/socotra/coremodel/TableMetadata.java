package com.socotra.coremodel;

import com.socotra.coremodel.interfaces.SelectableResource;

public interface TableMetadata extends SelectableResource {
  byte[] makeKey();

  default int configHashCode() {
    return 0;
  }
}
