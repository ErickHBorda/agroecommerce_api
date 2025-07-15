package com.api.agroventa.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.api.agroventa.dto.request.LoginRequest;
import com.api.agroventa.dto.request.UsuarioRequest;
import com.api.agroventa.dto.response.LoginResponse;
import com.api.agroventa.dto.response.UsuarioResponse;
import com.api.agroventa.model.Usuario;
import com.api.agroventa.repository.UsuarioRepository;

@Service
public class UsuarioService {
    private final UsuarioRepository repository;

    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
    }

    public List<UsuarioResponse> listarUsuarios() {
        return repository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public Usuario guardar(UsuarioRequest request) {
        if (repository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El correo ya está registrado");
        }
        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(request.getPassword());
        usuario.setTelefono(request.getTelefono());
        usuario.setDireccion(request.getDireccion());
        return repository.save(usuario);
    }

    private UsuarioResponse toDto(Usuario usuario) {
        UsuarioResponse dto = new UsuarioResponse();
        dto.setId(usuario.getId());
        dto.setNombre(usuario.getNombre());
        dto.setEmail(usuario.getEmail());
        dto.setTelefono(usuario.getTelefono());
        dto.setDireccion(usuario.getDireccion());
        return dto;
    }

    public boolean eliminarUsuario(Integer id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    public boolean actualizarUsuario(Integer id, UsuarioRequest request) {
        Optional<Usuario> optional = repository.findById(id);
        if (optional.isPresent()) {
            Usuario usuario = optional.get();
            usuario.setNombre(request.getNombre());
            usuario.setEmail(request.getEmail());
            usuario.setPassword(request.getPassword());
            usuario.setTelefono(request.getTelefono());
            usuario.setDireccion(request.getDireccion());
            repository.save(usuario);
            return true;
        }
        return false;
    }

    public Optional<LoginResponse> login(LoginRequest request) {
        Optional<Usuario> usuarioOpt = repository.findByEmail(request.getEmail());

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            if (usuario.getPassword().equals(request.getPassword())) { // En producción usar BCrypt
                LoginResponse res = new LoginResponse();
                res.setId(usuario.getId());
                res.setNombre(usuario.getNombre());
                res.setEmail(usuario.getEmail());
                res.setTelefono(usuario.getTelefono());
                res.setDireccion(usuario.getDireccion());
                return Optional.of(res);
            }
        }
        return Optional.empty();
    }

}
