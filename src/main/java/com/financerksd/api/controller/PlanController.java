package com.financerksd.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.financerksd.api.dto.CancelarPlanRequest;
import com.financerksd.api.dto.CierreRequest;
import com.financerksd.api.dto.CierreResponse;
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
    public PlanMejora actualizar(@PathVariable int id, @Valid @RequestBody PlanRequest req, Authentication auth) {
        exigirResponsable(id, auth);
        return service.actualizar(id, req);
    }

    /** Asesor: cierre de mes. Por defecto crea el plan del mes siguiente con las metas pendientes. */
    @PostMapping("/{id}/cerrar")
    public CierreResponse cerrar(@PathVariable int id, @RequestBody(required = false) CierreRequest req, Authentication auth) {
        exigirResponsable(id, auth);
        return service.cerrar(id, req);
    }

    /** Asesor: descarta el plan activo. */
    @PostMapping("/{id}/cancelar")
    public PlanMejora cancelar(@PathVariable int id, @RequestBody(required = false) CancelarPlanRequest req, Authentication auth) {
        exigirResponsable(id, auth);
        return service.cancelar(id, req == null ? null : req.motivo());
    }

    @PostMapping("/{id}/metas")
    public ResponseEntity<MetaFinanciera> agregarMeta(@PathVariable int id, @Valid @RequestBody MetaRequest req, Authentication auth) {
        exigirResponsable(id, auth);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.agregarMeta(id, req));
    }

    /** Solo el asesor asignado al dueño del plan (o cualquiera si el cliente aun no tiene asesor). */
    private void exigirResponsable(int idPlan, Authentication auth) {
        autorizacion.exigirAsesorResponsable(auth, service.buscar(idPlan).getCliente().getIdUsuario());
    }
}
