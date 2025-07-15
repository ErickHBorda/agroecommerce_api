package com.api.agroventa.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.api.agroventa.dto.request.ProductoRequest;
import com.api.agroventa.dto.response.ProductoResponse;
import com.api.agroventa.model.Producto;
import com.api.agroventa.repository.ProductoRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Service
public class ProductoService {
    private final ProductoRepository repository;
    private final Cloudinary cloudinary;

    public ProductoService(ProductoRepository repository, Cloudinary cloudinary) {
        this.repository = repository;
        this.cloudinary = cloudinary;
    }

    public List<ProductoResponse> listar() {
        return repository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public Producto guardar(ProductoRequest request) {
        Producto producto = new Producto();
        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecio(request.getPrecio());
        producto.setStock(request.getStock());
        producto.setImagenUrl(request.getImagenUrl());
        producto.setCategoria(Producto.Categoria.valueOf(request.getCategoria()));
        return repository.save(producto);
    }

    public boolean eliminar(Integer id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    public boolean actualizarProducto(Integer id, String nombre, String descripcion, Double precio, Integer stock,
            String categoria,
            MultipartFile imagen) throws IOException {
        Optional<Producto> optional = repository.findById(id);
        if (optional.isPresent()) {
            Producto producto = optional.get();

            // 1. Subir imagen si se proporcionó
            if (imagen != null && !imagen.isEmpty()) {
                Map<?, ?> uploadResult = cloudinary.uploader().upload(imagen.getBytes(), ObjectUtils.emptyMap());
                String url = uploadResult.get("secure_url").toString();
                producto.setImagenUrl(url);
            }

            // 2. Actualizar datos
            producto.setNombre(nombre);
            producto.setDescripcion(descripcion);
            producto.setPrecio(precio);
            producto.setStock(stock);
            producto.setCategoria(Producto.Categoria.valueOf(categoria));

            repository.save(producto);
            return true;
        }
        return false;
    }

    private ProductoResponse toDto(Producto producto) {
        ProductoResponse dto = new ProductoResponse();
        dto.setId(producto.getId());
        dto.setNombre(producto.getNombre());
        dto.setDescripcion(producto.getDescripcion());
        dto.setPrecio(producto.getPrecio());
        dto.setStock(producto.getStock());
        dto.setCategoria(producto.getCategoria().name());
        dto.setImagenUrl(producto.getImagenUrl());
        return dto;
    }

    public void subirYGuardarProducto(String nombre, String descripcion, Double precio, Integer stock, String categoria,
            MultipartFile imagen) throws IOException {
        // 1. Subir a Cloudinary
        Map<?, ?> uploadResult = cloudinary.uploader().upload(imagen.getBytes(), ObjectUtils.emptyMap());

        String url = uploadResult.get("secure_url").toString();

        // 2. Guardar en la base de datos
        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setPrecio(precio);
        producto.setStock(stock);
        producto.setCategoria(Producto.Categoria.valueOf(categoria));
        producto.setImagenUrl(url);

        repository.save(producto);
    }

    public List<ProductoResponse> listarPorCategoria(String categoriaNombre) {
        Producto.Categoria categoria = Producto.Categoria.valueOf(categoriaNombre);
        return repository.findByCategoria(categoria).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
