package com.financerksd.api.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.financerksd.api.dto.DiagnosticoRequest;
import com.financerksd.api.exception.RecursoNoEncontradoException;
import com.financerksd.api.model.Cliente;
import com.financerksd.api.model.DiagnosticoFinanciero;
import com.financerksd.api.repository.DiagnosticoFinancieroRepository;

@Service
public class DiagnosticoService {

    private final DiagnosticoFinancieroRepository repo;
    private final ClienteService clienteService;

    private final PlanMejoraService planService;
    private final Clock reloj;

    public DiagnosticoService(DiagnosticoFinancieroRepository repo, ClienteService clienteService,
                              PlanMejoraService planService, Clock reloj) {
        this.repo = repo;
        this.clienteService = clienteService;
        this.planService = planService;
        this.reloj = reloj;
    }

    @Transactional
    public DiagnosticoFinanciero crear(int idCliente, DiagnosticoRequest req) {
        Cliente cliente = clienteService.buscar(idCliente);

        DiagnosticoFinanciero d = new DiagnosticoFinanciero();
        d.setCliente(cliente);
        d.setFecha(req.fecha() != null ? req.fecha() : LocalDate.now(reloj));
        d.setIngresoMensual(req.ingresoMensual());
        d.setGastoMensual(req.gastoMensual());
        d.setDeudaTotal(req.deudaTotal());
        d.setAhorroActual(req.ahorroActual());
        DiagnosticoFinanciero guardado = repo.save(d);

        // Solo el diagnostico MAS RECIENTE mueve las metas (uno con fecha atrasada no pisa los valores nuevos).
        repo.findFirstByCliente_IdUsuarioOrderByFechaDescIdDiagnosticoDesc(idCliente)
                .filter(ultimo -> ultimo.getIdDiagnostico().equals(guardado.getIdDiagnostico()))
                .ifPresent(ultimo -> planService.sincronizarConDiagnostico(idCliente, ultimo));
        return guardado;
    }

    /** Del mas reciente al mas antiguo. */
    public List<DiagnosticoFinanciero> historial(int idCliente) {
        clienteService.buscar(idCliente);
        return repo.findByCliente_IdUsuarioOrderByFechaDescIdDiagnosticoDesc(idCliente);
    }

    /** Del mas antiguo al mas reciente (para graficas). */
    public List<DiagnosticoFinanciero> historialCronologico(int idCliente) {
        clienteService.buscar(idCliente);
        return repo.findByCliente_IdUsuarioOrderByFechaAscIdDiagnosticoAsc(idCliente);
    }

    public DiagnosticoFinanciero ultimo(int idCliente) {
        clienteService.buscar(idCliente);
        return repo.findFirstByCliente_IdUsuarioOrderByFechaDescIdDiagnosticoDesc(idCliente)
                .orElseThrow(() -> new RecursoNoEncontradoException("El cliente aún no tiene diagnósticos."));
    }
}
