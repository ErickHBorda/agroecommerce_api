package com.api.agroventa.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DetalleCompraResponse {

    private Integer pedidoId;
    private String fecha;
    private String metodoPago;
    private String direccionEntrega;
    private String estado;

    private List<ItemCompra> items;
    private Double total;

    @Data
    @Builder
    public static class ItemCompra {
        private String nombre;
        private Integer cantidad;
        private Double precioUnitario;
        private Double subtotal;
    }
}

