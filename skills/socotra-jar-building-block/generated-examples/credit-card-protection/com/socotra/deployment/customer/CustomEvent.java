package com.socotra.deployment.customer;

import com.socotra.coremodel.*;
import com.socotra.coremodel.interfaces.EventType;
import com.socotra.deployment.DeploymentConfig;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum CustomEvent implements com.socotra.coremodel.CustomEvent, Validatable {
    ;

    public static final String TYPE = "CustomEvent";

    private final EventType eventType;

    CustomEvent(String eventTypeId) {
        this.eventType = EventType.of(eventTypeId);
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
    public Collection<ValidationItem> validate(DeploymentConfig config, ValidatableContext context) {
        return List.of();
    }
}