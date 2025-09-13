package com.gymfex.notificaciones_service.application.service;

import com.gymfex.notificaciones_service.infrastructure.events.SocioEvent;

public interface NotificationService {
    void handleSocioEvent(SocioEvent event);
}