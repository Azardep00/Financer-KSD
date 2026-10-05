package com.financerksd.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.financerksd.api.dto.*;
import com.financerksd.api.model.Cliente;
import com.financerksd.api.model.DiagnosticoFinanciero;
import com.financerksd.api.model.PlanMejora;
import com.financerksd.api.service.*;

import jakarta.validation.Valid;

// Todo lo que "cuelga" de un cliente: su ficha, diagnosticos, planes y progreso.
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;
    private final DiagnosticoService diagnosticoService;
    private final PlanMejoraService planService;
    private final ProgresoService progresoService;
    private final AutorizacionService autorizacion;

    public ClienteController(ClienteService clienteService, DiagnosticoService diagnosticoService,
                             PlanMejoraService planService, ProgresoService progresoService,
                             AutorizacionService autorizacion) {
        this.clienteService = clienteService;
        this.diagnosticoService = diagnosticoService;
        this.planService = planService;
        this.progresoService = progresoService;
        this.autorizacion = autorizacion;
    }

    // ---------- Clientes (panel del asesor) ----------

    /** ?soloMios=true -> solo los clientes asignados al asesor que consulta. */
    @GetMapping
    public List<Cliente> listar(@RequestParam(defaultValue = "false") boolean soloMios, Authentication auth) {
        return clienteService.listar(soloMios ? autorizacion.idActual(auth) : null);
    }

    @GetMapping("/{id}")
    public Cliente obtener(@PathVariable int id, Authentication auth) {
        autorizacion.exigirAccesoACliente(auth, id);
        return clienteService.buscar(id);
    }

    @PatchMapping("/{id}/asesor")
    public Cliente asignarAsesor(@PathVariable int id, @Valid @RequestBody AsignarAsesorRequest req) {
        return clienteService.asignarAsesor(id, req.idAsesor());
    }

    // ---------- Diagnosticos ----------

    @PostMapping("/{id}/diagnosticos")
    public ResponseEntity<DiagnosticoFinanciero> crearDiagnostico(
            @PathVariable int id, @Valid @RequestBody DiagnosticoRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(diagnosticoService.crear(id, req));
    }

    @GetMapping("/{id}/diagnosticos")
    public List<DiagnosticoFinanciero> historialDiagnosticos(@PathVariable int id, Authentication auth) {
        autorizacion.exigirAccesoACliente(auth, id);
        return diagnosticoService.historial(id);
    }

    @GetMapping("/{id}/diagnosticos/ultimo")
    public DiagnosticoFinanciero ultimoDiagnostico(@PathVariable int id, Authentication auth) {
        autorizacion.exigirAccesoACliente(auth, id);
        return diagnosticoService.ultimo(id);
    }

    // ---------- Planes ----------

    @PostMapping("/{id}/planes")
    public ResponseEntity<PlanMejora> crearPlan(
            @PathVariable int id, @Valid @RequestBody PlanRequest req, Authentication auth) {
        PlanMejora plan = planService.crear(id, autorizacion.idActual(auth), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(plan);
    }

    @GetMapping("/{id}/planes")
    public List<PlanMejora> historialPlanes(@PathVariable int id, Authentication auth) {
        autorizacion.exigirAccesoACliente(auth, id);
        return planService.historial(id);
    }

    @GetMapping("/{id}/planes/activo")
    public PlanMejora planActivo(@PathVariable int id, Authentication auth) {
        autorizacion.exigirAccesoACliente(auth, id);
        return planService.planActivo(id);
    }

    // ---------- Progreso (dashboard) ----------

    @GetMapping("/{id}/progreso")
    public ProgresoResponse progreso(@PathVariable int id, Authentication auth) {
        autorizacion.exigirAccesoACliente(auth, id);
        return progresoService.calcular(id);
    }
}
