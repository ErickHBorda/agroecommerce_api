package com.api.agroventa.dto.request;

import com.api.agroventa.model.Pedido.EstadoPedido;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ActualizarEstadoPedidoRequest {

    @NotNull
    private Integer pedidoId;

    @NotNull
    private EstadoPedido nuevoEstado;
}