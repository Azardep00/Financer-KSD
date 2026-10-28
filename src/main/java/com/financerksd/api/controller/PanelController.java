package com.financerksd.api.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.financerksd.api.dto.PanelResponse;
import com.financerksd.api.model.EstadoCliente;
import com.financerksd.api.service.AutorizacionService;
import com.financerksd.api.service.PanelClientesService;

// Panel del asesor (solo ASESOR, ver SecurityConfig).
@RestController
public class PanelController {

    private final PanelClientesService service;
    private final AutorizacionService autorizacion;

    public PanelController(PanelClientesService service, AutorizacionService autorizacion) {
        this.service = service;
        this.autorizacion = autorizacion;
    }

    /**
     * GET /api/clientes/panel?q=juan&estado=ACTIVO&mios=true&conAlertas=true&orden=alertas&pagina=0&tamano=20
     * mios=true: solo mis clientes | sinAsesor=true: solo los que aun no tienen asesor.
     */
    @GetMapping("/api/clientes/panel")
    public PanelResponse panel(@RequestParam(required = false) String q,
                               @RequestParam(required = false) EstadoCliente estado,
                               @RequestParam(defaultValue = "false") boolean mios,
                               @RequestParam(defaultValue = "false") boolean sinAsesor,
                               @RequestParam(defaultValue = "false") boolean conAlertas,
                               @RequestParam(required = false) String orden,
                               @RequestParam(defaultValue = "0") int pagina,
                               @RequestParam(defaultValue = "20") int tamano,
                               Authentication auth) {
        return service.panel(autorizacion.idActual(auth), q, estado, mios, sinAsesor, conAlertas, orden, pagina, tamano);
    }
}
