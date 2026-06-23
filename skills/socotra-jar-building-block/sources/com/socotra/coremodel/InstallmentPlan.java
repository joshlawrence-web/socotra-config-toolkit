package com.socotra.coremodel;

import java.math.BigDecimal;
import java.util.Collection;

public interface InstallmentPlan {
  InstallmentCadence cadence();

  InstallmentAnchorMode anchorMode();

  Integer generateLeadDays();

  Integer dueLeadDays();

  Collection<BigDecimal> installmentWeights();

  Integer maxInstallmentsPerTerm();

  default BigDecimal autopayLeadDays() {
    return BigDecimal.ONE;
  }
}
