package com.gymfex.usuarios_service.application.dto.request;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Data
public class CreateAdminDto {
    @NotBlank
    private String nombre;
    @NotBlank
    private String apellidos;
    @Email
    private String email;
    @NotBlank
    private String telefono;
    @NotBlank
    private String password;
}