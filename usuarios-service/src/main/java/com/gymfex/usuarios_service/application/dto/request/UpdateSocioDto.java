package com.gymfex.usuarios_service.application.dto.request;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Data
public class UpdateSocioDto {
    @Size(max = 100)
    private String nombre;

    @Size(max = 100)
    private String apellidos;

    @Email
    private String email;

    @Size(max = 20)
    private String telefono;

    private String tipoMembresia;

    private LocalDate inicioMembresia;
    private LocalDate finMembresia;
}
