package com.socotra.coremodel;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;

@Builder
public record InstallmentPlanDetails(
    InstallmentCadence cadence,
    InstallmentAnchorMode anchorMode,
    Integer generateLeadDays,
    Integer dueLeadDays,
    List<BigDecimal> installmentWeights,
    Integer maxInstallmentsPerTerm,
    BigDecimal autopayLeadDays)
    implements InstallmentPlan {}
