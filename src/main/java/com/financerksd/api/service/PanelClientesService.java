package com.financerksd.api.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.financerksd.api.dto.PanelResponse;
import com.financerksd.api.dto.PanelResponse.ClientePanel;
import com.financerksd.api.dto.PanelResponse.PlanResumen;
import com.financerksd.api.dto.PanelResponse.Resumen;
import com.financerksd.api.model.Asesor;
import com.financerksd.api.model.Cliente;
import com.financerksd.api.model.EstadoCliente;
import com.financerksd.api.model.EstadoPlan;
import com.financerksd.api.model.PlanMejora;
import com.financerksd.api.repository.ClienteRepository;
import com.financerksd.api.repository.DiagnosticoFinancieroRepository;
import com.financerksd.api.repository.NotaAsesorRepository;
import com.financerksd.api.repository.PlanMejoraRepository;

// Panel del asesor. Trae los clientes del alcance elegido y completa planes, diagnosticos y notas con
// UNA consulta por tabla (no una por cliente). Busqueda, orden y paginacion se hacen en memoria:
// pensado para cientos o pocos miles de clientes. Si crecen mucho, mover filtros y paginacion a la BD.
@Service
public class PanelClientesService {

    public static final int TAMANO_MAXIMO = 100;

    private final ClienteRepository clientes;
    private final PlanMejoraRepository planes;
    private final DiagnosticoFinancieroRepository diagnosticos;
    private final NotaAsesorRepository notas;
    private final Clock reloj;

    public PanelClientesService(ClienteRepository clientes, PlanMejoraRepository planes,
                                DiagnosticoFinancieroRepository diagnosticos, NotaAsesorRepository notas, Clock reloj) {
        this.clientes = clientes;
        this.planes = planes;
        this.diagnosticos = diagnosticos;
        this.notas = notas;
        this.reloj = reloj;
    }

    /** orden: "alertas" (por defecto: mas alertas primero) | "nombre" | "diagnostico" (el mas antiguo primero). */
    @Transactional(readOnly = true)
    public PanelResponse panel(int idAsesorActual, String q, EstadoCliente estado, boolean mios, boolean sinAsesor,
                               boolean soloConAlertas, String orden, int pagina, int tamano) {
        if (mios && sinAsesor) {
            throw new IllegalArgumentException("Usa 'mios' o 'sinAsesor', no los dos a la vez.");
        }
        if (pagina < 0) throw new IllegalArgumentException("La página no puede ser negativa.");
        if (tamano < 1 || tamano > TAMANO_MAXIMO) {
            throw new IllegalArgumentException("El tamaño debe estar entre 1 y " + TAMANO_MAXIMO + ".");
        }
        LocalDate hoy = LocalDate.now(reloj);

        List<Cliente> alcance = mios ? clientes.findByAsesorAsignado_IdUsuario(idAsesorActual)
                : sinAsesor ? clientes.findByAsesorAsignadoIsNull()
                : clientes.findAll();

        // Una consulta por tabla, indexadas por id de cliente.
        Map<Integer, PlanMejora> planActivo = new HashMap<>();
        for (PlanMejora p : planes.findByEstado(EstadoPlan.ACTIVO)) {
            planActivo.merge(p.getCliente().getIdUsuario(), p, (a, b) -> esMasReciente(a, b) ? a : b);
        }
        Map<Integer, LocalDate> ultimoDiag = new HashMap<>();
        for (Object[] f : diagnosticos.ultimaFechaPorCliente()) {
            ultimoDiag.put(((Number) f[0]).intValue(), (LocalDate) f[1]);
        }
        Map<Integer, Long> cuentaNotas = new HashMap<>();
        for (Object[] f : notas.contarPorCliente()) {
            cuentaNotas.put(((Number) f[0]).intValue(), ((Number) f[1]).longValue());
        }

        List<ClientePanel> filas = alcance.stream()
                .map(c -> fila(c, planActivo.get(c.getIdUsuario()), ultimoDiag.get(c.getIdUsuario()),
                        cuentaNotas.getOrDefault(c.getIdUsuario(), 0L), hoy))
                .toList();

        Resumen resumen = resumen(filas);

        Comparator<ClientePanel> porNombre = Comparator
                .comparing((ClientePanel f) -> PanelFiltros.normalizar(f.apellido()))
                .thenComparing(f -> PanelFiltros.normalizar(f.nombre()));
        Comparator<ClientePanel> comparador = switch (orden == null ? "alertas" : orden) {
            case "nombre" -> porNombre;
            case "diagnostico" -> Comparator.comparing(
                    (ClientePanel f) -> f.ultimoDiagnostico() == null ? LocalDate.MIN : f.ultimoDiagnostico()).thenComparing(porNombre);
            case "alertas" -> Comparator.comparingInt((ClientePanel f) -> -f.alertas().size()).thenComparing(porNombre);
            default -> throw new IllegalArgumentException("orden debe ser alertas, nombre o diagnostico.");
        };

        List<ClientePanel> filtradas = filas.stream()
                .filter(f -> estado == null || f.estadoCliente() == estado)
                .filter(f -> !soloConAlertas || !f.alertas().isEmpty())
                .filter(f -> PanelFiltros.coincide(q, f.nombre(), f.apellido(), f.correo(), f.telefono()))
                .sorted(comparador)
                .toList();

        long total = filtradas.size();
        int desde = (int) Math.min((long) pagina * tamano, total);
        int hasta = (int) Math.min((long) desde + tamano, total);
        int totalPaginas = (int) ((total + tamano - 1) / tamano);
        return new PanelResponse(resumen, filtradas.subList(desde, hasta), pagina, tamano, total, totalPaginas);
    }

    /** Compara por mes del plan. Tolera periodoInicio nulo (plan viejo si la migracion de datos no corrio). */
    private static boolean esMasReciente(PlanMejora a, PlanMejora b) {
        LocalDate x = a.getPeriodoInicio();
        LocalDate y = b.getPeriodoInicio();
        if (x == null) return false;
        if (y == null) return true;
        return x.isAfter(y);
    }

    private ClientePanel fila(Cliente c, PlanMejora plan, LocalDate ultimoDiagnostico, long cantidadNotas, LocalDate hoy) {
        long vencidas = plan == null ? 0 : plan.metasVencidas(hoy);
        PlanResumen resumenPlan = plan == null ? null : new PlanResumen(plan.getIdPlan(), plan.getTitulo(), plan.getPeriodo(),
                plan.getProgreso(), plan.getMetas().size(), plan.metasPendientes().size(), vencidas);
        Asesor a = c.getAsesorAsignado();
        return new ClientePanel(c.getIdUsuario(), c.getNombre(), c.getApellido(), c.getCorreo(), c.getTelefono(),
                c.getEstadoCliente(), a == null ? null : a.getIdUsuario(),
                a == null ? null : a.getNombre() + " " + a.getApellido(),
                resumenPlan, ultimoDiagnostico,
                ultimoDiagnostico == null ? null : ChronoUnit.DAYS.between(ultimoDiagnostico, hoy),
                cantidadNotas,
                AlertasCliente.calcular(c.getEstadoCliente(), ultimoDiagnostico, plan != null,
                        plan == null ? null : plan.getPeriodoFin(), plan == null ? null : plan.getPeriodo(), vencidas, hoy));
    }

    private Resumen resumen(List<ClientePanel> filas) {
        Map<EstadoCliente, Long> porEstado = new EnumMap<>(EstadoCliente.class);
        for (EstadoCliente e : EstadoCliente.values()) porEstado.put(e, 0L);
        Map<String, Long> porTipo = new TreeMap<>();
        long conAlertas = 0, sinAsesor = 0;
        for (ClientePanel f : filas) {
            porEstado.merge(f.estadoCliente(), 1L, Long::sum);
            if (f.idAsesor() == null) sinAsesor++;
            if (!f.alertas().isEmpty()) conAlertas++;
            f.alertas().forEach(al -> porTipo.merge(al.tipo().name(), 1L, Long::sum));
        }
        return new Resumen(filas.size(), porEstado, conAlertas, sinAsesor, porTipo);
    }
}
