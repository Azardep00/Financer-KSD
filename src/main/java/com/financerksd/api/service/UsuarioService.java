package com.financerksd.api.service;

import org.springframework.stereotype.Service;

import com.financerksd.api.dto.LoginResponse;
import com.financerksd.api.dto.RegistroAsesorRequest;
import com.financerksd.api.dto.RegistroClienteRequest;
import com.financerksd.api.exception.CredencialesInvalidasException;
import com.financerksd.api.exception.RecursoNoEncontradoException;
import com.financerksd.api.model.Asesor;
import com.financerksd.api.model.Cliente;
import com.financerksd.api.model.Usuario;
import com.financerksd.api.repository.UsuarioRepository;
import com.financerksd.api.security.JwtService;

@Service
public class UsuarioService {

    private final UsuarioRepository repo;
    private final JwtService jwtService;

    public UsuarioService(UsuarioRepository repo, JwtService jwtService) {
        this.repo = repo;
        this.jwtService = jwtService;
    }

    public Usuario registrarCliente(RegistroClienteRequest req) {
        validarCorreoLibre(req.correo());
        Cliente c = new Cliente();
        c.setNombre(req.nombre());
        c.setApellido(req.apellido());
        c.setTelefono(req.telefono());
        c.setCorreo(req.correo());
        c.establecerContrasena(req.contrasena());
        return repo.save(c);
    }

    public Usuario registrarAsesor(RegistroAsesorRequest req) {
        validarCorreoLibre(req.correo());
        Asesor a = new Asesor();
        a.setNombre(req.nombre());
        a.setApellido(req.apellido());
        a.setEspecialidad(req.especialidad());
        a.setCorreo(req.correo());
        a.establecerContrasena(req.contrasena());
        return repo.save(a);
    }

    public LoginResponse iniciarSesion(String correo, String contrasena) {
        Usuario u = repo.findByCorreoIgnoreCase(correo)
                .orElseThrow(() -> new CredencialesInvalidasException("Correo o contraseña incorrectos."));
        if (!u.isEstado()) {
            throw new CredencialesInvalidasException("El usuario está inactivo.");
        }
        if (!u.verificarContrasena(contrasena)) {
            throw new CredencialesInvalidasException("Correo o contraseña incorrectos.");
        }
        return LoginResponse.desde(u, jwtService.generarToken(u));
    }

    public Usuario buscarPorId(int id) {
        return repo.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado."));
    }

    private void validarCorreoLibre(String correo) {
        if (repo.existsByCorreoIgnoreCase(correo)) {
            throw new IllegalArgumentException("Ya existe un usuario registrado con ese correo.");
        }
    }
}
