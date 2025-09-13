package com.gymfex.usuarios_service.application.service;

import com.gymfex.usuarios_service.application.dto.response.UsuariosDto;
import com.gymfex.usuarios_service.domain.Usuario;
import com.gymfex.usuarios_service.infrastructure.events.SocioEvent;
import com.gymfex.usuarios_service.infrastructure.events.SocioPayload;
import com.gymfex.usuarios_service.infrastructure.repository.usuarioRepository;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.gymfex.usuarios_service.application.mapper.UsuarioMapper;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import com.gymfex.usuarios_service.application.dto.request.CreateSocioDto;
import com.gymfex.usuarios_service.application.dto.request.UsuarioUpdateDto;
import com.gymfex.usuarios_service.application.dto.request.CreateAdminDto;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class usuarioServiceImpl implements usuarioService {

    private final usuarioRepository usuarioRepository;
    private final UsuarioMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final KafkaTemplate<String, SocioEvent> kafkaTemplate; // <- cambiado a SocioEvent

    public usuarioServiceImpl(usuarioRepository usuarioRepository,
                              UsuarioMapper mapper,
                              PasswordEncoder passwordEncoder,
                              KafkaTemplate<String, SocioEvent> kafkaTemplate) { // <- tipo cambiado aquí
        this.usuarioRepository = usuarioRepository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public List<UsuariosDto> getUsuarios() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        return usuarios.stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public Optional<UsuariosDto> findById(Long id) {
        return usuarioRepository.findById(id)
                .map(mapper::toDto);
    }

    @Override
    public List<UsuariosDto> buscarPorNombre(String nombre, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);

        Page<Usuario> usuariosPage;

        if (nombre == null || nombre.isBlank()) {
            usuariosPage = usuarioRepository.findAll(pageable);
        } else {
            usuariosPage = usuarioRepository.buscarPorNombre(nombre, pageable);
        }

        return usuariosPage.getContent()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public Usuario createSocioAndReturnEntity(CreateSocioDto dto) {
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email ya registrado");
        }
        Usuario usuario = mapper.toEntity(dto);
        usuario.setRole("SOCIO");
        // guardamos y usamos la entidad resultante (para asegurar id generado)
        Usuario savedUsuario = usuarioRepository.save(usuario);

        // construir el payload y el evento
        SocioPayload payload = new SocioPayload(
            savedUsuario.getId(),
            savedUsuario.getEmail(),
            savedUsuario.getNombre(),
            savedUsuario.getApellidos(),
            savedUsuario.getTipoMembresia(),
            savedUsuario.getFinMembresia()
        );

        SocioEvent evt = new SocioEvent();
        evt.setEventType("SOCIO_CREATED");
        evt.setSocioPayload(payload);

        // publicar (topic) usando KafkaTemplate<String, SocioEvent>
        kafkaTemplate.send("usuarios.socio.created", savedUsuario.getId().toString(), evt);

        return savedUsuario;
    }

    @Override
    public Usuario createAdminAndReturnEntity(CreateAdminDto dto) {
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email ya registrado");
        }
        Usuario usuario = mapper.toEntity(dto);
        usuario.setRole("ADMIN");
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password requerida");
        }
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuarioRepository.save(usuario);
        return usuario;
    }

    @Override 
    public Optional<Usuario> findEntityById(Long id) {
        return usuarioRepository.findById(id);
    }

    @Override
    @Transactional
    public void updateAdmin(Usuario usuario, UsuarioUpdateDto dto) {
        if (dto.getNombre() != null && !dto.getNombre().isBlank()) {
            usuario.setNombre(dto.getNombre().trim());
        }
        if (dto.getApellidos() != null && !dto.getApellidos().isBlank()) {
            usuario.setApellidos(dto.getApellidos().trim());
        }
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            String email = dto.getEmail().trim().toLowerCase();
            if (usuarioRepository.existsByEmailAndIdNot(email, usuario.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email ya registrado");
            }
            usuario.setEmail(email);
        }
        if (dto.getTelefono() != null && !dto.getTelefono().isBlank()) {
            usuario.setTelefono(dto.getTelefono().trim());
        }
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        usuarioRepository.save(usuario);
    }

    @Override
    public void updateSocio(Usuario usuario, UsuarioUpdateDto dto) {
        if (dto.getNombre() != null) usuario.setNombre(dto.getNombre());
        if (dto.getApellidos() != null) usuario.setApellidos(dto.getApellidos());
        if (dto.getEmail() != null) usuario.setEmail(dto.getEmail());
        if (dto.getTelefono() != null) usuario.setTelefono(dto.getTelefono());
        if (dto.getTipoMembresia() != null) usuario.setTipoMembresia(dto.getTipoMembresia());
        if (dto.getInicioMembresia() != null) usuario.setInicioMembresia(dto.getInicioMembresia());
        if (dto.getFinMembresia() != null) usuario.setFinMembresia(dto.getFinMembresia());
        usuarioRepository.save(usuario);
    }

    @Override
    public void deleteUsuario(Long id) {
        try {
            usuarioRepository.deleteById(id);
        } catch (EmptyResultDataAccessException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }
    }

    @Override
    public List<UsuariosDto> getAdministradores() {
        List<Usuario> admins = usuarioRepository.findAllByRoleIgnoreCase("ADMIN");
        return admins.stream().map(mapper::toDto).toList();
    }

    @Override
    public List<UsuariosDto> getSocios() {
        List<Usuario> socios = usuarioRepository.findAllByRoleIgnoreCase("SOCIO");
        return socios.stream().map(mapper::toDto).toList();
    }

    @Override
    public List<UsuariosDto> buscarPorRole(String role, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<Usuario> pageRes = usuarioRepository.findAllByRoleIgnoreCase(role, pageable);
        return pageRes.getContent().stream().map(mapper::toDto).toList();
    }

    @Override
    public boolean checkPassword(Usuario usuario, String rawPassword) {
        return passwordEncoder.matches(rawPassword, usuario.getPassword());
    }
    @Override
    public Usuario findEntityByEmail(String email) {
        return usuarioRepository.findByEmail(email).orElse(null);
    }
}
