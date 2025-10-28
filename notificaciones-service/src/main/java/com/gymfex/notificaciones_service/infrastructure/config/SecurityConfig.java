package com.gymfex.notificaciones_service.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(
                    "/v3/api-docs/**",  
                    "/swagger-ui/**",    
                    "/swagger-ui.html"   
                ).permitAll()         
                .anyRequest().authenticated() 
            )
            
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                )
            );

        return http.build();
    }
}
