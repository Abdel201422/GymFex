package com.gymfex.usuarios_service.application.dto.response;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UsuarioDetailDto {

    private Long id;
    private String nombre;
    private String apellidos;
    private String email;
    private String telefono;
    private String role;
    private String tipoMembresia;
    private LocalDate inicioMembresia;
    private LocalDate finMembresia;
    private String estado;
}
