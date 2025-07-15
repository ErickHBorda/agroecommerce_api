package com.api.agroventa.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.agroventa.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    boolean existsByEmail(String email);
    Optional<Usuario> findByEmail(String email);
}
