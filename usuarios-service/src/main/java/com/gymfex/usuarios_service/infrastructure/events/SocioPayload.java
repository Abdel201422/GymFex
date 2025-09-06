package com.gymfex.usuarios_service.infrastructure.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SocioPayload {
    private Long id;
    private String email;
    private String nombre;
    private String apellidos;
    private String tipoMembresia; 
    private LocalDate finMembresia;
 
}
