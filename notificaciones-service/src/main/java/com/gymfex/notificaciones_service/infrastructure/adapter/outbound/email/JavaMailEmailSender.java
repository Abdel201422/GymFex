package com.gymfex.notificaciones_service.infrastructure.adapter.outbound.email;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class JavaMailEmailSender {

    private final JavaMailSender mailSender;

    public JavaMailEmailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(body);
        msg.setFrom("da9aa40f00023f@inbox.mailtrap.io"); // <-- Remitente Mailtrap
        mailSender.send(msg);
    }
}
