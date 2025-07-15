package com.api.agroventa.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.agroventa.model.Carrito;

public interface CarritoRepository extends JpaRepository<Carrito, Integer> {
    List<Carrito> findByUsuarioId(Integer usuarioId);

    void deleteByUsuarioId(Integer usuarioId);

    Optional<Carrito> findByUsuarioIdAndProductoId(Integer usuarioId, Integer productoId);
    boolean existsByUsuarioIdAndProductoId(Integer usuarioId, Integer productoId);
    void deleteByUsuarioIdAndProductoId(Integer usuarioId, Integer productoId);
}