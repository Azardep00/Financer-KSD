package com.financerksd.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.financerksd.api.dto.MetaRequest;
import com.financerksd.api.model.MetaFinanciera;
import com.financerksd.api.service.AutorizacionService;
import com.financerksd.api.service.PlanMejoraService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/metas")
public class MetaController {

    private final PlanMejoraService service;
    private final AutorizacionService autorizacion;

    public MetaController(PlanMejoraService service, AutorizacionService autorizacion) {
        this.service = service;
        this.autorizacion = autorizacion;
    }

    /** Asesor: edita descripcion / fecha limite. */
    @PutMapping("/{id}")
    public MetaFinanciera editar(@PathVariable int id, @Valid @RequestBody MetaRequest req) {
        return service.editarMeta(id, req);
    }

    /** Asesor: elimina la meta. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable int id) {
        service.eliminarMeta(id);
        return ResponseEntity.noContent().build();
    }

    /** Cliente (dueño) o asesor: marca la meta como cumplida. */
    @PatchMapping("/{id}/completar")
    public MetaFinanciera completar(@PathVariable int id, Authentication auth) {
        autorizacion.exigirAccesoACliente(auth, service.idClienteDeMeta(id));
        return service.cambiarEstadoMeta(id, true);
    }

    /** Cliente (dueño) o asesor: devuelve la meta a pendiente. */
    @PatchMapping("/{id}/pendiente")
    public MetaFinanciera pendiente(@PathVariable int id, Authentication auth) {
        autorizacion.exigirAccesoACliente(auth, service.idClienteDeMeta(id));
        return service.cambiarEstadoMeta(id, false);
    }
}
