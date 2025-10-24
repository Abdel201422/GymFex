package com.gymfex.common.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminEvent {
    private String eventId;
    private String eventType;
    private Instant occurredAt;
    private String source;

    @JsonProperty("adminPayload")
    private AdminPayload payload;

    public static AdminEvent of(String eventType, AdminPayload payload) {
        return new AdminEvent(
            UUID.randomUUID().toString(),
            eventType,
            Instant.now(),
            "usuarios-service",
            payload
        );
    }
}
