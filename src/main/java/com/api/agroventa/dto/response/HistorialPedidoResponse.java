package com.api.agroventa.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HistorialPedidoResponse {

    private Integer pedidoId;
    private String fecha;
    private String estado;
    private String metodoPago;
    private String direccionEntrega;
    private List<ItemDetalle> productos;
    private Double total;
    private String nombreUsuario;
    private Integer usuarioId;
    private String emailUsuario; // opcional

    @Data
    @Builder
    public static class ItemDetalle {
        private String nombre;
        private Integer cantidad;
        private Double precioUnitario;
        private Double subtotal;
    }
}
