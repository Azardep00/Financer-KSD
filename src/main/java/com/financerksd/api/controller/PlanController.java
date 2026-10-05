package com.financerksd.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.financerksd.api.dto.MetaRequest;
import com.financerksd.api.dto.PlanRequest;
import com.financerksd.api.model.MetaFinanciera;
import com.financerksd.api.model.PlanMejora;
import com.financerksd.api.service.AutorizacionService;
import com.financerksd.api.service.PlanMejoraService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/planes")
public class PlanController {

    private final PlanMejoraService service;
    private final AutorizacionService autorizacion;

    public PlanController(PlanMejoraService service, AutorizacionService autorizacion) {
        this.service = service;
        this.autorizacion = autorizacion;
    }

    @GetMapping("/{id}")
    public PlanMejora obtener(@PathVariable int id, Authentication auth) {
        PlanMejora plan = service.buscar(id);
        autorizacion.exigirAccesoACliente(auth, plan.getCliente().getIdUsuario());
        return plan;
    }

    @PutMapping("/{id}")
    public PlanMejora actualizar(@PathVariable int id, @Valid @RequestBody PlanRequest req) {
        return service.actualizar(id, req);
    }

    @PostMapping("/{id}/metas")
    public ResponseEntity<MetaFinanciera> agregarMeta(@PathVariable int id, @Valid @RequestBody MetaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.agregarMeta(id, req));
    }
}
