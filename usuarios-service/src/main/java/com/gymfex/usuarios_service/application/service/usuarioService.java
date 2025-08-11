package com.gymfex.usuarios_service.application.service;

import com.gymfex.usuarios_service.application.dto.response.UsuariosDto;
import com.gymfex.usuarios_service.domain.Usuario;

import java.util.List;
import java.util.Optional;

import com.gymfex.usuarios_service.application.dto.request.CreateSocioDto;  
import com.gymfex.usuarios_service.application.dto.request.CreateAdminDto;
import com.gymfex.usuarios_service.application.dto.request.UsuarioUpdateDto;

public interface usuarioService {
    List<UsuariosDto> getUsuarios();
    Optional<UsuariosDto> findById(Long id);
    List<UsuariosDto> buscarPorNombre(String nombre, int page, int size);
    void createSocio(CreateSocioDto dto);
    void createAdmin(CreateAdminDto dto);
    Optional<Usuario> findEntityById(Long id);
    void updateAdmin(Usuario usuario, UsuarioUpdateDto dto);
    void updateSocio(Usuario usuario, UsuarioUpdateDto dto);
    void deleteUsuario(Long id);

}