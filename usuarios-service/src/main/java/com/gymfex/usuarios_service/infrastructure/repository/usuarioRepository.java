package com.gymfex.usuarios_service.infrastructure.repository;

import com.gymfex.usuarios_service.domain.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;

public interface usuarioRepository extends JpaRepository<Usuario, Long> {
    
    // Búsqueda insensible a mayúsculas/minúsculas con paginación
    @Query("SELECT u FROM Usuario u WHERE LOWER(u.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    Page<Usuario> buscarPorNombre(@Param("nombre") String nombre, Pageable pageable);
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Usuario> findAllByRoleIgnoreCase(String role);
    // paginado
    Page<Usuario> findAllByRoleIgnoreCase(String role, Pageable pageable);
    boolean existsByEmailAndIdNot(String email, Long id);

}
