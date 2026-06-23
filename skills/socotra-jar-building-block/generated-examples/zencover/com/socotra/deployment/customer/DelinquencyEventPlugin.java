package com.socotra.deployment.customer;

import com.socotra.coremodel.*;
import java.util.*;

@Plugin(type = PluginType.delinquencyEvent)
public interface DelinquencyEventPlugin {
  public static final Integer VERSION = 1;
  default Map<String, DelinquencyEventUpdateRequest> triggerDelinquencyEvent(DelinquencyEventPlugin.ZenCoverRequest request) {
    return Map.of();
  }

  public static final class DelinquencyEventPluginStub implements DelinquencyEventPlugin {
  }

  public static record ZenCoverRequest(Delinquency delinquency, DelinquencyEvent delinquencyEvent) implements PluginRequest {
    public Collection<?> structures() { return List.of(delinquency(), delinquencyEvent()); }
    public static ZenCoverRequest of(Delinquency delinquency, DelinquencyEvent delinquencyEvent) {
      Objects.requireNonNull(delinquency);
      Objects.requireNonNull(delinquencyEvent);
      return new ZenCoverRequest(delinquency, delinquencyEvent);
    }
  }
}