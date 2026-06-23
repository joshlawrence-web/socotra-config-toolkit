package com.socotra.coremodel;

import java.util.*;

public interface ExternalNumberingPlan {

  Set<QuoteState> ALLOWED_QUOTE_STATES =
      Collections.unmodifiableSet(
          new TreeSet<>(
              List.of(
                  QuoteState.validated,
                  QuoteState.earlyUnderwritten,
                  QuoteState.priced,
                  QuoteState.underwritten,
                  QuoteState.accepted,
                  QuoteState.issued)));

  String name();

  QuoteState triggerQuoteState();
}
