package com.socotra.coremodel;

import lombok.Builder;

@Builder(toBuilder = true)
public record MoratoriumStatus(
    boolean applicable,
    boolean eligible,
    boolean inScope,
    MoratoriumApplicationMode applicationMode) {
  public MoratoriumStatus {
    inScope = applicable & eligible;
  }
}
