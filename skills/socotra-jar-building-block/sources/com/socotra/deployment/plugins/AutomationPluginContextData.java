package com.socotra.deployment.plugins;

import com.socotra.coremodel.EventData;
import java.util.Map;
import java.util.Optional;
import lombok.Builder;

@Builder(toBuilder = true)
public record AutomationPluginContextData(
    Optional<String> accessToken,
    Optional<Map<String, Object>> secret,
    Optional<String> apiUrl,
    // extraData such as eventPayload
    Optional<EventData> eventData) {
  public AutomationPluginContextData {
    accessToken = accessToken == null ? Optional.empty() : accessToken;
    secret = secret == null ? Optional.empty() : secret;
    apiUrl = apiUrl == null ? Optional.empty() : apiUrl;
    eventData = eventData == null ? Optional.empty() : eventData;
  }
}
