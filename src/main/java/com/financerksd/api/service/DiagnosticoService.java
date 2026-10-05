package com.financerksd.api.service;

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

    public DiagnosticoService(DiagnosticoFinancieroRepository repo, ClienteService clienteService) {
        this.repo = repo;
        this.clienteService = clienteService;
    }

    @Transactional
    public DiagnosticoFinanciero crear(int idCliente, DiagnosticoRequest req) {
        Cliente cliente = clienteService.buscar(idCliente);

        DiagnosticoFinanciero d = new DiagnosticoFinanciero();
        d.setCliente(cliente);
        d.setFecha(req.fecha() != null ? req.fecha() : LocalDate.now());
        d.setIngresoMensual(req.ingresoMensual());
        d.setGastoMensual(req.gastoMensual());
        d.setDeudaTotal(req.deudaTotal());
        d.setAhorroActual(req.ahorroActual());
        return repo.save(d);
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
