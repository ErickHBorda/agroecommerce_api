package com.api.agroventa.dto.response;

import lombok.Data;

@Data
public class ProductoResponse {
    private Integer id;
    private String nombre;
    private String descripcion;
    private Double precio;
    private Integer stock;
    private String categoria;
    private String imagenUrl;
}
