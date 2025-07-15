package com.api.agroventa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.api.agroventa.model.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {
    List<Pedido> findByEstado(Pedido.EstadoPedido estado);
}