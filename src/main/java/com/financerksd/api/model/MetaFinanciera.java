package com.financerksd.api.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;

// Tarea/hito concreto dentro de un PlanMejora.
// Ej: "Crear fondo de emergencia de 3 meses", "Reducir gastos hormiga en 15%".
@Entity
@Table(name = "metas_financieras")
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetaFinanciera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idMeta;

    @ManyToOne(optional = false)
    @JsonIgnore // evita el loop plan -> metas -> plan y aligera el JSON
    private PlanMejora plan;

    @Column(nullable = false)
    private String descripcion;

    private LocalDate fechaLimite;

    @Column(nullable = false)
    private boolean completada = false;

    private LocalDate fechaCompletada;

    // ---- Meta medible (todas nullable: las metas viejas siguen funcionando como TEXTO) ----
    @Enumerated(EnumType.STRING)
    private TipoMeta tipo = TipoMeta.TEXTO;

    @Column(precision = 15, scale = 2)
    private BigDecimal montoObjetivo;

    /** Valor de la metrica cuando se creo la meta (punto de partida). */
    @Column(precision = 15, scale = 2)
    private BigDecimal valorInicial;

    /** Ultimo valor conocido (se sincroniza con el diagnostico). */
    @Column(precision = 15, scale = 2)
    private BigDecimal valorActual;

    /** Si la meta se arrastro desde un plan cerrado, id de la meta original. */
    private Integer origenMetaId;

    public Integer getIdMeta() { return idMeta; }
    public PlanMejora getPlan() { return plan; }
    public void setPlan(PlanMejora plan) { this.plan = plan; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public LocalDate getFechaLimite() { return fechaLimite; }
    public void setFechaLimite(LocalDate fechaLimite) { this.fechaLimite = fechaLimite; }
    public boolean isCompletada() { return completada; }
    public LocalDate getFechaCompletada() { return fechaCompletada; }

    public TipoMeta getTipo() { return tipo == null ? TipoMeta.TEXTO : tipo; }
    public void setTipo(TipoMeta tipo) { this.tipo = tipo; }
    public BigDecimal getMontoObjetivo() { return montoObjetivo; }
    public void setMontoObjetivo(BigDecimal montoObjetivo) { this.montoObjetivo = montoObjetivo; }
    public BigDecimal getValorInicial() { return valorInicial; }
    public void setValorInicial(BigDecimal valorInicial) { this.valorInicial = valorInicial; }
    public BigDecimal getValorActual() { return valorActual; }
    public void setValorActual(BigDecimal valorActual) { this.valorActual = valorActual; }
    public Integer getOrigenMetaId() { return origenMetaId; }
    public void setOrigenMetaId(Integer origenMetaId) { this.origenMetaId = origenMetaId; }

    /** True si tiene tipo con monto y los datos para calcular avance. */
    public boolean isMedible() {
        return getTipo() != TipoMeta.TEXTO && montoObjetivo != null && valorInicial != null;
    }

    /**
     * Avance 0-100. TEXTO (o sin datos): 0 o 100 segun este marcada.
     * AHORRO sube de valorInicial a montoObjetivo; las REDUCCION_* bajan de valorInicial a montoObjetivo.
     */
    public double getProgreso() {
        if (completada) return 100;
        if (!isMedible()) return 0;
        BigDecimal actual = valorActual != null ? valorActual : valorInicial;
        BigDecimal recorrido, total;
        if (getTipo() == TipoMeta.AHORRO) {
            recorrido = actual.subtract(valorInicial);
            total = montoObjetivo.subtract(valorInicial);
        } else {
            recorrido = valorInicial.subtract(actual);
            total = valorInicial.subtract(montoObjetivo);
        }
        if (total.signum() <= 0) return 0; // objetivo incoherente con el punto de partida
        double pct = recorrido.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP).doubleValue();
        return Math.max(0, Math.min(100, pct));
    }

    public void marcarCompletada() {
        marcarCompletada(LocalDate.now());
    }

    public void marcarCompletada(LocalDate hoy) {
        this.completada = true;
        this.fechaCompletada = hoy;
    }

    /**
     * Actualiza valorActual con el diagnostico segun el tipo de meta. Si con eso llega al 100% la marca
     * como cumplida. Devuelve true si algo cambio. Las metas TEXTO o ya completadas no se tocan.
     */
    public boolean sincronizarCon(DiagnosticoFinanciero d, LocalDate hoy) {
        if (completada || !isMedible()) return false;
        BigDecimal nuevo = switch (getTipo()) {
            case AHORRO -> d.getAhorroActual();
            case REDUCCION_DEUDA -> d.getDeudaTotal();
            case REDUCCION_GASTO -> d.getGastoMensual();
            default -> null;
        };
        if (nuevo == null) return false;
        boolean cambio = valorActual == null || valorActual.compareTo(nuevo) != 0;
        valorActual = nuevo;
        if (getProgreso() >= 100) {
            marcarCompletada(hoy);
            cambio = true;
        }
        return cambio;
    }

    /**
     * Copia de esta meta para el plan del mes siguiente (queda pendiente y recuerda su origen).
     * Conserva valorInicial para que el avance siga contando desde el punto de partida real.
     * Si la fecha limite ya quedo antes del nuevo mes, pasa al ultimo dia del nuevo mes.
     */
    public MetaFinanciera copiarParaSiguientePlan(LocalDate inicioNuevo, LocalDate finNuevo) {
        MetaFinanciera c = new MetaFinanciera();
        c.descripcion = descripcion;
        c.tipo = getTipo();
        c.montoObjetivo = montoObjetivo;
        c.valorInicial = valorInicial;
        c.valorActual = valorActual;
        c.origenMetaId = origenMetaId != null ? origenMetaId : idMeta;
        c.fechaLimite = (fechaLimite != null && fechaLimite.isBefore(inicioNuevo)) ? finNuevo : fechaLimite;
        return c;
    }

    public void marcarPendiente() {
        this.completada = false;
        this.fechaCompletada = null;
    }
}
