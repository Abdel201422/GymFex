package com.gymfex.usuarios_service.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;


@Entity
@Table(name = "usuario")
@Data
public class Usuario {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 150)
    private String apellidos;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(length = 255)
    private String password;

    @Column(length = 20)
    private String telefono;

    @Column(nullable = false, length = 50)
    private String role;

    @Column(length = 50)
    private String tipoMembresia;

    private LocalDate inicioMembresia;
    private LocalDate finMembresia;

    @Column(length = 20)
    private String estado;

    @Column(nullable = false)
    private OffsetDateTime creadoEn;

    @PrePersist
    public void prePersist() {
        this.creadoEn = OffsetDateTime.now();
        // Calcula estado según fechas de membresía
        if (this.tipoMembresia != null
         && this.inicioMembresia != null
         && this.finMembresia != null) {
            LocalDate hoy = LocalDate.now();
            if (hoy.isBefore(inicioMembresia))
                this.estado = "PENDIENTE";
            else if (!hoy.isAfter(finMembresia))
                this.estado = "ACTIVO";
            else
                this.estado = "VENCIDO";
        } else {
            this.estado = "INACTIVO";
        }
    }
}

