package com.socotra.coremodel;

public interface TransactionChangeInstruction {
  default TransactionChangeInstruction metadata() {
    return this;
  }
}
