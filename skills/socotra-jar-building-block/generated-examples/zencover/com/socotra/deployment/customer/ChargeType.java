package com.socotra.deployment.customer;

import java.util.Collection;
import java.util.Optional;

import com.socotra.coremodel.ChargeCategory;
import com.socotra.coremodel.ChargeHandling;
import com.socotra.coremodel.ChargeInvoicing;
import com.socotra.coremodel.Validatable;
import com.socotra.coremodel.ValidationItem;
import com.socotra.deployment.DeploymentConfig;

public enum ChargeType implements com.socotra.coremodel.ChargeType, com.socotra.coremodel.CustomerObject {
    premiumExclDiscount(ChargeCategory.nonFinancial, ChargeHandling.normal, ChargeInvoicing.scheduled, Optional.of(false)),
    tax(ChargeCategory.tax, ChargeHandling.normal, ChargeInvoicing.scheduled, Optional.of(false)),
    premium(ChargeCategory.premium, ChargeHandling.normal, ChargeInvoicing.scheduled, Optional.of(false)),
    fee(ChargeCategory.fee, ChargeHandling.normal, ChargeInvoicing.scheduled, Optional.of(false)),
    invoiceFee(ChargeCategory.invoiceFee, ChargeHandling.flat, ChargeInvoicing.next, Optional.of(false)),
    settlement(ChargeCategory.premium, ChargeHandling.retention, ChargeInvoicing.next, Optional.of(false));

    public static final String TYPE = "ChargeType";
    private final ChargeCategory category;
    private final ChargeHandling handling;
    private final ChargeInvoicing invoicing;
    private final Optional<Boolean> transactionBundlingEnabled;

    ChargeType(ChargeCategory category, ChargeHandling handling, ChargeInvoicing invoicing,
                Optional<Boolean> transactionBundlingEnabled) {
        this.category = category;
        this.handling = handling;
        this.invoicing = invoicing;
        this.transactionBundlingEnabled = transactionBundlingEnabled;
    }

    public ChargeCategory category() {
        return category;
    }

    @Override
    public ChargeInvoicing invoicing() {
        return invoicing;
    }

    @Override
    public ChargeHandling handling() {
        return handling;
    }

    @Override
    public Optional<Boolean> transactionBundlingEnabled() {
        return transactionBundlingEnabled;
    }

    @Override
    public String type() {
        return TYPE;
    }
}