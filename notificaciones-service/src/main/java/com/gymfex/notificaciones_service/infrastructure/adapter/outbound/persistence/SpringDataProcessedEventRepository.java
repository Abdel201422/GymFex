package com.gymfex.notificaciones_service.infrastructure.adapter.outbound.persistence;


import com.gymfex.notificaciones_service.domain.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataProcessedEventRepository extends JpaRepository<ProcessedEvent, Long> {
    Optional<ProcessedEvent> findByEventId(String eventId);
}
