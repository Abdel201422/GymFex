package com.gymfex.usuarios_service.application.mapper;

import org.mapstruct.*;
import com.gymfex.usuarios_service.domain.Usuario;
import com.gymfex.usuarios_service.application.dto.request.UpdateAdminDto;
import com.gymfex.usuarios_service.application.dto.response.UsuarioDetailDto;
import com.gymfex.usuarios_service.application.dto.response.UsuariosDto;
import com.gymfex.usuarios_service.application.dto.request.CreateAdminDto;
import com.gymfex.usuarios_service.application.dto.request.CreateSocioDto;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    UsuariosDto toDto(Usuario usuario);

    //UsuarioDetailDto toDetail(Usuario usuario);

    Usuario toEntity(CreateSocioDto req_SocioDto);
    Usuario toEntity(CreateAdminDto req_AdminDto);

}