package com.socotra.coremodel;

import java.math.BigDecimal;
import java.util.Optional;

public interface ShortfallTolerancePlan {
  Optional<BigDecimal> getThresholdForCurrency(String currency);
}
