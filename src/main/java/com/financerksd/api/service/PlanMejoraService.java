package com.financerksd.api.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.financerksd.api.dto.CierreRequest;
import com.financerksd.api.dto.CierreResponse;
import com.financerksd.api.dto.MetaRequest;
import com.financerksd.api.dto.PlanRequest;
import com.financerksd.api.exception.RecursoNoEncontradoException;
import com.financerksd.api.model.Asesor;
import com.financerksd.api.model.Cliente;
import com.financerksd.api.model.DiagnosticoFinanciero;
import com.financerksd.api.model.EstadoCliente;
import com.financerksd.api.model.EstadoPlan;
import com.financerksd.api.model.MetaFinanciera;
import com.financerksd.api.model.PlanMejora;
import com.financerksd.api.model.TipoMeta;
import com.financerksd.api.repository.AsesorRepository;
import com.financerksd.api.repository.ClienteRepository;
import com.financerksd.api.repository.DiagnosticoFinancieroRepository;
import com.financerksd.api.repository.MetaFinancieraRepository;
import com.financerksd.api.repository.PlanMejoraRepository;

@Service
public class PlanMejoraService {

    private final PlanMejoraRepository planes;
    private final MetaFinancieraRepository metas;
    private final ClienteService clienteService;
    private final ClienteRepository clientes;
    private final AsesorRepository asesores;
    private final DiagnosticoFinancieroRepository diagnosticos;
    private final Clock reloj;

    public PlanMejoraService(PlanMejoraRepository planes, MetaFinancieraRepository metas,
                             ClienteService clienteService, ClienteRepository clientes,
                             AsesorRepository asesores, DiagnosticoFinancieroRepository diagnosticos,
                             Clock reloj) {
        this.planes = planes;
        this.metas = metas;
        this.clienteService = clienteService;
        this.clientes = clientes;
        this.asesores = asesores;
        this.diagnosticos = diagnosticos;
        this.reloj = reloj;
    }

    // ---------------- Planes ----------------

    /** Crea el plan de un mes. Un cliente solo puede tener UN plan activo a la vez. */
    @Transactional
    public PlanMejora crear(int idCliente, int idAsesor, PlanRequest req) {
        Cliente cliente = clienteService.buscar(idCliente);
        Asesor asesor = asesores.findById(idAsesor)
                .orElseThrow(() -> new RecursoNoEncontradoException("Asesor no encontrado."));
        LocalDate hoy = LocalDate.now(reloj);

        if (planes.existsByCliente_IdUsuarioAndEstado(idCliente, EstadoPlan.ACTIVO)) {
            throw new IllegalArgumentException(
                    "El cliente ya tiene un plan activo. Ciérralo (o cancélalo) antes de crear otro.");
        }
        LocalDate periodo = req.periodo() != null ? req.periodo() : hoy;
        validarPeriodoLibre(idCliente, periodo.withDayOfMonth(1));

        PlanMejora plan = new PlanMejora();
        plan.setCliente(cliente);
        plan.setAsesor(asesor);
        plan.setTitulo(req.titulo());
        plan.setDescripcion(req.descripcion());
        plan.setFechaCreacion(hoy);
        plan.definirPeriodo(periodo);
        if (req.metas() != null) {
            req.metas().forEach(m -> plan.agregarMeta(nuevaMeta(m)));
        }
        // Las metas medibles arrancan con el valor real del ultimo diagnostico.
        diagnosticos.findFirstByCliente_IdUsuarioOrderByFechaDescIdDiagnosticoDesc(idCliente)
                .ifPresent(d -> plan.sincronizarConDiagnostico(d, hoy));

        // Si el cliente aun no tenia asesor, queda asignado al que le arma el plan.
        if (cliente.getAsesorAsignado() == null) {
            cliente.setAsesorAsignado(asesor);
        }
        // Primer plan: el prospecto pasa a ser cliente activo.
        if (cliente.getEstadoCliente() == EstadoCliente.PROSPECTO) {
            cliente.setEstadoCliente(EstadoCliente.ACTIVO);
        }
        clientes.save(cliente);
        return planes.save(plan);
    }

    @Transactional(readOnly = true)
    public List<PlanMejora> historial(int idCliente) {
        clienteService.buscar(idCliente);
        return planes.findByCliente_IdUsuarioOrderByFechaCreacionDescIdPlanDesc(idCliente);
    }

    /** El plan ACTIVO del cliente (el del mes en curso). */
    @Transactional(readOnly = true)
    public PlanMejora planActivo(int idCliente) {
        clienteService.buscar(idCliente);
        return planes.findFirstByCliente_IdUsuarioAndEstadoOrderByPeriodoInicioDescIdPlanDesc(idCliente, EstadoPlan.ACTIVO)
                .orElseThrow(() -> new RecursoNoEncontradoException("El cliente no tiene un plan activo."));
    }

    @Transactional(readOnly = true)
    public PlanMejora buscar(int idPlan) {
        return planes.findById(idPlan).orElseThrow(() -> new RecursoNoEncontradoException("Plan no encontrado."));
    }

    @Transactional
    public PlanMejora actualizar(int idPlan, PlanRequest req) {
        PlanMejora plan = buscar(idPlan);
        plan.exigirActivo();
        plan.setTitulo(req.titulo());
        plan.setDescripcion(req.descripcion());
        return planes.save(plan);
    }

    /**
     * Cierre de mes: el plan pasa a CERRADO y, por defecto, se crea el plan del mes siguiente
     * con las metas que quedaron pendientes.
     */
    @Transactional
    public CierreResponse cerrar(int idPlan, CierreRequest req) {
        PlanMejora plan = buscar(idPlan);
        plan.exigirActivo();
        boolean crearSiguiente = req == null || req.crearSiguiente() == null || req.crearSiguiente();
        boolean arrastrar = req == null || req.arrastrarPendientes() == null || req.arrastrarPendientes();
        LocalDate hoy = LocalDate.now(reloj);

        if (crearSiguiente) {
            validarPeriodoLibre(plan.getCliente().getIdUsuario(), plan.inicioSiguientePeriodo());
        }
        int total = plan.getMetas().size();
        int cumplidas = total - plan.metasPendientes().size();
        double progreso = plan.getProgreso();

        plan.cerrar(req == null ? null : req.notaCierre(), hoy);
        planes.save(plan);

        PlanMejora siguiente = null;
        if (crearSiguiente) {
            siguiente = plan.crearSiguiente(req == null ? null : req.tituloSiguiente(), arrastrar, hoy);
            final PlanMejora nuevo = siguiente;
            diagnosticos.findFirstByCliente_IdUsuarioOrderByFechaDescIdDiagnosticoDesc(plan.getCliente().getIdUsuario())
                    .ifPresent(d -> nuevo.sincronizarConDiagnostico(d, hoy));
            siguiente = planes.save(nuevo);
        }
        var resumen = new CierreResponse.Resumen(plan.getIdPlan(), plan.getPeriodo(), progreso, total, cumplidas,
                total - cumplidas, siguiente == null ? 0 : siguiente.getMetas().size());
        return new CierreResponse(resumen, siguiente);
    }

    /** Se descarta el plan activo antes de que termine el mes. */
    @Transactional
    public PlanMejora cancelar(int idPlan, String motivo) {
        PlanMejora plan = buscar(idPlan);
        plan.cancelar(motivo, LocalDate.now(reloj));
        return planes.save(plan);
    }

    /**
     * Cada vez que se guarda un diagnostico nuevo, las metas medibles del plan activo
     * toman los valores reales (ahorro, deuda, gasto) y se marcan solas al llegar al 100%.
     */
    @Transactional
    public void sincronizarConDiagnostico(int idCliente, DiagnosticoFinanciero d) {
        planes.findFirstByCliente_IdUsuarioAndEstadoOrderByPeriodoInicioDescIdPlanDesc(idCliente, EstadoPlan.ACTIVO)
                .ifPresent(plan -> {
                    if (plan.sincronizarConDiagnostico(d, LocalDate.now(reloj)) > 0) planes.save(plan);
                });
    }

    private void validarPeriodoLibre(int idCliente, LocalDate inicioMes) {
        if (planes.existsByCliente_IdUsuarioAndPeriodoInicioAndEstadoNot(idCliente, inicioMes, EstadoPlan.CANCELADO)) {
            throw new IllegalArgumentException("El cliente ya tiene un plan para " + inicioMes.getYear() + "-"
                    + String.format("%02d", inicioMes.getMonthValue()) + ".");
        }
    }

    // ---------------- Metas ----------------

    @Transactional
    public MetaFinanciera agregarMeta(int idPlan, MetaRequest req) {
        PlanMejora plan = buscar(idPlan);
        plan.exigirActivo();
        MetaFinanciera meta = nuevaMeta(req);
        plan.agregarMeta(meta);
        diagnosticos.findFirstByCliente_IdUsuarioOrderByFechaDescIdDiagnosticoDesc(plan.getCliente().getIdUsuario())
                .ifPresent(d -> meta.sincronizarCon(d, LocalDate.now(reloj)));
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
        m.getPlan().exigirActivo();
        aplicar(m, req);
        return metas.save(m);
    }

    @Transactional
    public MetaFinanciera cambiarEstadoMeta(int idMeta, boolean completada) {
        MetaFinanciera m = buscarMeta(idMeta);
        m.getPlan().exigirActivo();
        if (completada) m.marcarCompletada(LocalDate.now(reloj)); else m.marcarPendiente();
        return metas.save(m);
    }

    @Transactional
    public void eliminarMeta(int idMeta) {
        MetaFinanciera m = buscarMeta(idMeta);
        m.getPlan().exigirActivo();
        m.getPlan().quitarMeta(m); // orphanRemoval se encarga del DELETE
    }

    /** Id del cliente dueño de una meta (para validar permisos). */
    @Transactional(readOnly = true)
    public int idClienteDeMeta(int idMeta) {
        return buscarMeta(idMeta).getPlan().getCliente().getIdUsuario();
    }

    private MetaFinanciera nuevaMeta(MetaRequest req) {
        MetaFinanciera m = new MetaFinanciera();
        aplicar(m, req);
        return m;
    }

    /** Copia los datos del request a la meta y valida que las metas medibles traigan sus montos. */
    private void aplicar(MetaFinanciera m, MetaRequest req) {
        TipoMeta tipo = req.tipo() == null ? TipoMeta.TEXTO : req.tipo();
        if (tipo != TipoMeta.TEXTO && (req.montoObjetivo() == null || req.valorInicial() == null)) {
            throw new IllegalArgumentException("Una meta de tipo " + tipo + " requiere montoObjetivo y valorInicial.");
        }
        m.setDescripcion(req.descripcion());
        m.setFechaLimite(req.fechaLimite());
        m.setTipo(tipo);
        m.setMontoObjetivo(req.montoObjetivo());
        m.setValorInicial(req.valorInicial());
        if (m.getValorActual() == null) m.setValorActual(req.valorInicial());
    }
}
