package com.api.agroventa.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.api.agroventa.dto.CustomResponse;
import com.api.agroventa.dto.request.ActualizarEstadoPedidoRequest;
import com.api.agroventa.dto.request.PedidoRequest;
import com.api.agroventa.dto.request.PedidoRequestYapeConFile;
import com.api.agroventa.dto.response.DetalleCompraResponse;
import com.api.agroventa.dto.response.HistorialPedidoResponse;
import com.api.agroventa.model.Pedido;
import com.api.agroventa.service.PedidoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin
public class PedidoController {

    private final PedidoService service;

    public PedidoController(PedidoService service) {
        this.service = service;
    }

    @PostMapping("/realizar")
    public ResponseEntity<CustomResponse<DetalleCompraResponse>> realizarPedido(@RequestBody PedidoRequest request) {
        try {
            DetalleCompraResponse recibo = service.realizarPedido(request);
            return ResponseEntity.ok(CustomResponse.<DetalleCompraResponse>builder()
                    .type("success")
                    .message("Pedido realizado con éxito")
                    .data(recibo)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(400).body(CustomResponse.<DetalleCompraResponse>builder()
                    .type("error")
                    .message("Error al realizar pedido: " + e.getMessage())
                    .data(null)
                    .build());
        }
    }

    @GetMapping("/historial/{usuarioId}")
    public ResponseEntity<CustomResponse<Map<String, Object>>> historial(@PathVariable Integer usuarioId) {
        List<HistorialPedidoResponse> historial = service.obtenerHistorialPorUsuario(usuarioId);

        Map<String, Object> data = new HashMap<>();
        data.put("pedidos", historial);

        return ResponseEntity.ok(CustomResponse.<Map<String, Object>>builder()
                .type("success")
                .message("Historial de pedidos")
                .data(data)
                .build());
    }

    @PostMapping(path = "/pagoyape", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CustomResponse<Object>> procesarPagoConComprobante(
            @ModelAttribute PedidoRequestYapeConFile request) {
        try {
            var response = service.procesarPagoConComprobante(
                    request.getUsuarioId(),
                    request.getDireccionEntrega(),
                    request.getMetodoPago(),
                    request.getComprobante() // puede ser null si es pago en efectivo
            );

            return ResponseEntity.ok(CustomResponse.builder()
                    .type("success")
                    .message("Pedido realizado con éxito")
                    .data(response)
                    .build());

        } catch (Exception e) {
            return ResponseEntity.status(400).body(CustomResponse.builder()
                    .type("error")
                    .message("Error al realizar pedido: " + e.getMessage())
                    .data(null)
                    .build());
        }
    }

    @PutMapping("/admin/estado")
    public ResponseEntity<CustomResponse<Object>> actualizarEstadoPedido(
            @Valid @RequestBody ActualizarEstadoPedidoRequest request) {
        try {
            service.actualizarEstadoPedido(request.getPedidoId(), request.getNuevoEstado());

            return ResponseEntity.ok(CustomResponse.builder()
                    .type("success")
                    .message("Estado del pedido actualizado correctamente")
                    .data(null)
                    .build());

        } catch (Exception e) {
            return ResponseEntity.status(400).body(CustomResponse.builder()
                    .type("error")
                    .message("Error al actualizar estado: " + e.getMessage())
                    .data(null)
                    .build());
        }
    }

    @GetMapping("/admin/estado")
    public ResponseEntity<CustomResponse<Map<String, Object>>> obtenerPedidosPorEstado(
            @RequestParam("estado") String estado) {
        try {
            Pedido.EstadoPedido estadoPedido = Pedido.EstadoPedido.valueOf(estado.toUpperCase());
            List<HistorialPedidoResponse> pedidos = service.obtenerPedidosPorEstado(estadoPedido);

            Map<String, Object> data = new HashMap<>();
            data.put("pedidos", pedidos);

            return ResponseEntity.ok(CustomResponse.<Map<String, Object>>builder()
                    .type("success")
                    .message("Pedidos con estado: " + estadoPedido.name())
                    .data(data)
                    .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(CustomResponse.<Map<String, Object>>builder()
                    .type("error")
                    .message("Estado inválido. Usa: PENDIENTE, COMPLETADO o CANCELADO.")
                    .data(null)
                    .build());
        }
    }
}
