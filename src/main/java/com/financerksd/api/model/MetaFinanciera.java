package com.financerksd.api.model;

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

    public Integer getIdMeta() { return idMeta; }
    public PlanMejora getPlan() { return plan; }
    public void setPlan(PlanMejora plan) { this.plan = plan; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public LocalDate getFechaLimite() { return fechaLimite; }
    public void setFechaLimite(LocalDate fechaLimite) { this.fechaLimite = fechaLimite; }
    public boolean isCompletada() { return completada; }
    public LocalDate getFechaCompletada() { return fechaCompletada; }

    public void marcarCompletada() {
        this.completada = true;
        this.fechaCompletada = LocalDate.now();
    }

    public void marcarPendiente() {
        this.completada = false;
        this.fechaCompletada = null;
    }
}
