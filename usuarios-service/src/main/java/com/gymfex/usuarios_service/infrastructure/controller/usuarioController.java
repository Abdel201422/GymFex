package com.gymfex.usuarios_service.infrastructure.controller;

import com.gymfex.usuarios_service.application.dto.request.CreateSocioDto;
import com.gymfex.usuarios_service.application.dto.request.UpdateAdminDto;
import com.gymfex.usuarios_service.application.dto.request.CreateAdminDto;
import com.gymfex.usuarios_service.application.dto.request.UpdateSocioDto;
import com.gymfex.usuarios_service.application.dto.response.UsuariosDto;
import com.gymfex.usuarios_service.application.service.usuarioService;
import com.gymfex.usuarios_service.domain.Usuario;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/usuarios")
public class usuarioController {

    private final usuarioService usuarioService;

    public usuarioController(usuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/administradores")
    public List<UsuariosDto> getAdministradores() {
        return usuarioService.getAdministradores();
    }

    @GetMapping("/socios")
    public List<UsuariosDto> getSocios() {
        return usuarioService.getSocios();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuariosDto> getUsuarioPorId(@PathVariable Long id) {
        return usuarioService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<UsuariosDto>> buscarUsuarios(
            @RequestParam(required = false) String nombre,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<UsuariosDto> lista = usuarioService.buscarPorNombre(nombre, page, size);
        return ResponseEntity.ok(lista);
    }

    @PostMapping("/socio")
    public ResponseEntity<String> crearSocio(@Valid @RequestBody CreateSocioDto dto) {

        usuarioService.createSocioAndReturnEntity(dto);
        return ResponseEntity.status(201).body("Socio creado correctamente");
    }

    @PostMapping("/admin")
    public ResponseEntity<String> crearAdmin(@Valid @RequestBody CreateAdminDto dto) {
        usuarioService.createAdminAndReturnEntity(dto);
        return ResponseEntity.status(201).body("Administrador creado correctamente");
    }

    @PutMapping("/admin/{id}")
    public ResponseEntity<String> actualizarAdmin(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAdminDto adminDto) {

        Optional<Usuario> opt = usuarioService.findEntityById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Usuario usuario = opt.get();
        if (!"ADMIN".equalsIgnoreCase(usuario.getRole())) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Role no válido: " + usuario.getRole());
        }

        usuarioService.updateAdmin(usuario, adminDto);
        return ResponseEntity.ok("Admin actualizado correctamente");
    }

    @PutMapping("/socio/{id}")
    public ResponseEntity<String> actualizarSocio(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSocioDto socioDto) {

        Optional<Usuario> opt = usuarioService.findEntityById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Usuario usuario = opt.get();
        if (!"SOCIO".equalsIgnoreCase(usuario.getRole())) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Role no válido: " + usuario.getRole());
        }

        usuarioService.updateSocio(usuario, socioDto);
        return ResponseEntity.ok("Socio actualizado correctamente");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {

        usuarioService.deleteUsuario(id);
        return ResponseEntity.ok().build();
    }

}