package com.socotra.coremodel;

public enum PaymentState {
  draft,
  validated,
  requested,
  executing,
  posted,
  failed,
  cancelled,
  reversed,
  discarded;
}
