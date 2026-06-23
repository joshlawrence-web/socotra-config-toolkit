package com.socotra.deployment.customer;

import com.socotra.coremodel.*;
import com.socotra.coremodel.interfaces.EventType;
import com.socotra.deployment.DeploymentConfig;
import java.util.*;
import java.math.BigDecimal;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum CustomEvent implements com.socotra.coremodel.CustomEvent {
    ;
    public static final String TYPE = "CustomEvent";

    private final EventType eventType;
    private final Optional<EventSchedule> schedule;

    CustomEvent(String eventTypeId, EventSchedule schedule) {
        this.eventType = EventType.of(eventTypeId);
        this.schedule = Optional.ofNullable(schedule);
    }

    public static Map <String, com.socotra.coremodel.CustomEvent> map() {
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
    public Optional<EventSchedule> schedule() {
        return schedule;
    }
}