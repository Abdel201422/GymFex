package com.gymfex.usuarios_service.application.service;

import com.gymfex.usuarios_service.application.dto.response.UsuariosDto;
import com.gymfex.usuarios_service.domain.Usuario;
import com.gymfex.usuarios_service.infrastructure.repository.usuarioRepository;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.gymfex.usuarios_service.application.mapper.UsuarioMapper;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import com.gymfex.usuarios_service.application.dto.request.CreateSocioDto;
import com.gymfex.usuarios_service.application.dto.request.UsuarioUpdateDto;
import com.gymfex.usuarios_service.application.dto.request.CreateAdminDto;



import java.util.List;
import java.util.Optional;

@Service
public class usuarioServiceImpl implements usuarioService {

    private final usuarioRepository usuarioRepository;
    private final UsuarioMapper mapper;

    public usuarioServiceImpl(usuarioRepository usuarioRepository, UsuarioMapper mapper) {
        this.usuarioRepository = usuarioRepository;
        this.mapper = mapper;
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
        // Crea objeto de paginación
        PageRequest pageable = PageRequest.of(page, size);
        
        Page<Usuario> usuariosPage;
        
        if (nombre == null || nombre.isBlank()) {
            // Si no hay nombre, obtener todos paginados
            usuariosPage = usuarioRepository.findAll(pageable);
        } else {
            // Búsqueda con filtro
            usuariosPage = usuarioRepository.buscarPorNombre(nombre, pageable);
        }
        
        return usuariosPage.getContent()
                        .stream()
                        .map(mapper::toDto)
                        .toList();
    }
  
    @Override
    public void createSocio(CreateSocioDto dto) {
        Usuario usuario = mapper.toEntity(dto);
        usuario.setRole("socio"); // Asignar rol por defecto
        usuarioRepository.save(usuario);
    }

    @Override
    public void createAdmin(CreateAdminDto dto) {
        Usuario usuario = mapper.toEntity(dto);
        usuario.setRole("admin"); // Asignar rol de administrador
        usuarioRepository.save(usuario);
    }

    @Override 
    public Optional<Usuario> findEntityById(Long id) {
        return usuarioRepository.findById(id);
    }

    @Override
    public void updateAdmin(Usuario usuario, UsuarioUpdateDto dto) {
        // Actualizar campos específicos del administrador
        usuario.setNombre(dto.getNombre());
        usuario.setApellidos(dto.getApellidos());
        usuario.setEmail(dto.getEmail());
        usuario.setTelefono(dto.getTelefono());
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            usuario.setPassword(dto.getPassword());
        }
        usuarioRepository.save(usuario);
    }

    @Override
    public void updateSocio(Usuario usuario, UsuarioUpdateDto dto) {
        // Actualizar campos específicos del socio
        usuario.setNombre(dto.getNombre());
        usuario.setApellidos(dto.getApellidos());
        usuario.setEmail(dto.getEmail());
        usuario.setTelefono(dto.getTelefono());
        usuario.setTipoMembresia(dto.getTipoMembresia());
        usuario.setInicioMembresia(dto.getInicioMembresia());
        usuario.setFinMembresia(dto.getFinMembresia());
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
}