package com.financerksd.api.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "clientes")
public class Cliente extends Usuario {

    private String telefono;

    // Asesor que guia a este cliente. Puede ser null al registrarse.
    @ManyToOne
    private Asesor asesorAsignado;

    // Columna nullable a proposito: los clientes que ya existian en la BD no la tienen.
    // El getter devuelve PROSPECTO si esta vacia y la migracion inicial la llena bien.
    @Enumerated(EnumType.STRING)
    private EstadoCliente estadoCliente = EstadoCliente.PROSPECTO;

    @Override
    public String getTipoUsuario() { return "Cliente"; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public EstadoCliente getEstadoCliente() { return estadoCliente == null ? EstadoCliente.PROSPECTO : estadoCliente; }
    public void setEstadoCliente(EstadoCliente estadoCliente) { this.estadoCliente = estadoCliente; }
    public Asesor getAsesorAsignado() { return asesorAsignado; }
    public void setAsesorAsignado(Asesor asesorAsignado) { this.asesorAsignado = asesorAsignado; }
}
