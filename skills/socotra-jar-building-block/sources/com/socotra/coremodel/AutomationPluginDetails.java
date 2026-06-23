package com.socotra.coremodel;

import java.util.Map;
import lombok.Builder;

@Builder
public record AutomationPluginDetails(
    String pluginName,
    Map<String, AutomationActionDetails> actions,
    boolean enableWebhooks,
    String secret) {

  public AutomationPluginDetails {
    actions = actions == null ? Map.of() : actions;
  }
}
