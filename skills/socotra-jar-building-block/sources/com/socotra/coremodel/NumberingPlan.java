package com.socotra.coremodel;

import java.util.Collection;
import java.util.Set;

public interface NumberingPlan {
  String name();

  String initialCoreNumber();

  String format();

  boolean copyFromQuote();

  String termNumberFormat();

  String coreNumberFormat();

  Collection<String> placeholders();

  default String quoteNumberFormat() {
    return "";
  }

  default String initialQuoteCoreNumber() {
    return "";
  }

  default String quoteCoreNumberFormat() {
    return "";
  }

  default Collection<String> quotePlaceholders() {
    return Set.of();
  }

  // missing productScopes in config means both quotes and policies are handled
  // Note that this is just for checking application at the quote / policy level
  // The above implies that an account , payment, fnol etc. can still use them
  default Collection<NumberingProductScope> productScopes() {
    return Set.of(NumberingProductScope.quote, NumberingProductScope.policy);
  }
}
