package com.financerksd.api.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;

// Plan que el asesor arma para un cliente: titulo + lista de metas.
// Cada plan corresponde a un mes (periodoInicio/periodoFin) y tiene estado: ACTIVO, CERRADO o CANCELADO.
@Entity
@Table(name = "planes_mejora")
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlanMejora {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idPlan;

    @ManyToOne(optional = false)
    private Cliente cliente;

    @ManyToOne(optional = false)
    private Asesor asesor;

    @Column(nullable = false)
    private String titulo;

    private String descripcion;

    @Column(nullable = false)
    private LocalDate fechaCreacion = LocalDate.now();

    // ---- Plan mensual (columnas nullable: los planes viejos se llenan con la migracion inicial) ----
    private LocalDate periodoInicio = LocalDate.now().withDayOfMonth(1);

    private LocalDate periodoFin = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

    @Enumerated(EnumType.STRING)
    private EstadoPlan estado = EstadoPlan.ACTIVO;

    private LocalDate fechaCierre;

    @Column(length = 1000)
    private String notaCierre;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MetaFinanciera> metas = new ArrayList<>();

    public Integer getIdPlan() { return idPlan; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public Asesor getAsesor() { return asesor; }
    public void setAsesor(Asesor asesor) { this.asesor = asesor; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public LocalDate getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDate fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public List<MetaFinanciera> getMetas() { return metas; }

    public LocalDate getPeriodoInicio() { return periodoInicio; }
    public LocalDate getPeriodoFin() { return periodoFin; }
    public EstadoPlan getEstado() { return estado == null ? EstadoPlan.ACTIVO : estado; }
    public void setEstado(EstadoPlan estado) { this.estado = estado; }
    public LocalDate getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(LocalDate fechaCierre) { this.fechaCierre = fechaCierre; }
    public String getNotaCierre() { return notaCierre; }
    public void setNotaCierre(String notaCierre) { this.notaCierre = notaCierre; }

    /** Mes del plan en formato yyyy-MM (para mostrar en el frontend). */
    public String getPeriodo() {
        LocalDate base = periodoInicio != null ? periodoInicio : fechaCreacion;
        return base.format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }

    /** Fija el mes del plan a partir de cualquier dia de ese mes. */
    public void definirPeriodo(LocalDate cualquierDiaDelMes) {
        this.periodoInicio = cualquierDiaDelMes.withDayOfMonth(1);
        this.periodoFin = cualquierDiaDelMes.withDayOfMonth(cualquierDiaDelMes.lengthOfMonth());
    }

    /** Un plan cerrado o cancelado es de solo lectura. */
    public void exigirActivo() {
        if (getEstado() != EstadoPlan.ACTIVO) {
            throw new IllegalArgumentException("El plan de " + getPeriodo() + " está "
                    + getEstado().name().toLowerCase() + " y ya no se puede modificar.");
        }
    }

    /** Metas sin cumplir cuya fecha limite ya paso. */
    public long metasVencidas(LocalDate hoy) {
        return metas.stream()
                .filter(m -> !m.isCompletada() && m.getFechaLimite() != null && m.getFechaLimite().isBefore(hoy))
                .count();
    }

    public List<MetaFinanciera> metasPendientes() {
        return metas.stream().filter(m -> !m.isCompletada()).toList();
    }

    /** Cierre de mes. */
    public void cerrar(String nota, LocalDate hoy) {
        exigirActivo();
        this.estado = EstadoPlan.CERRADO;
        this.fechaCierre = hoy;
        this.notaCierre = nota;
    }

    /** Se descarta el plan antes de que termine el mes. */
    public void cancelar(String motivo, LocalDate hoy) {
        exigirActivo();
        this.estado = EstadoPlan.CANCELADO;
        this.fechaCierre = hoy;
        this.notaCierre = motivo;
    }

    /** Primer dia del mes siguiente al de este plan. */
    public LocalDate inicioSiguientePeriodo() {
        LocalDate base = periodoInicio != null ? periodoInicio : fechaCreacion.withDayOfMonth(1);
        return base.plusMonths(1);
    }

    /** Arma (sin guardar) el plan del mes siguiente, con las metas pendientes si arrastrar=true. */
    public PlanMejora crearSiguiente(String tituloNuevo, boolean arrastrarPendientes, LocalDate hoy) {
        PlanMejora p = new PlanMejora();
        p.cliente = cliente;
        p.asesor = asesor;
        p.fechaCreacion = hoy;
        p.definirPeriodo(inicioSiguientePeriodo());
        p.titulo = (tituloNuevo == null || tituloNuevo.isBlank()) ? "Plan " + p.getPeriodo() : tituloNuevo;
        if (arrastrarPendientes) {
            for (MetaFinanciera m : metasPendientes()) {
                p.agregarMeta(m.copiarParaSiguientePlan(p.periodoInicio, p.periodoFin));
            }
        }
        return p;
    }

    /** Sincroniza las metas medibles con un diagnostico. Devuelve cuantas cambiaron. */
    public int sincronizarConDiagnostico(DiagnosticoFinanciero d, LocalDate hoy) {
        int n = 0;
        for (MetaFinanciera m : metas) if (m.sincronizarCon(d, hoy)) n++;
        return n;
    }

    public void agregarMeta(MetaFinanciera meta) {
        meta.setPlan(this);
        metas.add(meta);
    }

    public void quitarMeta(MetaFinanciera meta) {
        metas.remove(meta);
    }

    /** Promedio del avance de las metas (las de tipo TEXTO cuentan 0 o 100, igual que antes). */
    public double getProgreso() {
        if (metas.isEmpty()) return 0;
        return metas.stream().mapToDouble(MetaFinanciera::getProgreso).average().orElse(0);
    }
}
