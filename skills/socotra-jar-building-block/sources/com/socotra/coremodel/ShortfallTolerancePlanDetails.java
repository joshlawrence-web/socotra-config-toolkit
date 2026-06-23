package com.socotra.coremodel;

import com.socotra.deployment.MoneyService;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import lombok.Builder;

@Builder
public record ShortfallTolerancePlanDetails(Map<String, BigDecimal> currencyTolerances)
    implements ShortfallTolerancePlan {

  @Override
  public Optional<BigDecimal> getThresholdForCurrency(String currency) {
    String currencyCode = currency.toUpperCase();
    return Optional.ofNullable(currencyTolerances.get(currencyCode))
        .map(
            amount -> {
              MoneyService moneyService = new MoneyService(currencyCode);
              return moneyService.toMoney(amount);
            });
  }
}
