package com.socotra.coremodel;

import lombok.Builder;

@Builder
public record ExternalNumberingPlanDetails(String name, QuoteState triggerQuoteState)
    implements ExternalNumberingPlan {}
