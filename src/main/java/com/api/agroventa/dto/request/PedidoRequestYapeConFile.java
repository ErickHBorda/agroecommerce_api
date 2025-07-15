package com.api.agroventa.dto.request;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class PedidoRequestYapeConFile {
    private Integer usuarioId;
    private String direccionEntrega;
    private String metodoPago; 
    private MultipartFile comprobante; // Solo para YAPE
}