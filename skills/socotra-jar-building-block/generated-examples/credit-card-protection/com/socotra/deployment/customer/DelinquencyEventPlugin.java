package com.socotra.deployment.customer;

import com.socotra.coremodel.*;
import java.util.*;

@Plugin(type = PluginType.delinquencyEvent)
public interface DelinquencyEventPlugin {
  default Map<String, DelinquencyEventUpdateRequest> triggerDelinquencyEvent(DelinquencyEventPlugin.BasicCreditCardProtectionRequest request) {
    return Map.of();
  }
  default Map<String, DelinquencyEventUpdateRequest> triggerDelinquencyEvent(DelinquencyEventPlugin.PremiumCreditCardProtectionRequest request) {
    return Map.of();
  }

  public static final class DelinquencyEventPluginStub implements DelinquencyEventPlugin {
  }

  public static record BasicCreditCardProtectionRequest(Delinquency delinquency, DelinquencyEvent delinquencyEvent) implements PluginRequest {
    public Collection<?> structures() { return List.of(delinquency(), delinquencyEvent()); }
    public static BasicCreditCardProtectionRequest of(Delinquency delinquency, DelinquencyEvent delinquencyEvent) {
      Objects.requireNonNull(delinquency);
      Objects.requireNonNull(delinquencyEvent);
      return new BasicCreditCardProtectionRequest(delinquency, delinquencyEvent);
    }
  }
  public static record PremiumCreditCardProtectionRequest(Delinquency delinquency, DelinquencyEvent delinquencyEvent) implements PluginRequest {
    public Collection<?> structures() { return List.of(delinquency(), delinquencyEvent()); }
    public static PremiumCreditCardProtectionRequest of(Delinquency delinquency, DelinquencyEvent delinquencyEvent) {
      Objects.requireNonNull(delinquency);
      Objects.requireNonNull(delinquencyEvent);
      return new PremiumCreditCardProtectionRequest(delinquency, delinquencyEvent);
    }
  }
}
