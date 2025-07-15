package com.api.agroventa.dto.request;

import lombok.Data;

@Data
public class PedidoRequest {
    private Integer usuarioId;
    private String direccionEntrega;
    private String metodoPago; // "EFECTIVO_CONTRA_ENTREGA" o "YAPE"
}
