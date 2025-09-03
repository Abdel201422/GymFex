package com.gymfex.usuarios_service.infrastructure.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.gymfex.usuarios_service.infrastructure.security.CustomUserDetailsService;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
protected void doFilterInternal(HttpServletRequest request,
                                HttpServletResponse response,
                                FilterChain filterChain) throws ServletException, IOException {

    logger.debug("[JWT FILTER] Entrando al filtro para URI: " + request.getRequestURI()); // <-- agregado

    final String authHeader = request.getHeader("Authorization");

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        logger.debug("[JWT FILTER] No Authorization header o formato incorrecto");
        filterChain.doFilter(request, response);
        return;
    }

    String token = authHeader.substring(7);
    logger.debug("[JWT FILTER] Token recibido: " + token);

    if (!jwtService.isTokenValid(token)) {
        logger.debug("[JWT FILTER] Token inválido o expirado");
        filterChain.doFilter(request, response);
        return;
    }

    String username = jwtService.extractUsername(token);
    logger.debug("[JWT FILTER] Username extraído: " + username);

    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        logger.debug("[JWT FILTER] UserDetails cargado: " + userDetails.getUsername());

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
        logger.debug("[JWT FILTER] Autenticación establecida");
    }

    filterChain.doFilter(request, response);
}

}
