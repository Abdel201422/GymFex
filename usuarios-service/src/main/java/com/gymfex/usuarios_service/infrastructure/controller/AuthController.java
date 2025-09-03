package com.gymfex.usuarios_service.infrastructure.controller;

import com.gymfex.usuarios_service.application.dto.request.CreateAdminDto;
import com.gymfex.usuarios_service.application.dto.request.CreateSocioDto;
import com.gymfex.usuarios_service.application.service.usuarioService;
import com.gymfex.usuarios_service.domain.Usuario;
import com.gymfex.usuarios_service.infrastructure.config.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

record AuthRequest(String email, String password) {}
record AuthResponse(String token) {}

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final usuarioService usuarioService;
    private final JwtService jwtService;

    public AuthController(usuarioService usuarioService, JwtService jwtService) {
        this.usuarioService = usuarioService;
        this.jwtService = jwtService;
    }

    /* @PostMapping("/register/socio")
    public ResponseEntity<AuthResponse> registerSocio(@Valid @RequestBody CreateSocioDto dto) {
        // IMPORTANTE: CreateSocioDto debe incluir password si quieres que el socio tenga password
        Usuario u = usuarioService.createSocioAndReturnEntity(dto);
        String token = jwtService.generateToken(u.getEmail(), u.getRole());
        return ResponseEntity.status(201).body(new AuthResponse(token));
    }*/

    @PostMapping("/register/admin")
    public ResponseEntity<AuthResponse> registerAdmin(@Valid @RequestBody CreateAdminDto dto) {
        Usuario u = usuarioService.createAdminAndReturnEntity(dto);
        String token = jwtService.generateToken(u.getEmail(), u.getRole());
        return ResponseEntity.status(201).body(new AuthResponse(token));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest req) {
        Usuario u = usuarioService.findEntityByEmail(req.email());
        if (u == null) {
            return ResponseEntity.status(401).body("usario no encontrado");
        }
        if (!usuarioService.checkPassword(u, req.password())) {
            return ResponseEntity.status(401).body("Credenciales inválidas");
        }
        String token = jwtService.generateToken(u.getEmail(), u.getRole());
        return ResponseEntity.ok(new AuthResponse(token));
    }
}
