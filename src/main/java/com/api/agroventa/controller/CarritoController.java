package com.api.agroventa.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.agroventa.dto.CustomResponse;
import com.api.agroventa.dto.request.CarritoRequest;
import com.api.agroventa.dto.response.CarritoResponse;
import com.api.agroventa.service.CarritoService;

@RestController
@RequestMapping("/api/carrito")
@CrossOrigin
public class CarritoController {

    private final CarritoService service;

    public CarritoController(CarritoService service) {
        this.service = service;
    }

    @GetMapping("/{usuarioId}")
    public ResponseEntity<CustomResponse<Map<String, Object>>> listar(@PathVariable Integer usuarioId) {
        List<CarritoResponse> lista = service.listarCarritoPorUsuario(usuarioId);
        Map<String, Object> data = new HashMap<>();
        data.put("items", lista);
        return ResponseEntity.ok(CustomResponse.<Map<String, Object>>builder()
                .type("success")
                .message("Carrito listado")
                .data(data)
                .build());
    }

    @PostMapping("/add")
    public ResponseEntity<CustomResponse<Object>> agregar(@RequestBody CarritoRequest request) {
        service.agregarProducto(request);
        return ResponseEntity.ok(CustomResponse.builder()
                .type("success")
                .message("Producto agregado al carrito")
                .data(null)
                .build());
    }

    @DeleteMapping("/delete/{usuarioId}/{productoId}")
    public ResponseEntity<CustomResponse<Object>> eliminar(@PathVariable Integer usuarioId, @PathVariable Integer productoId) {
        boolean eliminado = service.eliminarProducto(usuarioId, productoId);
        if (eliminado) {
            return ResponseEntity.ok(CustomResponse.builder()
                    .type("success")
                    .message("Producto eliminado del carrito")
                    .data(null)
                    .build());
        } else {
            return ResponseEntity.status(404).body(CustomResponse.builder()
                    .type("error")
                    .message("Producto no encontrado en el carrito")
                    .data(null)
                    .build());
        }
    }

    @DeleteMapping("/clear/{usuarioId}")
    public ResponseEntity<CustomResponse<Object>> vaciar(@PathVariable Integer usuarioId) {
        service.vaciarCarrito(usuarioId);
        return ResponseEntity.ok(CustomResponse.builder()
                .type("success")
                .message("Carrito vaciado")
                .data(null)
                .build());
    }
}
