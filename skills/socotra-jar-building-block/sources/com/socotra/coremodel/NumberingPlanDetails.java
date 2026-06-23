package com.socotra.coremodel;

import java.util.Collection;
import lombok.Builder;

@Builder
public record NumberingPlanDetails(
    String name,
    String initialCoreNumber,
    String format,
    boolean copyFromQuote,
    String termNumberFormat,
    String coreNumberFormat,
    Collection<String> placeholders,
    String quoteNumberFormat,
    String initialQuoteCoreNumber,
    String quoteCoreNumberFormat,
    Collection<String> quotePlaceholders,
    Collection<NumberingProductScope> productScopes)
    implements NumberingPlan {}
