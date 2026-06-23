package com.socotra.coremodel;

import java.util.*;
import java.util.function.Consumer;

public record EventCadence(
    java.lang.Integer intervalDuration,
    com.socotra.coremodel.DurationBasis durationBasis,
    Optional<java.lang.Integer> limit) {
  public EventCadence {

    limit = limit == null ? Optional.empty() : limit;

    Objects.requireNonNull(intervalDuration, "intervalDuration is required but it is null");
    Objects.requireNonNull(durationBasis, "durationBasis is required but it is null");
    Objects.requireNonNull(limit, "limit is required but it is null");
  }

  public static EventCadenceBuilder builder() {
    return new EventCadenceBuilder();
  }

  public EventCadenceBuilder toBuilder() {
    return new EventCadenceBuilder()
        .intervalDuration(this.intervalDuration)
        .durationBasis(this.durationBasis)
        .limit(this.limit);
  }

  public static class EventCadenceBuilder {
    private java.lang.Integer intervalDuration;

    public EventCadenceBuilder intervalDuration(java.lang.Integer intervalDuration) {
      this.intervalDuration = intervalDuration;
      return this;
    }

    private com.socotra.coremodel.DurationBasis durationBasis;

    public EventCadenceBuilder durationBasis(com.socotra.coremodel.DurationBasis durationBasis) {
      this.durationBasis = durationBasis;
      return this;
    }

    private Optional<java.lang.Integer> limit;

    public EventCadenceBuilder limit(Optional<java.lang.Integer> limit) {
      this.limit = limit;
      return this;
    }

    public EventCadenceBuilder limit(java.lang.Integer limit) {
      this.limit = Optional.ofNullable(limit);
      return this;
    }

    public EventCadenceBuilder() {}

    public EventCadence build() {
      return new EventCadence(intervalDuration, durationBasis, limit);
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
  }
}
