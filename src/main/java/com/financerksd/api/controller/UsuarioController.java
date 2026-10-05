package com.financerksd.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.financerksd.api.dto.*;
import com.financerksd.api.model.Usuario;
import com.financerksd.api.service.AutorizacionService;
import com.financerksd.api.service.UsuarioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService service;
    private final AutorizacionService autorizacion;

    public UsuarioController(UsuarioService service, AutorizacionService autorizacion) {
        this.service = service;
        this.autorizacion = autorizacion;
    }

    /** Registro publico de clientes. */
    @PostMapping("/clientes")
    public ResponseEntity<LoginResponse> registrarCliente(@Valid @RequestBody RegistroClienteRequest req) {
        Usuario u = service.registrarCliente(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(LoginResponse.desde(u));
    }

    /** Solo un asesor autenticado puede crear otros asesores. */
    @PostMapping("/asesores")
    public ResponseEntity<LoginResponse> registrarAsesor(@Valid @RequestBody RegistroAsesorRequest req) {
        Usuario u = service.registrarAsesor(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(LoginResponse.desde(u));
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        return service.iniciarSesion(req.correo(), req.contrasena());
    }

    /** Datos del usuario del token (util para que el frontend restaure la sesion). */
    @GetMapping("/me")
    public LoginResponse me(Authentication auth) {
        return LoginResponse.desde(service.buscarPorId(autorizacion.idActual(auth)));
    }
}
