package com.financerksd.api.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;

// "Foto" de la situacion financiera de un cliente en un momento dado.
// Se guarda historico (no se sobreescribe) para poder medir el progreso.
@Entity
@Table(name = "diagnosticos_financieros")
@JsonIgnoreProperties(ignoreUnknown = true)
public class DiagnosticoFinanciero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idDiagnostico;

    @ManyToOne(optional = false)
    private Cliente cliente;

    @Column(nullable = false)
    private LocalDate fecha = LocalDate.now();

    @Column(nullable = false)
    private BigDecimal ingresoMensual;

    @Column(nullable = false)
    private BigDecimal gastoMensual;

    @Column(nullable = false)
    private BigDecimal deudaTotal;

    @Column(nullable = false)
    private BigDecimal ahorroActual;

    public Integer getIdDiagnostico() { return idDiagnostico; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public BigDecimal getIngresoMensual() { return ingresoMensual; }
    public void setIngresoMensual(BigDecimal v) { this.ingresoMensual = v; }
    public BigDecimal getGastoMensual() { return gastoMensual; }
    public void setGastoMensual(BigDecimal v) { this.gastoMensual = v; }
    public BigDecimal getDeudaTotal() { return deudaTotal; }
    public void setDeudaTotal(BigDecimal v) { this.deudaTotal = v; }
    public BigDecimal getAhorroActual() { return ahorroActual; }
    public void setAhorroActual(BigDecimal v) { this.ahorroActual = v; }

    // ---------- Metricas derivadas (se calculan al vuelo, no se guardan) ----------

    /** Lo que le queda libre cada mes despues de gastos. */
    public BigDecimal getFlujoLibreMensual() {
        return ingresoMensual.subtract(gastoMensual);
    }

    /** % del ingreso que logra ahorrar cada mes. */
    public BigDecimal getTasaAhorro() {
        if (ingresoMensual.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return getFlujoLibreMensual()
                .divide(ingresoMensual, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /** Deuda / ingreso ANUAL en %. Sobre 36% suele ser zona de alerta. */
    public BigDecimal getRelacionDeudaIngreso() {
        BigDecimal ingresoAnual = ingresoMensual.multiply(BigDecimal.valueOf(12));
        if (ingresoAnual.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return deudaTotal
                .divide(ingresoAnual, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /** Cuantos meses de gastos cubre el ahorro (fondo de emergencia). */
    public BigDecimal getMesesFondoEmergencia() {
        if (gastoMensual.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return ahorroActual.divide(gastoMensual, 1, RoundingMode.HALF_UP);
    }
}
