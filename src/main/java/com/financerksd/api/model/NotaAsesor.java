package com.financerksd.api.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;

// Nota privada que un asesor escribe sobre un cliente. El cliente NUNCA debe verlas
// (los endpoints que las exponen se protegen en un checkpoint posterior).
@Entity
@Table(name = "notas_asesor")
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotaAsesor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idNota;

    @ManyToOne(optional = false)
    @JsonIgnore
    private Cliente cliente;

    @ManyToOne(optional = false)
    private Asesor asesor;

    @Column(nullable = false, length = 2000)
    private String texto;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    public Integer getIdNota() { return idNota; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public Asesor getAsesor() { return asesor; }
    public void setAsesor(Asesor asesor) { this.asesor = asesor; }
    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
}
