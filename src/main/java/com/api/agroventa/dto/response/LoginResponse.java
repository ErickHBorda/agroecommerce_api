package com.api.agroventa.dto.response;

import lombok.Data;

@Data
public class LoginResponse {
    private Integer id;
    private String nombre;
    private String email;
    private String telefono;
    private String direccion;
}