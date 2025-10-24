package com.gymfex.common.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminPayload {
    private Long id;
    private String email;
    private String nombre;
    private String apellidos;
    private String telefono;
}
