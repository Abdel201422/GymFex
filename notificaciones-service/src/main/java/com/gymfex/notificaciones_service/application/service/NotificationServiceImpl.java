package com.gymfex.notificaciones_service.application.service;

import com.gymfex.common.events.SocioEvent;
import com.gymfex.common.events.SocioPayload;
import com.gymfex.notificaciones_service.infrastructure.adapter.outbound.persistence.JpaProcessedEventRepository;
import com.gymfex.notificaciones_service.infrastructure.adapter.outbound.email.JavaMailEmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailSendException;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final JavaMailEmailSender emailSender;
    private final JpaProcessedEventRepository processedRepo;

    public NotificationServiceImpl(JavaMailEmailSender emailSender,
                                   JpaProcessedEventRepository processedRepo) {
        this.emailSender = emailSender;
        this.processedRepo = processedRepo;
    }

    @Override
    public void handleSocioEvent(SocioEvent event) {
        log.info("Procesando evento {} tipo {}", event == null ? "null" : event.getEventId(), event == null ? "null" : event.getEventType());

        if (event == null || event.getEventId() == null) {
            log.warn("Evento nulo o sin eventId, se ignora");
            return;
        }

        if (processedRepo.existsByEventId(event.getEventId())) {
            log.debug("Evento ya procesado: {}", event.getEventId());
            return;
        }

        SocioPayload p = event.getPayload();
        if (p == null) {
            log.warn("Evento {} sin payload", event.getEventId());
            processedRepo.saveProcessedEvent(event.getEventId());
            return;
        }

        try {
            String type = event.getEventType();
            switch (type) {
                case "SOCIO_CREATED" -> sendWelcome(p);
                case "SOCIO_UPDATED" -> sendUpdated(p);
                case "SOCIO_DELETED" -> sendDeleted(p);
                default -> log.info("Tipo de evento no gestionado: {} (eventId={})", type, event.getEventId());
            }
            processedRepo.saveProcessedEvent(event.getEventId());
        } catch (MailSendException mse) {
            log.error("Error enviando correo (SMTP) para eventId={}: {}", event.getEventId(), mse.getMessage());
            // Marcar como procesado para evitar reintentos inmediatos y saturación del SMTP.
            processedRepo.saveProcessedEvent(event.getEventId());
            // Opcional: persistir en tabla de fallos para reintento manual/por batch.
        } catch (Exception ex) {
            log.error("Error procesando evento {}: {}", event.getEventId(), ex.getMessage(), ex);
            throw ex;
        }
    }

    private void sendWelcome(SocioPayload p) {
        String subject = "¡Bienvenido a GymFex, " + safe(p.getNombre()) + "!";
        String body = "Hola " + safe(p.getNombre()) + " " + safe(p.getApellidos()) + ",\n\n"
                + "¡Nos alegra darte la bienvenida a GymFex! Tu membresía está activa hasta: " + safeString(p.getFinMembresia()) + ".\n\n"
                + "El equipo de GymFex.";
        log.info("Enviando email de bienvenida a {}", p.getEmail());
        emailSender.sendEmail(p.getEmail(), subject, body);
    }

    private void sendUpdated(SocioPayload p) {
        String subject = "Actualización de tus datos en GymFex";
        String body = "Hola " + safe(p.getNombre()) + ",\n\n"
                + "Tus datos han sido actualizados correctamente.\n\n"
                + "El equipo de GymFex.";
        log.info("Enviando email de actualización a {}", p.getEmail());
        emailSender.sendEmail(p.getEmail(), subject, body);
    }

    private void sendDeleted(SocioPayload p) {
        String subject = "Tu baja en GymFex";
        String body = "Hola " + safe(p.getNombre()) + ",\n\n"
                + "Tu cuenta en GymFex ha sido dada de baja. Si fue un error, contacta con nosotros.\n\n"
                + "El equipo de GymFex.";
        log.info("Enviando email de baja a {}", p.getEmail());
        emailSender.sendEmail(p.getEmail(), subject, body);
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String safeString(Object o) {
        return o == null ? "desconocida" : o.toString();
    }
}
