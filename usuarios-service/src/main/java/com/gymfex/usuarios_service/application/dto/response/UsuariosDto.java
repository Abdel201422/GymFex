package com.gymfex.usuarios_service.application.dto.response;

import lombok.Data;

@Data
public class UsuariosDto {
    private String nombre;
    private String apellidos;
    private String email;
    private String telefono;
    private String role;

}
