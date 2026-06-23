package com.socotra.coremodel;

import com.socotra.deployment.*;
import java.time.*;
import java.util.*;
import java.util.function.Consumer;

public record EventSchedule(
    EventAnchor anchor,
    Optional<EventTimeAlignment> alignment,
    Map<DurationBasis, Integer> offset,
    Optional<EventCadence> cadence,
    Collection<PolicyStatus> suppressOnStatuses) {
  public EventSchedule {

    alignment = alignment == null ? Optional.empty() : alignment;

    cadence = cadence == null ? Optional.empty() : cadence;
    offset = offset == null ? Map.of() : new TreeMap<>(offset);
    suppressOnStatuses = suppressOnStatuses == null ? List.of() : List.copyOf(suppressOnStatuses);

    Objects.requireNonNull(anchor, "anchor is required but it is null");
    Objects.requireNonNull(alignment, "alignment is required but it is null");
    Objects.requireNonNull(cadence, "cadence is required but it is null");
  }

  public static EventScheduleBuilder builder() {
    return new EventScheduleBuilder();
  }

  public EventScheduleBuilder toBuilder() {
    return new EventScheduleBuilder()
        .anchor(this.anchor)
        .alignment(this.alignment)
        .offsets(this.offset)
        .cadence(this.cadence)
        .suppressOnStatuses(this.suppressOnStatuses);
  }

  public static class EventScheduleBuilder {
    private EventAnchor anchor;

    public EventScheduleBuilder anchor(EventAnchor anchor) {
      this.anchor = anchor;
      return this;
    }

    private Optional<EventTimeAlignment> alignment;

    public EventScheduleBuilder alignment(Optional<EventTimeAlignment> alignment) {
      this.alignment = alignment;
      return this;
    }

    public EventScheduleBuilder alignment(EventTimeAlignment alignment) {
      this.alignment = Optional.ofNullable(alignment);
      return this;
    }

    private Map<DurationBasis, Integer> offsets;

    public EventScheduleBuilder offsets(Map<DurationBasis, Integer> offsets) {
      this.offsets = offsets;
      return this;
    }

    public EventScheduleBuilder addOffset(DurationBasis durationBasis, int duration) {
      if (this.offsets == null) {
        this.offsets = new TreeMap<>();
      }
      this.offsets.put(durationBasis, duration);
      return this;
    }

    private Optional<EventCadence> cadence;

    public EventScheduleBuilder cadence(Optional<EventCadence> cadence) {
      this.cadence = cadence;
      return this;
    }

    public EventScheduleBuilder cadence(EventCadence cadence) {
      this.cadence = Optional.ofNullable(cadence);
      return this;
    }

    private Collection<PolicyStatus> suppressOnStatuses;

    public EventScheduleBuilder suppressOnStatuses(Collection<PolicyStatus> suppressOnStatuses) {
      this.suppressOnStatuses = suppressOnStatuses;
      return this;
    }

    public EventScheduleBuilder() {
      offsets = new TreeMap<>();
      suppressOnStatuses = new ArrayList<>();
    }

    public EventSchedule build() {
      return new EventSchedule(anchor, alignment, offsets, cadence, suppressOnStatuses);
    }

    private <T> Collection<T> copyIfImmutable(Collection<T> c, Consumer<Collection<T>> consumer) {
      if (c == null) {
        c = new ArrayList<>();
      }
      try {
        consumer.accept(c);
      } catch (UnsupportedOperationException e) {
        c = new ArrayList<>(c);
        consumer.accept(c);
      }
      return c;
    }

    public EventScheduleBuilder addSuppressOnStatus(PolicyStatus field) {
      this.suppressOnStatuses = copyIfImmutable(this.suppressOnStatuses, c -> c.add(field));
      return this;
    }

    public EventScheduleBuilder addSuppressOnStatuses(Collection<PolicyStatus> fields) {
      this.suppressOnStatuses = copyIfImmutable(this.suppressOnStatuses, c -> c.addAll(fields));
      return this;
    }

    public boolean hasSuppressOnStatuses() {
      return !this.suppressOnStatuses.isEmpty();
    }
  }
}
