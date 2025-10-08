package com.gymfex.usuarios_service.application.service;

import com.gymfex.usuarios_service.application.dto.response.UsuariosDto;
import com.gymfex.usuarios_service.domain.Usuario;

import java.util.List;
import java.util.Optional;

import com.gymfex.usuarios_service.application.dto.request.CreateSocioDto;  
import com.gymfex.usuarios_service.application.dto.request.CreateAdminDto;
import com.gymfex.usuarios_service.application.dto.request.UpdateAdminDto;
import com.gymfex.usuarios_service.application.dto.request.UpdateSocioDto;


public interface usuarioService {
    List<UsuariosDto> getUsuarios();
    Optional<UsuariosDto> findById(Long id);
    List<UsuariosDto> buscarPorNombre(String nombre, int page, int size);
    Usuario createSocioAndReturnEntity(CreateSocioDto dto);
    Usuario createAdminAndReturnEntity(CreateAdminDto dto);
    Optional<Usuario> findEntityById(Long id);
    void updateAdmin(Usuario usuario, UpdateAdminDto dto);
    void updateSocio(Usuario usuario, UpdateSocioDto dto);
    void deleteUsuario(Long id);
    List<UsuariosDto> getAdministradores();
    List<UsuariosDto> getSocios();
    List<UsuariosDto> buscarPorRole(String role, int page, int size);
    Usuario findEntityByEmail(String email);
    boolean checkPassword(Usuario usuario, String rawPassword);
   
}