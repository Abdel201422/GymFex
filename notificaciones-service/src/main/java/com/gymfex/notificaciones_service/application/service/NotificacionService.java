package com.gymfex.notificaciones_service.application.service;

import com.gymfex.notificaciones_service.domain.model.Socio;


@Service
public class NotificacionService {
  
  @Autowired
  private EmailService emailService;
  
  @Autowired
  private KafkaTemplate<String, String> kafkaTemplate;
  
  @Value("${notificaciones.topic}")
  private String topic;
  
  @Value("${notificaciones.administradores}")
  private String administradores;
  
  public void enviarNotificacion(Socio socio, TipoNotificacion tipoNotificacion) {
    // Crea el mensaje que se va a enviar por correo electrónico
    String mensaje = crearMensaje(socio, tipoNotificacion);
    
    // Envía el correo electrónico al socio
    emailService.enviarCorreo(socio.getEmail(), mensaje);
    
    // Envía el correo electrónico a los administradores
    emailService.enviarCorreo(administradores, mensaje);
  }
  
  private String crearMensaje(Socio socio, TipoNotificacion tipoNotificacion) {
    // Crea el mensaje que se va a enviar por correo electrónico
    switch (tipoNotificacion) {
      case CREACION:
        return "Se ha creado un nuevo socio con id " + socio.getId();
      case ACTUALIZACION:
        return "Se ha actualizado el socio con id " + socio.getId();
      case ELIMINACION:
        return "Se ha eliminado el socio con id " + socio.getId();
      default:
        return "";
    }
  }
}
