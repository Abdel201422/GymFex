package com.gymfex.notificaciones_service.infrastructure.events;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gymfex.notificaciones_service.application.service.NotificationService;
import com.gymfex.common.events.SocioPayload;
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

    /**
     * Listener que recibe payload como String (JSON) y lo mapea a SocioEvent local.
     * Topic ejemplo: usuarios.socio.created (ajusta si usas otro)
     */
    @KafkaListener(topics = "usuarios.socio.created", groupId = "notificaciones-service")
    public void listen(String payload, Acknowledgment ack, ConsumerRecord<?, ?> record) {
        try {
            log.debug("Recibido mensaje en partition {} offset {}",
                    record.partition(), record.offset());

            SocioEvent event = objectMapper.readValue(payload, SocioEvent.class);

            if (event == null) {
                log.warn("Payload mapeado a null, offset {}", record.offset());
                ack.acknowledge();
                return;
            }

            notificationService.handleSocioEvent(event);

            // confirmar consumo si todo OK
            ack.acknowledge();
        } catch (Exception ex) {
            // No acknowledges -> deja que el ErrorHandlingDeserializer / la configuración de retries decida.
            log.error("Error deserializando o procesando payload: {} — payload: {}", ex.getMessage(), payload);
            // Si deseas descartar o enviar a DLQ, configura DefaultErrorHandler/DeadLetterPublishingRecoverer.
            throw new RuntimeException(ex);
        }
    }
}
