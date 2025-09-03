package com.gymfex.usuarios_service.infrastructure.security;

import com.gymfex.usuarios_service.domain.Usuario;
import com.gymfex.usuarios_service.infrastructure.repository.usuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final usuarioRepository usuarioRepository;

    public CustomUserDetailsService(usuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario u = usuarioRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        return new User(u.getEmail(), u.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + u.getRole().toUpperCase())));
    }
}
