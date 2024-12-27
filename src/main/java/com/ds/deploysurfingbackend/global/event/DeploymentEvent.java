package com.ds.deploysurfingbackend.global.event;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public abstract class DeploymentEvent {
    private final String eventId;
    private final LocalDateTime localDateTime;
    private final EventType eventType;
    private final String payload;

    public DeploymentEvent(EventType eventType, String payload) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.localDateTime = LocalDateTime.now();
        this.payload = payload;
    }
}
