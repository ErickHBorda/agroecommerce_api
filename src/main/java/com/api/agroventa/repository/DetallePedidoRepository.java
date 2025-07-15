package com.api.agroventa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.agroventa.model.DetallePedido;

public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Integer> {
    List<DetallePedido> findByPedidoId(Integer pedidoId);
}
