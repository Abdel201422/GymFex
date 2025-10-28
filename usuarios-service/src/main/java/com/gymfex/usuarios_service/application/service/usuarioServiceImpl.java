package com.gymfex.usuarios_service.application.service;

import com.gymfex.usuarios_service.application.dto.response.UsuariosDto;
import com.gymfex.usuarios_service.domain.Usuario;
import com.gymfex.common.events.SocioEvent;
import com.gymfex.common.events.SocioPayload;
import com.gymfex.common.events.AdminEvent;
import com.gymfex.common.events.AdminPayload;
import com.gymfex.usuarios_service.infrastructure.repository.usuarioRepository;
import com.gymfex.usuarios_service.application.mapper.UsuarioMapper;
import com.gymfex.usuarios_service.constants.Constants;
import com.gymfex.usuarios_service.application.dto.request.CreateSocioDto;
import com.gymfex.usuarios_service.application.dto.request.UpdateAdminDto;
import com.gymfex.usuarios_service.application.dto.request.UpdateSocioDto;
import com.gymfex.usuarios_service.application.dto.request.CreateAdminDto;
import com.gymfex.usuarios_service.constants.Constants;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;
import java.util.Optional;
import java.time.OffsetDateTime;
import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class usuarioServiceImpl implements usuarioService {

    private final usuarioRepository usuarioRepository;
    private final UsuarioMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final KafkaTemplate<String, SocioEvent> socioKafkaTemplate;
    private final KafkaTemplate<String, AdminEvent> adminKafkaTemplate;
    private static final Logger logger = LoggerFactory.getLogger(usuarioServiceImpl.class);

    public usuarioServiceImpl(usuarioRepository usuarioRepository,
                              UsuarioMapper mapper,
                              PasswordEncoder passwordEncoder,
                              @Qualifier("socioKafkaTemplate") KafkaTemplate<String, SocioEvent> socioKafkaTemplate,
                              @Qualifier("adminKafkaTemplate") KafkaTemplate<String, AdminEvent> adminKafkaTemplate) {
        this.usuarioRepository = usuarioRepository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
        this.socioKafkaTemplate = socioKafkaTemplate;
        this.adminKafkaTemplate = adminKafkaTemplate;
    }

    @Override
    public List<UsuariosDto> getUsuarios() {
        return usuarioRepository.findAll().stream()
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
        Page<Usuario> usuariosPage = (nombre == null || nombre.isBlank())
                ? usuarioRepository.findAll(pageable)
                : usuarioRepository.buscarPorNombre(nombre, pageable);
        return usuariosPage.getContent().stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public Usuario createSocioAndReturnEntity(CreateSocioDto dto) {
        logger.debug("createSocioAndReturnEntity dto : {}", dto);
        String emailNormalized = dto.getEmail() != null ? dto.getEmail().trim().toLowerCase() : null;
        if (emailNormalized == null || emailNormalized.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email requerido");
        }
        if (usuarioRepository.existsByEmail(emailNormalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email ya registrado");
        }

        Usuario usuario = mapper.toEntity(dto);
        usuario.setEmail(emailNormalized);
        usuario.setRole("SOCIO");
        usuario.setCreadoEn(OffsetDateTime.now());

        recalcularEstado(usuario);

        Usuario savedUsuario = usuarioRepository.save(usuario);

        SocioPayload payload = new SocioPayload(
                savedUsuario.getId(),
                savedUsuario.getEmail(),
                savedUsuario.getNombre(),
                savedUsuario.getApellidos(),
                savedUsuario.getTipoMembresia(),
                savedUsuario.getFinMembresia()
        );
        SocioEvent evt = SocioEvent.of("SOCIO_CREATED", payload);

        logger.debug("Publicando evento SocioEvent eventId={} eventType={} for usuarioId={}",
                evt.getEventId(), evt.getEventType(), savedUsuario.getId());
        socioKafkaTemplate.send("usuarios.socio.created", savedUsuario.getId().toString(), evt);

        return savedUsuario;
    }

    @Override
    public Usuario createAdminAndReturnEntity(CreateAdminDto dto) {
        String emailNormalized = dto.getEmail() != null ? dto.getEmail().trim().toLowerCase() : null;
        if (emailNormalized == null || emailNormalized.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Constants.EMAIL_REGISTERED);
        }
        if (usuarioRepository.existsByEmail(emailNormalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Constants.EMAIL_REGISTERED);
        }
        logger.debug("CreateAdminDto: {}", dto);
        Usuario usuario = mapper.toEntity(dto);

        usuario.setEmail(emailNormalized);
        usuario.setRole("ADMIN");

        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password requerida");
        }
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));

        usuario.setCreadoEn(OffsetDateTime.now());

        // ADMIN no depende de membresía -> EXENTO
        usuario.setEstado("EXENTO");

        logger.debug("Usuario final (antes de save): {}", usuario);
        Usuario savedUsuario = usuarioRepository.save(usuario);

        AdminPayload payload = new AdminPayload(
                savedUsuario.getId(),
                savedUsuario.getEmail(),
                savedUsuario.getNombre(),
                savedUsuario.getApellidos(),
                savedUsuario.getTelefono()
        );
        AdminEvent evt = AdminEvent.of("ADMIN_CREATED", payload);

        logger.debug("Publicando evento ADMIN_CREATED eventId={} for usuarioId={}", evt.getEventId(), savedUsuario.getId());
        adminKafkaTemplate.send("usuarios.admin.created", savedUsuario.getId().toString(), evt);

        return savedUsuario;
    }

    @Override
    public Optional<Usuario> findEntityById(Long id) {
        return usuarioRepository.findById(id);
    }

    @Override
    @Transactional
    public void updateAdmin(Usuario usuario, UpdateAdminDto dto) {
        if (dto.getNombre() != null && !dto.getNombre().isBlank()) {
            usuario.setNombre(dto.getNombre().trim());
        }
        if (dto.getApellidos() != null && !dto.getApellidos().isBlank()) {
            usuario.setApellidos(dto.getApellidos().trim());
        }
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            String email = dto.getEmail().trim().toLowerCase();
            if (usuarioRepository.existsByEmailAndIdNot(email, usuario.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Constants.EMAIL_REGISTERED);
            }
            usuario.setEmail(email);
        }
        if (dto.getTelefono() != null && !dto.getTelefono().isBlank()) {
            usuario.setTelefono(dto.getTelefono().trim());
        }
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        usuario.setEstado("EXENTO");

        usuarioRepository.save(usuario);

        AdminPayload payload = new AdminPayload(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getNombre(),
                usuario.getApellidos(),
                usuario.getTelefono()
        );
        AdminEvent evt = AdminEvent.of("ADMIN_UPDATED", payload);

        logger.debug("Publicando evento ADMIN_UPDATED eventId={} for usuarioId={}", evt.getEventId(), usuario.getId());
        adminKafkaTemplate.send("usuarios.admin.updated", usuario.getId().toString(), evt);
    }

    @Override
    @Transactional
    public void updateSocio(Usuario usuario, UpdateSocioDto dto) {
        if (dto.getNombre() != null && !dto.getNombre().isBlank()) {
            usuario.setNombre(dto.getNombre().trim());
        }
        if (dto.getApellidos() != null && !dto.getApellidos().isBlank()) {
            usuario.setApellidos(dto.getApellidos().trim());
        }
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            String email = dto.getEmail().trim().toLowerCase();
            if (usuarioRepository.existsByEmailAndIdNot(email, usuario.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Constants.EMAIL_REGISTERED);
            }
            usuario.setEmail(email);
        }
        if (dto.getTelefono() != null && !dto.getTelefono().isBlank()) {
            usuario.setTelefono(dto.getTelefono().trim());
        }
        if (dto.getTipoMembresia() != null && !dto.getTipoMembresia().isBlank()) {
            usuario.setTipoMembresia(dto.getTipoMembresia().trim());
        }
        if (dto.getInicioMembresia() != null) {
            usuario.setInicioMembresia(dto.getInicioMembresia());
        }
        if (dto.getFinMembresia() != null) {
            usuario.setFinMembresia(dto.getFinMembresia());
        }

        // Recalcular estado en base a las fechas/rol 
        recalcularEstado(usuario);

        usuarioRepository.save(usuario);

        SocioPayload payload = new SocioPayload(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getNombre(),
                usuario.getApellidos(),
                usuario.getTipoMembresia(),
                usuario.getFinMembresia()
        );
        SocioEvent evt = SocioEvent.of("SOCIO_UPDATED", payload);

        logger.debug("Publicando evento SOCIO_UPDATED eventId={} for usuarioId={}", evt.getEventId(), usuario.getId());
        socioKafkaTemplate.send("usuarios.socio.updated", usuario.getId().toString(), evt);
    }

    @Override
    public void deleteUsuario(Long id) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }
        Usuario usuario = usuarioOpt.get();
        usuarioRepository.deleteById(id);

        if ("SOCIO".equalsIgnoreCase(usuario.getRole())) {
            SocioPayload payload = new SocioPayload(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getNombre(),
                usuario.getApellidos(),
                usuario.getTipoMembresia(),
                usuario.getFinMembresia()
            );
            SocioEvent evt = SocioEvent.of("SOCIO_DELETED", payload);
            logger.debug("Publicando evento SOCIO_DELETED eventId={} for usuarioId={}", evt.getEventId(), usuario.getId());
            socioKafkaTemplate.send("usuarios.socio.deleted", usuario.getId().toString(), evt);
        } else if ("ADMIN".equalsIgnoreCase(usuario.getRole())) {
            AdminPayload payload = new AdminPayload(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getNombre(),
                usuario.getApellidos(),
                usuario.getTelefono()
            );
            AdminEvent evt = AdminEvent.of("ADMIN_DELETED", payload);
            logger.debug("Publicando evento ADMIN_DELETED eventId={} for usuarioId={}", evt.getEventId(), usuario.getId());
            adminKafkaTemplate.send("usuarios.admin.deleted", usuario.getId().toString(), evt);
        }
    }

    @Override
    public List<UsuariosDto> getAdministradores() {
        return usuarioRepository.findAllByRoleIgnoreCase("ADMIN").stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public List<UsuariosDto> getSocios() {
        return usuarioRepository.findAllByRoleIgnoreCase("SOCIO").stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public List<UsuariosDto> buscarPorRole(String role, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<Usuario> pageRes = usuarioRepository.findAllByRoleIgnoreCase(role, pageable);
        return pageRes.getContent().stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public boolean checkPassword(Usuario usuario, String rawPassword) {
        return passwordEncoder.matches(rawPassword, usuario.getPassword());
    }

    @Override
    public Usuario findEntityByEmail(String email) {
        return usuarioRepository.findByEmail(email).orElse(null);
    }

    // ------------------------------
    private void recalcularEstado(Usuario usuario) {
        if (usuario == null) return;

        String role = usuario.getRole();
        LocalDate hoy = LocalDate.now();

        if ("ADMIN".equalsIgnoreCase(role)) {
            usuario.setEstado("EXENTO");
            return;
        }

        
        if ("SOCIO".equalsIgnoreCase(role)) {
            LocalDate inicio = usuario.getInicioMembresia();
            LocalDate fin = usuario.getFinMembresia();

            if (inicio != null && fin != null) {
                if (( !hoy.isBefore(inicio) ) && ( !hoy.isAfter(fin) )) {
                    usuario.setEstado("ACTIVO");
                    return;
                } else {
                    usuario.setEstado("INACTIVO");
                    return;
                }
            } else {
                usuario.setEstado("INACTIVO");
                return;
            }
        }

        // Fallback por seguridad
        usuario.setEstado("INACTIVO");
    }
}
