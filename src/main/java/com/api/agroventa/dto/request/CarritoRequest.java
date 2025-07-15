package com.api.agroventa.dto.request;

import lombok.Data;

@Data
public class CarritoRequest {
    private Integer usuarioId;
    private Integer productoId;
    private Integer cantidad;
}
