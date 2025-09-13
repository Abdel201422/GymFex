package com.gymfex.notificaciones_service.infrastructure.repository;


public interface processedEventRepository {
    boolean existsByEventId(String eventId);
    void saveProcessedEvent(String eventId);
}
