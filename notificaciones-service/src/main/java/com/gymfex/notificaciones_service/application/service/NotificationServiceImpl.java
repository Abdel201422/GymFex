package com.gymfex.notificaciones_service.application.service;

import com.gymfex.common.events.SocioEvent;
import com.gymfex.common.events.SocioPayload;
import com.gymfex.notificaciones_service.infrastructure.adapter.outbound.persistence.JpaProcessedEventRepository;
import com.gymfex.notificaciones_service.infrastructure.adapter.outbound.email.JavaMailEmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
                default:
                    log.info("Tipo de evento no gestionado: {} (eventId={})", type, event.getEventId());
                    break;
            }
            // marcar como procesado si todo ha ido bien
            processedRepo.saveProcessedEvent(event.getEventId());
        } catch (Exception ex) {
            log.error("Error procesando evento {}: {}", event.getEventId(), ex.getMessage(), ex);
            throw ex;
        }
    }

    private void sendWelcome(SocioPayload p) {
    String subject = "¡Bienvenido a GymFex, " + safe(p.getNombre()) + "!";
    String body = "Hola " + safe(p.getNombre()) + " " + safe(p.getApellidos()) + ",\n\n"
            + "¡Nos alegra mucho darte la bienvenida a la familia GymFex! A partir de ahora podrás disfrutar de nuestras instalaciones, "
            + "clases y asesoramiento personalizado para alcanzar tus objetivos.\n\n"
            + "Tu membresía estará activa hasta el día: " + safeString(p.getFinMembresia()) + ". "
            + "Recuerda que puedes renovar fácilmente desde nuestra app o directamente en recepción.\n\n"
            + "¡Gracias por elegirnos para acompañarte en tu camino hacia una vida más activa y saludable!\n\n"
            + "El equipo de GymFex.";
    emailSender.sendEmail(p.getEmail(), subject, body);
}


    private void sendUpdated(SocioPayload p) {
    String subject = "Actualización de tus datos en GymFex";
    String body = "Hola " + safe(p.getNombre()) + ",\n\n"
            + "Te informamos que tus datos en GymFex han sido actualizados correctamente. "
            + "Si tú solicitaste este cambio, no es necesario que hagas nada más.\n\n"
            + "En caso de que no hayas realizado ninguna modificación, por favor contacta con nosotros lo antes posible "
            + "para revisar tu información y mantener la seguridad de tu cuenta.\n\n"
            + "Gracias por mantener tus datos al día.\n\n"
            + "El equipo de GymFex.";
    emailSender.sendEmail(p.getEmail(), subject, body);
}


    private void sendDeleted(SocioPayload p) {
    String subject = "Tu baja en GymFex";
    String body = "Hola " + safe(p.getNombre()) + ",\n\n"
            + "Lamentamos informarte que tu cuenta en GymFex ha sido dada de baja. "
            + "A partir de este momento ya no tendrás acceso a nuestros servicios ni a las instalaciones.\n\n"
            + "Si esta baja fue solicitada por ti, esperamos volver a verte pronto. "
            + "Y si crees que ha sido un error, por favor comunícate con nosotros para ayudarte a resolverlo.\n\n"
            + "Gracias por haber sido parte de GymFex.\n\n"
            + "El equipo de GymFex.";
    emailSender.sendEmail(p.getEmail(), subject, body);
}


    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String safeString(Object o) {
        return o == null ? "desconocida" : o.toString();
    }
}
