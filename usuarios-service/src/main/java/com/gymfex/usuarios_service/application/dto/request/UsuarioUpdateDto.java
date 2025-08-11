package com.gymfex.usuarios_service.application.dto.request;

import java.time.LocalDate;


import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;


@Data
public class UsuarioUpdateDto {

    

    @Size(max = 100)
    private String nombre;

    @Size(max = 150)
    private String apellidos;

    @Email
    @Size(max = 255)
    private String email;

    @Size(min = 6, max = 255)
    private String password;

    @Size(max = 20)
    private String telefono;

    @Size(max = 50)
    private String tipoMembresia;

    private LocalDate inicioMembresia;
    private LocalDate finMembresia;
}
