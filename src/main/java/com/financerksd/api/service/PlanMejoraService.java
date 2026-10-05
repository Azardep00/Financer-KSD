package com.financerksd.api.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.financerksd.api.dto.MetaRequest;
import com.financerksd.api.dto.PlanRequest;
import com.financerksd.api.exception.RecursoNoEncontradoException;
import com.financerksd.api.model.Asesor;
import com.financerksd.api.model.Cliente;
import com.financerksd.api.model.MetaFinanciera;
import com.financerksd.api.model.PlanMejora;
import com.financerksd.api.repository.AsesorRepository;
import com.financerksd.api.repository.ClienteRepository;
import com.financerksd.api.repository.MetaFinancieraRepository;
import com.financerksd.api.repository.PlanMejoraRepository;

@Service
public class PlanMejoraService {

    private final PlanMejoraRepository planes;
    private final MetaFinancieraRepository metas;
    private final ClienteService clienteService;
    private final ClienteRepository clientes;
    private final AsesorRepository asesores;

    public PlanMejoraService(PlanMejoraRepository planes, MetaFinancieraRepository metas,
                             ClienteService clienteService, ClienteRepository clientes,
                             AsesorRepository asesores) {
        this.planes = planes;
        this.metas = metas;
        this.clienteService = clienteService;
        this.clientes = clientes;
        this.asesores = asesores;
    }

    // ---------------- Planes ----------------

    @Transactional
    public PlanMejora crear(int idCliente, int idAsesor, PlanRequest req) {
        Cliente cliente = clienteService.buscar(idCliente);
        Asesor asesor = asesores.findById(idAsesor)
                .orElseThrow(() -> new RecursoNoEncontradoException("Asesor no encontrado."));

        PlanMejora plan = new PlanMejora();
        plan.setCliente(cliente);
        plan.setAsesor(asesor);
        plan.setTitulo(req.titulo());
        plan.setDescripcion(req.descripcion());
        if (req.metas() != null) {
            req.metas().forEach(m -> plan.agregarMeta(nuevaMeta(m)));
        }

        // Si el cliente aun no tenia asesor, queda asignado al que le arma el plan.
        if (cliente.getAsesorAsignado() == null) {
            cliente.setAsesorAsignado(asesor);
            clientes.save(cliente);
        }
        return planes.save(plan);
    }

    @Transactional(readOnly = true)
    public List<PlanMejora> historial(int idCliente) {
        clienteService.buscar(idCliente);
        return planes.findByCliente_IdUsuarioOrderByFechaCreacionDescIdPlanDesc(idCliente);
    }

    @Transactional(readOnly = true)
    public PlanMejora planActivo(int idCliente) {
        clienteService.buscar(idCliente);
        return planes.findFirstByCliente_IdUsuarioOrderByFechaCreacionDescIdPlanDesc(idCliente)
                .orElseThrow(() -> new RecursoNoEncontradoException("El cliente aún no tiene un plan de mejora."));
    }

    @Transactional(readOnly = true)
    public PlanMejora buscar(int idPlan) {
        return planes.findById(idPlan).orElseThrow(() -> new RecursoNoEncontradoException("Plan no encontrado."));
    }

    @Transactional
    public PlanMejora actualizar(int idPlan, PlanRequest req) {
        PlanMejora plan = buscar(idPlan);
        plan.setTitulo(req.titulo());
        plan.setDescripcion(req.descripcion());
        return planes.save(plan);
    }

    // ---------------- Metas ----------------

    @Transactional
    public MetaFinanciera agregarMeta(int idPlan, MetaRequest req) {
        PlanMejora plan = buscar(idPlan);
        MetaFinanciera meta = nuevaMeta(req);
        plan.agregarMeta(meta);
        planes.save(plan);
        return meta;
    }

    @Transactional(readOnly = true)
    public MetaFinanciera buscarMeta(int idMeta) {
        return metas.findById(idMeta).orElseThrow(() -> new RecursoNoEncontradoException("Meta no encontrada."));
    }

    @Transactional
    public MetaFinanciera editarMeta(int idMeta, MetaRequest req) {
        MetaFinanciera m = buscarMeta(idMeta);
        m.setDescripcion(req.descripcion());
        m.setFechaLimite(req.fechaLimite());
        return metas.save(m);
    }

    @Transactional
    public MetaFinanciera cambiarEstadoMeta(int idMeta, boolean completada) {
        MetaFinanciera m = buscarMeta(idMeta);
        if (completada) m.marcarCompletada(); else m.marcarPendiente();
        return metas.save(m);
    }

    @Transactional
    public void eliminarMeta(int idMeta) {
        MetaFinanciera m = buscarMeta(idMeta);
        m.getPlan().quitarMeta(m); // orphanRemoval se encarga del DELETE
    }

    /** Id del cliente dueño de una meta (para validar permisos). */
    @Transactional(readOnly = true)
    public int idClienteDeMeta(int idMeta) {
        return buscarMeta(idMeta).getPlan().getCliente().getIdUsuario();
    }

    private MetaFinanciera nuevaMeta(MetaRequest req) {
        MetaFinanciera m = new MetaFinanciera();
        m.setDescripcion(req.descripcion());
        m.setFechaLimite(req.fechaLimite());
        return m;
    }
}
