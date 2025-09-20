package com.gymfex.notificaciones_service.application.service;

import com.gymfex.common.events.SocioEvent;

public interface NotificationService {
    void handleSocioEvent(SocioEvent event);
}