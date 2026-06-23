package com.socotra.coremodel;

public enum TransactionState {
  draft,
  initialized,
  validated,
  earlyUnderwritten,
  priced,
  underwritten,
  accepted,
  issued,
  underwrittenBlocked,
  declined,
  rejected,
  refused,
  discarded,
  invalidated,
  reversed
}
