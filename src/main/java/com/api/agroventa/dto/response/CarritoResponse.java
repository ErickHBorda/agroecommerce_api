package com.api.agroventa.dto.response;

import lombok.Data;

@Data
public class CarritoResponse {
    private Integer id;
    private Integer cantidad;

    private Integer productoId;
    private String nombre;
    private String imagenUrl;
    private Double precio;

    private Double subtotal;
}