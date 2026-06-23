package com.socotra.deployment.customer;

import com.socotra.coremodel.TransactionCategory;

public enum TransactionType implements com.socotra.coremodel.TransactionType, com.socotra.coremodel.CustomerObject {
    issuance(TransactionCategory.issuance, true),
    change(TransactionCategory.change, true),
    renewal(TransactionCategory.renewal, true),
    cancellation(TransactionCategory.cancellation, true),
    reinstatement(TransactionCategory.reinstatement, true),
    reversal(TransactionCategory.reversal, true),
    aggregate(TransactionCategory.aggregate, true);

    public static final String TYPE = "TransactionType";
    private final TransactionCategory category;
    private final boolean costBearing;
    TransactionType(TransactionCategory category, boolean costBearing) {
        this.category = category;
        this.costBearing = costBearing;
    }

    @Override
    public TransactionCategory category() {
        return category;
    }

    @Override
    public boolean costBearing() {
        return costBearing;
    }

    @Override
    public String type() {
        return TYPE;
    }
}