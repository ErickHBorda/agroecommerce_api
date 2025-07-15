package com.api.agroventa.dto.request;

import lombok.Data;

@Data
public class UsuarioRequest {
    private String nombre;
    private String email;
    private String password;
    private String telefono;
    private String direccion;
}
