package com.financerksd.api.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;

// Plan que el asesor arma para un cliente: titulo + lista de metas.
// El plan activo es el mas reciente (los anteriores quedan como historial).
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
    public List<MetaFinanciera> getMetas() { return metas; }

    public void agregarMeta(MetaFinanciera meta) {
        meta.setPlan(this);
        metas.add(meta);
    }

    public void quitarMeta(MetaFinanciera meta) {
        metas.remove(meta);
    }

    /** % de metas completadas (para la barra de progreso). */
    public double getProgreso() {
        if (metas.isEmpty()) return 0;
        long completadas = metas.stream().filter(MetaFinanciera::isCompletada).count();
        return (completadas * 100.0) / metas.size();
    }
}
