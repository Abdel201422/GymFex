package com.gymfex.usuarios_service.application.mapper;

import org.mapstruct.*;
import com.gymfex.usuarios_service.domain.Usuario;
import com.gymfex.usuarios_service.application.dto.request.UpdateAdminDto;
import com.gymfex.usuarios_service.application.dto.request.UpdateSocioDto;
import com.gymfex.usuarios_service.application.dto.response.UsuarioDetailDto;
import com.gymfex.usuarios_service.application.dto.response.UsuariosDto;
import com.gymfex.usuarios_service.application.dto.request.CreateAdminDto;
import com.gymfex.usuarios_service.application.dto.request.CreateSocioDto;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    UsuariosDto toDto(Usuario usuario);

    UsuarioDetailDto toDetailDto(Usuario usuario);

    Usuario toEntity(CreateSocioDto dto);

    Usuario toEntity(CreateAdminDto dto);

    void updateAdminFromDto(UpdateAdminDto dto, @MappingTarget Usuario usuario);

    void updateSocioFromDto(UpdateSocioDto dto, @MappingTarget Usuario usuario);
}