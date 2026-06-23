package com.socotra.coremodel;

import com.socotra.platform.tools.ULID;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.Builder;
import lombok.NonNull;

@Builder(toBuilder = true)
public record EventData(
    @NonNull ULID eventLocator,
    @NonNull Instant timestamp,
    String eventId,
    Optional<UUID> userLocator,
    Map<String, Object> data) {
  public EventData {
    if (eventId == null || eventId.isBlank()) {
      throw new IllegalArgumentException("eventId cannot be null or blank");
    }
    userLocator = userLocator == null ? Optional.empty() : userLocator;
    data = data == null ? Map.of() : data;
  }
}
