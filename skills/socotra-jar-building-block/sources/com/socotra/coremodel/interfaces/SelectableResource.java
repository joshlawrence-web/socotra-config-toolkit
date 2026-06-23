package com.socotra.coremodel.interfaces;

import com.socotra.coremodel.SelectionTimeBasis;

public interface SelectableResource {
  String staticName();

  SelectionTimeBasis selectionTimeBasis();
}
