package com.api.agroventa.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.api.agroventa.dto.request.PedidoRequest;
import com.api.agroventa.dto.response.DetalleCompraResponse;
import com.api.agroventa.dto.response.HistorialPedidoResponse;
import com.api.agroventa.model.Carrito;
import com.api.agroventa.model.DetallePedido;
import com.api.agroventa.model.Pedido;
import com.api.agroventa.model.Producto;
import com.api.agroventa.model.Usuario;
import com.api.agroventa.repository.CarritoRepository;
import com.api.agroventa.repository.DetallePedidoRepository;
import com.api.agroventa.repository.PedidoRepository;
import com.api.agroventa.repository.ProductoRepository;
import com.api.agroventa.repository.UsuarioRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import jakarta.transaction.Transactional;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepo;
    private final DetallePedidoRepository detalleRepo;
    private final CarritoRepository carritoRepo;
    private final ProductoRepository productoRepo;
    private final UsuarioRepository usuarioRepo;
    private final Cloudinary cloudinary;

    public PedidoService(PedidoRepository pedidoRepo, DetallePedidoRepository detalleRepo,
            CarritoRepository carritoRepo, ProductoRepository productoRepo,
            UsuarioRepository usuarioRepo, Cloudinary cloudinary) {
        this.pedidoRepo = pedidoRepo;
        this.detalleRepo = detalleRepo;
        this.carritoRepo = carritoRepo;
        this.productoRepo = productoRepo;
        this.usuarioRepo = usuarioRepo;
        this.cloudinary = cloudinary;
    }

    @Transactional
    public DetalleCompraResponse realizarPedido(PedidoRequest request) {
        Usuario usuario = usuarioRepo.findById(request.getUsuarioId()).orElseThrow();
        List<Carrito> carritoItems = carritoRepo.findByUsuarioId(usuario.getId());

        if (carritoItems.isEmpty()) {
            throw new IllegalStateException("El carrito está vacío");
        }

        // Crear pedido
        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setDireccionEntrega(request.getDireccionEntrega());
        pedido.setMetodoPago(Pedido.MetodoPago.valueOf(request.getMetodoPago()));
        pedidoRepo.save(pedido);

        double total = 0;
        List<DetalleCompraResponse.ItemCompra> detalles = new ArrayList<>();

        for (Carrito item : carritoItems) {
            Producto prod = item.getProducto();

            if (prod.getStock() < item.getCantidad()) {
                throw new IllegalStateException("Stock insuficiente para: " + prod.getNombre());
            }

            // Guardar detalle
            DetallePedido detalle = new DetallePedido();
            detalle.setPedido(pedido);
            detalle.setProducto(prod);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(prod.getPrecio());
            detalle.setSubtotal(prod.getPrecio() * item.getCantidad());
            detalleRepo.save(detalle);

            // Descontar stock
            prod.setStock(prod.getStock() - item.getCantidad());
            productoRepo.save(prod);

            // Para el recibo
            detalles.add(DetalleCompraResponse.ItemCompra.builder()
                    .nombre(prod.getNombre())
                    .cantidad(item.getCantidad())
                    .precioUnitario(prod.getPrecio())
                    .subtotal(detalle.getSubtotal())
                    .build());

            total += detalle.getSubtotal();
        }

        carritoRepo.deleteByUsuarioId(usuario.getId());

        return DetalleCompraResponse.builder()
                .pedidoId(pedido.getId())
                .fecha(pedido.getCreadoEn().toString())
                .direccionEntrega(pedido.getDireccionEntrega())
                .metodoPago(pedido.getMetodoPago().name())
                .estado(pedido.getEstado().name())
                .items(detalles)
                .total(total)
                .build();
    }

    public List<HistorialPedidoResponse> obtenerHistorialPorUsuario(Integer usuarioId) {
        List<Pedido> pedidos = pedidoRepo.findAll().stream()
                .filter(p -> p.getUsuario().getId().equals(usuarioId))
                .collect(Collectors.toList());

        return pedidos.stream().map(pedido -> {
            List<DetallePedido> detalles = detalleRepo.findAll().stream()
                    .filter(d -> d.getPedido().getId().equals(pedido.getId()))
                    .toList();

            List<HistorialPedidoResponse.ItemDetalle> items = detalles.stream()
                    .map(d -> HistorialPedidoResponse.ItemDetalle.builder()
                            .nombre(d.getProducto().getNombre())
                            .cantidad(d.getCantidad())
                            .precioUnitario(d.getPrecioUnitario())
                            .subtotal(d.getSubtotal())
                            .build())
                    .toList();

            double total = detalles.stream().mapToDouble(DetallePedido::getSubtotal).sum();

            return HistorialPedidoResponse.builder()
                    .pedidoId(pedido.getId())
                    .fecha(pedido.getCreadoEn().toString())
                    .estado(pedido.getEstado().name())
                    .metodoPago(pedido.getMetodoPago().name())
                    .direccionEntrega(pedido.getDireccionEntrega())
                    .productos(items)
                    .total(total)
                    .build();
        }).toList();
    }

    @Transactional
    public DetalleCompraResponse procesarPagoConComprobante(
            Integer usuarioId,
            String direccionEntrega,
            String metodoPago,
            MultipartFile comprobante) throws IOException {

        // 1. Validar usuario
        Usuario usuario = usuarioRepo.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // 2. Obtener productos del carrito
        List<Carrito> carritoItems = carritoRepo.findByUsuarioId(usuarioId);
        if (carritoItems.isEmpty()) {
            throw new IllegalStateException("El carrito está vacío");
        }

        // 3. Validar método de pago
        Pedido.MetodoPago metodo;
        try {
            metodo = Pedido.MetodoPago.valueOf(metodoPago.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Método de pago inválido: " + metodoPago);
        }

        Pedido.EstadoPedido estado = Pedido.EstadoPedido.PENDIENTE;
        String comprobanteUrl = null;

        // 4. Si es YAPE, se debe subir el comprobante
        if (metodo == Pedido.MetodoPago.YAPE) {
            if (comprobante == null || comprobante.isEmpty()) {
                throw new IllegalArgumentException("Debe subir el comprobante de pago Yape.");
            }
            Map<?, ?> uploadResult = cloudinary.uploader().upload(comprobante.getBytes(), ObjectUtils.emptyMap());
            comprobanteUrl = uploadResult.get("secure_url").toString();
            estado = Pedido.EstadoPedido.PENDIENTE;
        }

        // 5. Crear y guardar pedido
        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setDireccionEntrega(direccionEntrega);
        pedido.setMetodoPago(metodo);
        pedido.setEstado(estado);
        pedido.setComprobanteUrl(comprobanteUrl);
        pedidoRepo.save(pedido);

        // 6. Crear detalles del pedido y calcular total
        double total = 0;
        List<DetalleCompraResponse.ItemCompra> detalleList = new ArrayList<>();

        for (Carrito item : carritoItems) {
            Producto producto = item.getProducto();

            if (producto.getStock() < item.getCantidad()) {
                throw new IllegalStateException("Stock insuficiente para el producto: " + producto.getNombre());
            }

            double subtotal = producto.getPrecio() * item.getCantidad();

            DetallePedido detalle = new DetallePedido();
            detalle.setPedido(pedido);
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecio());
            detalle.setSubtotal(subtotal);
            detalleRepo.save(detalle);

            // Actualizar stock
            producto.setStock(producto.getStock() - item.getCantidad());
            productoRepo.save(producto);

            // Añadir al detalle de respuesta
            detalleList.add(DetalleCompraResponse.ItemCompra.builder()
                    .nombre(producto.getNombre())
                    .cantidad(item.getCantidad())
                    .precioUnitario(producto.getPrecio())
                    .subtotal(subtotal)
                    .build());

            total += subtotal;
        }

        // 7. Vaciar carrito
        carritoRepo.deleteByUsuarioId(usuarioId);

        // 8. Retornar respuesta
        return DetalleCompraResponse.builder()
                .pedidoId(pedido.getId())
                .fecha(pedido.getCreadoEn().toString())
                .metodoPago(pedido.getMetodoPago().name())
                .estado(pedido.getEstado().name())
                .direccionEntrega(pedido.getDireccionEntrega())
                .total(total)
                .items(detalleList)
                .build();
    }

    @Transactional
    public void actualizarEstadoPedido(Integer pedidoId, Pedido.EstadoPedido nuevoEstado) {
        Pedido pedido = pedidoRepo.findById(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado con ID: " + pedidoId));

        if (pedido.getEstado() == Pedido.EstadoPedido.COMPLETADO && nuevoEstado == Pedido.EstadoPedido.CANCELADO) {
            throw new IllegalStateException("No se puede cancelar un pedido ya completado.");
        }

        // Restaurar stock si el pedido pasa de PENDIENTE a CANCELADO
        if (pedido.getEstado() == Pedido.EstadoPedido.PENDIENTE && nuevoEstado == Pedido.EstadoPedido.CANCELADO) {
            List<DetallePedido> detalles = detalleRepo.findByPedidoId(pedidoId);

            for (DetallePedido detalle : detalles) {
                Producto producto = detalle.getProducto();
                producto.setStock(producto.getStock() + detalle.getCantidad());
                productoRepo.save(producto); // restaurar stock
            }
        }

        pedido.setEstado(nuevoEstado);
        pedidoRepo.save(pedido);
    }

    public List<HistorialPedidoResponse> obtenerPedidosPorEstado(Pedido.EstadoPedido estado) {
        List<Pedido> pedidos = pedidoRepo.findByEstado(estado);

        return pedidos.stream().map(pedido -> {
            List<DetallePedido> detalles = detalleRepo.findByPedidoId(pedido.getId());

            List<HistorialPedidoResponse.ItemDetalle> items = detalles.stream()
                    .map(d -> HistorialPedidoResponse.ItemDetalle.builder()
                            .nombre(d.getProducto().getNombre())
                            .cantidad(d.getCantidad())
                            .precioUnitario(d.getPrecioUnitario())
                            .subtotal(d.getSubtotal())
                            .build())
                    .toList();

            double total = detalles.stream().mapToDouble(DetallePedido::getSubtotal).sum();

            return HistorialPedidoResponse.builder()
                    .pedidoId(pedido.getId())
                    .fecha(pedido.getCreadoEn().toString())
                    .estado(pedido.getEstado().name())
                    .metodoPago(pedido.getMetodoPago().name())
                    .direccionEntrega(pedido.getDireccionEntrega())
                    .productos(items)
                    .total(total)
                    .nombreUsuario(pedido.getUsuario().getNombre()) // nuevo
                    .usuarioId(pedido.getUsuario().getId()) // nuevo
                    .emailUsuario(pedido.getUsuario().getEmail()) // si lo tienes
                    .build();
        }).toList();
    }

}
