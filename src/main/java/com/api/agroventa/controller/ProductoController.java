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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.api.agroventa.dto.CustomResponse;
import com.api.agroventa.dto.response.ProductoResponse;
import com.api.agroventa.service.ProductoService;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin
public class ProductoController {
    private final ProductoService service;

    public ProductoController(ProductoService service) {
        this.service = service;
    }

    @GetMapping("/list")
    public ResponseEntity<CustomResponse<Map<String, Object>>> listar() {
        List<ProductoResponse> lista = service.listar();
        Map<String, Object> data = new HashMap<>();
        data.put("listProductos", lista);

        return ResponseEntity.ok(CustomResponse.<Map<String, Object>>builder()
                .type("success")
                .message("Productos listados correctamente")
                .data(data)
                .build());
    }

    @PostMapping(value = "/insert", consumes = { "multipart/form-data" })
    public ResponseEntity<CustomResponse<Object>> insertarProducto(
            @RequestParam("nombre") String nombre,
            @RequestParam("descripcion") String descripcion,
            @RequestParam("precio") Double precio,
            @RequestParam("stock") Integer stock,
            @RequestParam("categoria") String categoria,
            @RequestParam("imagen") MultipartFile imagen) {

        try {
            service.subirYGuardarProducto(nombre, descripcion, precio, stock, categoria, imagen);

            return ResponseEntity.ok(CustomResponse.builder()
                    .type("success")
                    .message("Producto registrado correctamente")
                    .data(null)
                    .build());

        } catch (Exception e) {
            return ResponseEntity.status(500).body(CustomResponse.builder()
                    .type("error")
                    .message("Error al registrar el producto: " + e.getMessage())
                    .data(null)
                    .build());
        }
    }

    @PutMapping(value = "/update/{id}", consumes = { "multipart/form-data" })
    public ResponseEntity<CustomResponse<Object>> actualizarProducto(
            @PathVariable Integer id,
            @RequestParam("nombre") String nombre,
            @RequestParam("descripcion") String descripcion,
            @RequestParam("precio") Double precio,
            @RequestParam("stock") Integer stock,
            @RequestParam("categoria") String categoria,
            @RequestParam(value = "imagen", required = false) MultipartFile imagen) {

        try {
            boolean actualizado = service.actualizarProducto(id, nombre, descripcion, precio, stock, categoria, imagen);

            if (actualizado) {
                return ResponseEntity.ok(CustomResponse.builder()
                        .type("success")
                        .message("Producto actualizado correctamente")
                        .data(null)
                        .build());
            } else {
                return ResponseEntity.status(404).body(CustomResponse.builder()
                        .type("error")
                        .message("Producto no encontrado")
                        .data(null)
                        .build());
            }

        } catch (Exception e) {
            return ResponseEntity.status(500).body(CustomResponse.builder()
                    .type("error")
                    .message("Error al actualizar producto: " + e.getMessage())
                    .data(null)
                    .build());
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<CustomResponse<Object>> eliminar(@PathVariable Integer id) {
        boolean eliminado = service.eliminar(id);

        if (eliminado) {
            return ResponseEntity.ok(CustomResponse.builder()
                    .type("success")
                    .message("Producto eliminado correctamente")
                    .data(null)
                    .build());
        } else {
            return ResponseEntity.status(404).body(CustomResponse.builder()
                    .type("error")
                    .message("Producto no encontrado")
                    .data(null)
                    .build());
        }
    }

    @GetMapping("/listby/category/{categoria}")
    public ResponseEntity<CustomResponse<Map<String, Object>>> listarPorCategoria(
            @PathVariable("categoria") String categoria) {

        List<ProductoResponse> lista = service.listarPorCategoria(categoria);
        Map<String, Object> data = new HashMap<>();
        data.put("listProductos", lista);

        return ResponseEntity.ok(CustomResponse.<Map<String, Object>>builder()
                .type("success")
                .message("Productos filtrados por categoría correctamente")
                .data(data)
                .build());
    }
}
