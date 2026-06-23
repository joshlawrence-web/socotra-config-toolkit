package com.socotra.deployment.customer;

import com.socotra.coremodel.ReversalCreditType;

public enum ReversalType implements com.socotra.coremodel.ReversalType, com.socotra.coremodel.CustomerObject {
    standard(ReversalCreditType.any);

    public static final String TYPE = "ReversalType";
    private final ReversalCreditType creditType;
    ReversalType(ReversalCreditType creditType) {
        this.creditType = creditType;
    }

    public ReversalCreditType creditType() {
        return creditType;
    }

    @Override
    public String type() {
        return TYPE;
    }
}