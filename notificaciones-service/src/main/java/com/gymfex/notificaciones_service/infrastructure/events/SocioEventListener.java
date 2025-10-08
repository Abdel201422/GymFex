package com.gymfex.notificaciones_service.infrastructure.events;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gymfex.notificaciones_service.application.service.NotificationService;
import com.gymfex.common.events.SocioEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class SocioEventListener {

    private static final Logger log = LoggerFactory.getLogger(SocioEventListener.class);

    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    public SocioEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule()) // para LocalDate/LocalDateTime
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @KafkaListener(topics = "usuarios.socio.created", groupId = "notificaciones-service")
    public void listen(String payload, Acknowledgment ack, ConsumerRecord<?, ?> record) {
        try {
            SocioEvent event = objectMapper.readValue(payload, SocioEvent.class);
            if (event != null) {
                notificationService.handleSocioEvent(event);
            }
            ack.acknowledge();
        } catch (Exception ex) {
            log.error("Error procesando payload: {}", ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
}
