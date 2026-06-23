package com.socotra.coremodel;

import com.socotra.deployment.TimeService;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

public record TenantEventSchedule(
    Optional<EventTimeAlignment> alignment,
    Map<DurationBasis, Integer> offset,
    Optional<EventCadence> cadence) {
  public TenantEventSchedule {

    alignment = alignment == null ? Optional.empty() : alignment;

    cadence = cadence == null ? Optional.empty() : cadence;
    offset = offset == null ? Map.of() : new TreeMap<>(offset);
    Objects.requireNonNull(alignment, "alignment is required but it is null");
    Objects.requireNonNull(cadence, "cadence is required but it is null");
  }

  public static TenantEventScheduleBuilder builder() {
    return new TenantEventScheduleBuilder();
  }

  public TenantEventScheduleBuilder toBuilder() {
    return new TenantEventScheduleBuilder()
        .alignment(this.alignment)
        .offset(this.offset)
        .cadence(this.cadence);
  }

  public static class TenantEventScheduleBuilder {

    private Optional<EventTimeAlignment> alignment;

    public TenantEventScheduleBuilder alignment(Optional<EventTimeAlignment> alignment) {
      this.alignment = alignment;
      return this;
    }

    public TenantEventScheduleBuilder alignment(EventTimeAlignment alignment) {
      this.alignment = Optional.ofNullable(alignment);
      return this;
    }

    private Map<DurationBasis, Integer> offsets;

    public TenantEventScheduleBuilder offset(Map<DurationBasis, Integer> offsets) {
      this.offsets = offsets;
      return this;
    }

    public TenantEventScheduleBuilder addOffset(DurationBasis durationBasis, int duration) {
      if (this.offsets == null) {
        this.offsets = new TreeMap<>();
      }
      this.offsets.put(durationBasis, duration);
      return this;
    }

    private Optional<EventCadence> cadence;

    public TenantEventScheduleBuilder cadence(Optional<EventCadence> cadence) {
      this.cadence = cadence;
      return this;
    }

    public TenantEventScheduleBuilder cadence(EventCadence cadence) {
      this.cadence = Optional.ofNullable(cadence);
      return this;
    }

    public TenantEventScheduleBuilder() {
      offsets = new TreeMap<>();
    }

    public TenantEventSchedule build() {
      return new TenantEventSchedule(alignment, offsets, cadence);
    }
  }

  public static TenantEventSchedule from(EventSchedule schedule) {
    return new TenantEventSchedule(schedule.alignment(), schedule.offset(), schedule.cadence());
  }

  public Instant getEventTime(Instant anchorTime, TimeService timeService) {
    Instant eventTime =
        this.alignment().map(a -> a.align(anchorTime, timeService)).orElse(anchorTime);
    for (Map.Entry<DurationBasis, Integer> offset : offset.entrySet()) {
      eventTime =
          timeService.addDuration(eventTime, offset.getValue().doubleValue(), offset.getKey());
    }
    return eventTime;
  }
}
