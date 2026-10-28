package com.financerksd.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.financerksd.api.dto.NotaRequest;
import com.financerksd.api.model.NotaAsesor;
import com.financerksd.api.service.AutorizacionService;
import com.financerksd.api.service.NotaService;

import jakarta.validation.Valid;

// Notas PRIVADAS del asesor. Todas las rutas son solo de ASESOR (ver SecurityConfig).
@RestController
@RequestMapping("/api")
public class NotaController {

    private final NotaService service;
    private final AutorizacionService autorizacion;

    public NotaController(NotaService service, AutorizacionService autorizacion) {
        this.service = service;
        this.autorizacion = autorizacion;
    }

    @GetMapping("/clientes/{id}/notas")
    public List<NotaAsesor> listar(@PathVariable int id, Authentication auth) {
        autorizacion.exigirAsesorResponsable(auth, id);
        return service.listar(id);
    }

    @PostMapping("/clientes/{id}/notas")
    public ResponseEntity<NotaAsesor> crear(@PathVariable int id, @Valid @RequestBody NotaRequest req, Authentication auth) {
        autorizacion.exigirAsesorResponsable(auth, id);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(id, autorizacion.idActual(auth), req.texto()));
    }

    @DeleteMapping("/notas/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable int id, Authentication auth) {
        service.eliminar(id, autorizacion.idActual(auth));
        return ResponseEntity.noContent().build();
    }
}
