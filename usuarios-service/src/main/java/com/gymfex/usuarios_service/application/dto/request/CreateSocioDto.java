package com.gymfex.usuarios_service.application.dto.request;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;


// DTO para crear un socio
@Data
public class CreateSocioDto {
    @NotBlank      private String nombre;
    @NotBlank      private String apellidos;
    @Email         private String email;
                   private String telefono;
    @NotBlank      private String tipoMembresia;
    @NotNull       private LocalDate inicioMembresia;
    @NotNull       private LocalDate finMembresia;
}