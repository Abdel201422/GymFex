package com.gymfex.usuarios_service.infrastructure.events;

import lombok.Data;

@Data
public class UsuarioEvent {
    public enum Tipo { CREADO, ACTUALIZADO, ELIMINADO }

    private Long id;
    private String role;
    private Tipo tipo;
    
}
