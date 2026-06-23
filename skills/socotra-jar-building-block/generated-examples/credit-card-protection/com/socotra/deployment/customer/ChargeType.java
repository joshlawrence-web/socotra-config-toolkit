package com.socotra.deployment.customer;

import java.util.Collection;
import com.socotra.coremodel.ChargeCategory;
import com.socotra.coremodel.ChargeHandling;
import com.socotra.coremodel.ChargeInvoicing;
import com.socotra.coremodel.Validatable;
import com.socotra.coremodel.ValidationItem;
import com.socotra.deployment.DeploymentConfig;

public enum ChargeType implements com.socotra.coremodel.ChargeType, com.socotra.coremodel.CustomerObject {
    commission(ChargeCategory.nonFinancial, ChargeHandling.normal, ChargeInvoicing.scheduled),
    tax(ChargeCategory.tax, ChargeHandling.normal, ChargeInvoicing.scheduled),
    premium(ChargeCategory.premium, ChargeHandling.normal, ChargeInvoicing.scheduled),
    invoiceFee(ChargeCategory.invoiceFee, ChargeHandling.flat, ChargeInvoicing.next);

    public static final String TYPE = "ChargeType";
    private final ChargeCategory category;
    private final ChargeHandling handling;
    private final ChargeInvoicing invoicing;

    ChargeType(ChargeCategory category, ChargeHandling handling, ChargeInvoicing invoicing) {
        this.category = category;
        this.handling = handling;
        this.invoicing = invoicing;
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
    public String type() {
        return TYPE;
    }
}