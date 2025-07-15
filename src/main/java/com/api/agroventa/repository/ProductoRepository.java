package com.api.agroventa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.agroventa.model.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    List<Producto> findByCategoria(Producto.Categoria categoria);
}
