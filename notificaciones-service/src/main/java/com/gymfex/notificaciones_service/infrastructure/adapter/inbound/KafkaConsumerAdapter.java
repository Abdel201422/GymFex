package com.gymfex.notificaciones_service.infrastructure.adapter.inbound;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymfex.notificaciones_service.application.service.NotificationService;
import com.gymfex.notificaciones_service.infrastructure.events.SocioEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumerAdapter {

    private final NotificationService notificationService;

    public KafkaConsumerAdapter(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = {
            "usuarios.socio.created",
            "usuarios.socio.updated",
            "usuarios.socio.deleted",
            "usuarios.socio.membership.renewed",
            "usuarios.socio.membership.expiring"
    }, containerFactory = "kafkaListenerContainerFactory")
    public void listen(String message) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        try {
            SocioEvent event = mapper.readValue(message, SocioEvent.class);
            notificationService.handleSocioEvent(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
