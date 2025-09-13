package com.gymfex.notificaciones_service.infrastructure.adapter.outbound.persistence;


import com.gymfex.notificaciones_service.domain.ProcessedEvent;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class JpaProcessedEventRepository {

    private final SpringDataProcessedEventRepository repo;

    public JpaProcessedEventRepository(SpringDataProcessedEventRepository repo) {
        this.repo = repo;
    }

    public boolean existsByEventId(String eventId) {
        return repo.findByEventId(eventId).isPresent();
    }

    public void saveProcessedEvent(String eventId) {
        ProcessedEvent e = new ProcessedEvent();
        e.setEventId(eventId);
        e.setProcessedAt(OffsetDateTime.now());
        repo.save(e);
    }
}
