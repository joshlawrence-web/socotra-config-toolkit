package com.socotra.coremodel;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public record QuoteHoldScope(QuoteState quoteState) {
  public static final Collection<QuoteState> ALLOWED_HOLD_STATES =
      Set.of(QuoteState.validated, QuoteState.priced, QuoteState.underwritten, QuoteState.accepted);

  public QuoteHoldScope {
    if (!ALLOWED_HOLD_STATES.contains(quoteState)) {
      throw new IllegalArgumentException(
          String.format(
              "Quote state %s is not allowed as quote hold state, allowed only [%s]",
              quoteState,
              ALLOWED_HOLD_STATES.stream()
                  .map(QuoteState::toString)
                  .collect(Collectors.joining(", "))));
    }
  }
}
