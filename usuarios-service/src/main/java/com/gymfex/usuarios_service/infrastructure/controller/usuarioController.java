package com.gymfex.usuarios_service.infrastructure.controller;

import com.gymfex.usuarios_service.application.dto.request.CreateSocioDto;
import com.gymfex.usuarios_service.application.dto.request.UsuarioUpdateDto;
import com.gymfex.usuarios_service.application.dto.request.CreateAdminDto;
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
@RequestMapping("/usuarios") // Ruta base para todos los endpoints
public class usuarioController {

    private final usuarioService usuarioService;

    public usuarioController(usuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // 1) Obtener todos los usuarios (GET /usuarios)
    @GetMapping
    public List<UsuariosDto> getUsuarios() {
        return usuarioService.getUsuarios();
    }

    // 2) Obtener usuario por ID (GET /usuarios/{id})
    @GetMapping("/{id}")
    public ResponseEntity<UsuariosDto> getUsuarioPorId(@PathVariable Long id) {
        return usuarioService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    // 3) Búsqueda con parámetros (GET /usuarios/search?nombre=...)
    @GetMapping("/search")
    public ResponseEntity<List<UsuariosDto>> buscarUsuarios(
        @RequestParam(required = false) String nombre,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        List<UsuariosDto> lista = usuarioService.buscarPorNombre(nombre, page, size);
        return ResponseEntity.ok(lista);
    }

    // 4) Crear un nuevo usuario (POST /usuarios)
   @PostMapping("/socio")
   public ResponseEntity<String> crearSocio(@Valid @RequestBody CreateSocioDto dto) {
        usuarioService.createSocio(dto);
        return ResponseEntity.status(201).body("Socio creado correctamente");
    }

    // 5) Crear un nuevo administrador (POST /usuarios/admin)
    @PostMapping("/admin")
    public ResponseEntity<String> crearAdmin(@Valid @RequestBody CreateAdminDto dto) {
        usuarioService.createAdmin(dto);
        return ResponseEntity.status(201).body("Administrador creado correctamente");
    }
    
    // 6) Actualizar un usuario (PUT /usuarios/{id})
    @PutMapping("/{id}")
    public ResponseEntity<String> actualizarUsuario(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioUpdateDto dto) {
        
        Optional<Usuario> opt = usuarioService.findEntityById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Usuario usuario = opt.get();
        String role = usuario.getRole();  

        if ("ADMIN".equalsIgnoreCase(role)) {
            // actualizamos solo los campos válidos para admin
            usuarioService.updateAdmin(usuario, dto);
        }
        else if ("SOCIO".equalsIgnoreCase(role)) {
            usuarioService.updateSocio(usuario, dto);
        }
        else {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Role no válido: " + role);
        }

        return ResponseEntity.ok("Usuario actualizado correctamente");
    }

    // 7) Eliminar un usuario (DELETE /usuarios/{id})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {

        usuarioService.deleteUsuario(id);
        return ResponseEntity.ok().build();
    }

}