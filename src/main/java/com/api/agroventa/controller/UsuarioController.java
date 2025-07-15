package com.api.agroventa.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.agroventa.dto.CustomResponse;
import com.api.agroventa.dto.request.LoginRequest;
import com.api.agroventa.dto.request.UsuarioRequest;
import com.api.agroventa.dto.response.LoginResponse;
import com.api.agroventa.dto.response.UsuarioResponse;
import com.api.agroventa.service.UsuarioService;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin
public class UsuarioController {
    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping("/list")
    public ResponseEntity<CustomResponse<Map<String, Object>>> listar() {
        List<UsuarioResponse> lista = service.listarUsuarios();
        Map<String, Object> data = new HashMap<>();
        data.put("listUsuarios", lista);

        CustomResponse<Map<String, Object>> response = CustomResponse.<Map<String, Object>>builder()
                .type("success")
                .message("Usuarios encontrados correctamente")
                .data(data)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/insert")
    public ResponseEntity<CustomResponse<Object>> registrar(@RequestBody UsuarioRequest request) {
        try {
            service.guardar(request);
            return ResponseEntity.ok(CustomResponse.builder()
                    .type("success")
                    .message("Usuario registrado correctamente")
                    .data(null)
                    .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(CustomResponse.builder()
                    .type("error")
                    .message(e.getMessage())
                    .data(null)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(CustomResponse.builder()
                    .type("error")
                    .message("Error inesperado: " + e.getMessage())
                    .data(null)
                    .build());
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<CustomResponse<Object>> actualizar(@PathVariable Integer id,
            @RequestBody UsuarioRequest request) {
        boolean actualizado = service.actualizarUsuario(id, request);

        if (actualizado) {
            return ResponseEntity.ok(CustomResponse.builder()
                    .type("success")
                    .message("Usuario actualizado correctamente")
                    .data(null)
                    .build());
        } else {
            return ResponseEntity.status(404).body(CustomResponse.builder()
                    .type("error")
                    .message("Usuario no encontrado")
                    .data(null)
                    .build());
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<CustomResponse<Object>> eliminarUsuario(@PathVariable Integer id) {
        boolean eliminado = service.eliminarUsuario(id);

        if (eliminado) {
            return ResponseEntity.ok(CustomResponse.builder()
                    .type("success")
                    .message("Usuario eliminado correctamente")
                    .data(null)
                    .build());
        } else {
            return ResponseEntity.status(404).body(CustomResponse.builder()
                    .type("error")
                    .message("Usuario no encontrado")
                    .data(null)
                    .build());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<CustomResponse<Map<String, Object>>> login(@RequestBody LoginRequest request) {
        Optional<LoginResponse> login = service.login(request);

        if (login.isPresent()) {
            Map<String, Object> data = new HashMap<>();
            data.put("user", login.get());

            return ResponseEntity.ok(CustomResponse.<Map<String, Object>>builder()
                    .type("success")
                    .message("Login correcto")
                    .data(data)
                    .build());
        } else {
            return ResponseEntity.badRequest().body(CustomResponse.<Map<String, Object>>builder()
                    .type("error")
                    .message("Correo o contraseña incorrecta")
                    .data(null)
                    .build());
        }
    }
}
