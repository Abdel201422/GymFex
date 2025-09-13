package com.gymfex.notificaciones_service.application.service;

import com.gymfex.notificaciones_service.infrastructure.events.SocioEvent;
import com.gymfex.notificaciones_service.infrastructure.events.SocioPayload;
import com.gymfex.notificaciones_service.infrastructure.adapter.outbound.persistence.JpaProcessedEventRepository;
import com.gymfex.notificaciones_service.infrastructure.adapter.outbound.email.JavaMailEmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;

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
        if (event == null || event.getEventId() == null) {
            log.warn("Evento nulo o sin eventId, se ignora");
            return;
        }

        if (processedRepo.existsByEventId(event.getEventId())) {
            // ya procesado -> idempotencia
            log.debug("Evento ya procesado: {}", event.getEventId());
            return;
        }

        SocioPayload p = event.getPayload();
        if (p == null) {
            log.warn("Evento {} sin payload", event.getEventId());
            processedRepo.saveProcessedEvent(event.getEventId());
            return;
        }

        String type = event.getEventType();
        try {
            switch (type) {
                case "SOCIO_CREATED":
                    sendWelcome(p);
                    break;
                case "SOCIO_UPDATED":
                    sendUpdated(p);
                    break;
                case "SOCIO_DELETED":
                    sendDeleted(p);
                    break;
                case "MEMBERSHIP_UPDATED":
                case "MEMBERSHIP_RENEWED":
                    sendMembershipUpdated(p);
                    break;
                case "MEMBERSHIP_EXPIRING":
                    sendExpiringReminder(p);
                    break;
                default:
                    log.info("Tipo de evento no gestionado: {} (eventId={})", type, event.getEventId());
                    break;
            }
            // marcar como procesado si todo ha ido bien
            processedRepo.saveProcessedEvent(event.getEventId());
        } catch (Exception ex) {
            log.error("Error procesando evento {}: {}", event.getEventId(), ex.getMessage(), ex);
            // no marcar como procesado -> se podrá reintentar (dependiendo de tu DLQ/retry)
            throw ex;
        }
    }

    private void sendWelcome(SocioPayload p) {
        String subject = "Bienvenido a GymFex, " + safe(p.getNombre());
        String body = "Hola " + safe(p.getNombre()) + " " + safe(p.getApellidos())
                + ",\nTu membresía finaliza: " + safeString(p.getFinMembresia());
        emailSender.sendEmail(p.getEmail(), subject, body);
        emailSender.sendEmail("admins@gymfex.com", "Nuevo socio: " + p.getEmail(),
                "Se creó un nuevo socio: " + safe(p.getNombre()) + " " + safe(p.getApellidos()));
    }

    private void sendUpdated(SocioPayload p) {
        emailSender.sendEmail(p.getEmail(), "Tus datos han sido actualizados", "Se han modificado tus datos.");
    }

    private void sendDeleted(SocioPayload p) {
        emailSender.sendEmail(p.getEmail(), "Baja en GymFex", "Tu cuenta ha sido dada de baja.");
    }

    private void sendMembershipUpdated(SocioPayload p) {
        emailSender.sendEmail(p.getEmail(), "Membresía actualizada", "Tu nueva fecha fin: " + safeString(p.getFinMembresia()));
    }

    private void sendExpiringReminder(SocioPayload p) {
        long dias = -1;
        if (p.getFinMembresia() != null) {
            try {
                dias = ChronoUnit.DAYS.between(java.time.LocalDate.now(), p.getFinMembresia());
            } catch (Exception e) {
                log.warn("No se pudo calcular días restante para {}: {}", p.getEmail(), e.getMessage());
            }
        }
        String body = "Tu membresía expira en " + dias + " días (" + safeString(p.getFinMembresia()) + "). Renueva pronto.";
        emailSender.sendEmail(p.getEmail(), "Recordatorio: membresía próxima a vencer", body);
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String safeString(Object o) {
        return o == null ? "desconocida" : o.toString();
    }
}
