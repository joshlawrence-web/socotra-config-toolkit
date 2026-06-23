package com.socotra.deployment.customer;

import com.socotra.coremodel.*;
import com.socotra.coremodel.interfaces.EventType;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum TenantCustomEvent implements com.socotra.coremodel.TenantCustomEvent {
    ;
    public static final String TYPE = "TenantCustomEvent";

    private final EventType eventType;
    private final Optional<TenantEventSchedule> schedule;
    private final boolean isPersisted;

    TenantCustomEvent(String eventTypeId, TenantEventSchedule schedule, boolean isPersisted) {
        this.eventType = EventType.of(eventTypeId);
        this.schedule = Optional.ofNullable(schedule);
        this.isPersisted = isPersisted;
    }

    public static Map <String, com.socotra.coremodel.TenantCustomEvent> map() {
        return Arrays.stream(values()).collect(Collectors.toMap(
                e -> e.eventType().id(),
                Function.identity()));
    }

    @Override
    public EventType eventType() {
        return eventType;
    }

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public Optional<TenantEventSchedule> schedule() {
        return schedule;
    }

    @Override
    public boolean isPersisted() {
        return isPersisted;
    }
}