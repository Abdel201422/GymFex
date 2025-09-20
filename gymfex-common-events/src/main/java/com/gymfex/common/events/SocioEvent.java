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
public class SocioEvent {
    private String eventId;
    private String eventType;
    private Instant occurredAt;
    private String source;

    @JsonProperty("socioPayload")
    private SocioPayload payload;

    public static SocioEvent of(String eventType, SocioPayload payload) {
        return new SocioEvent(
            UUID.randomUUID().toString(),
            eventType,
            Instant.now(),
            "usuarios-service",
            payload
        );
    }
}
