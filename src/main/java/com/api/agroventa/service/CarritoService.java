package com.api.agroventa.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.api.agroventa.dto.request.CarritoRequest;
import com.api.agroventa.dto.response.CarritoResponse;
import com.api.agroventa.model.Carrito;
import com.api.agroventa.model.Producto;
import com.api.agroventa.model.Usuario;
import com.api.agroventa.repository.CarritoRepository;
import com.api.agroventa.repository.ProductoRepository;
import com.api.agroventa.repository.UsuarioRepository;

import jakarta.transaction.Transactional;

@Service
public class CarritoService {

    private final CarritoRepository carritoRepo;
    private final UsuarioRepository usuarioRepo;
    private final ProductoRepository productoRepo;

    public CarritoService(CarritoRepository carritoRepo, UsuarioRepository usuarioRepo,
            ProductoRepository productoRepo) {
        this.carritoRepo = carritoRepo;
        this.usuarioRepo = usuarioRepo;
        this.productoRepo = productoRepo;
    }

    public List<CarritoResponse> listarCarritoPorUsuario(Integer usuarioId) {
        return carritoRepo.findByUsuarioId(usuarioId).stream().map(carrito -> {
            CarritoResponse response = new CarritoResponse();
            response.setId(carrito.getId());
            response.setCantidad(carrito.getCantidad());
            response.setProductoId(carrito.getProducto().getId());
            response.setNombre(carrito.getProducto().getNombre());
            response.setImagenUrl(carrito.getProducto().getImagenUrl());
            response.setPrecio(carrito.getProducto().getPrecio());
            response.setSubtotal(carrito.getProducto().getPrecio() * carrito.getCantidad());
            return response;
        }).collect(Collectors.toList());
    }

    public void agregarProducto(CarritoRequest request) {
        Optional<Carrito> carritoOpt = carritoRepo.findByUsuarioIdAndProductoId(request.getUsuarioId(),
                request.getProductoId());

        if (carritoOpt.isPresent()) {
            Carrito carrito = carritoOpt.get();
            carrito.setCantidad(carrito.getCantidad() + request.getCantidad());
            carritoRepo.save(carrito);
        } else {
            Usuario usuario = usuarioRepo.findById(request.getUsuarioId()).orElseThrow();
            Producto producto = productoRepo.findById(request.getProductoId()).orElseThrow();

            Carrito nuevo = new Carrito();
            nuevo.setUsuario(usuario);
            nuevo.setProducto(producto);
            nuevo.setCantidad(request.getCantidad());
            carritoRepo.save(nuevo);
        }
    }

    @Transactional
    public boolean eliminarProducto(Integer usuarioId, Integer productoId) {
        if (carritoRepo.existsByUsuarioIdAndProductoId(usuarioId, productoId)) {
            carritoRepo.deleteByUsuarioIdAndProductoId(usuarioId, productoId);
            return true;
        }
        return false;
    }

    @Transactional
    public void vaciarCarrito(Integer usuarioId) {
        carritoRepo.deleteByUsuarioId(usuarioId);
    }
}
