package com.gymfex.usuarios_service.infrastructure.events;

import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
public class SocioEvent {
    private String eventId = UUID.randomUUID().toString();
    private String eventType; // SOCIO_CREATED, SOCIO_UPDATED, MEMBERSHIP_RENEWED, SOCIO_DELETED
    private Instant occurredAt = Instant.now();
    private String source = "usuarios-service";
    private Object payload; 
    
}
