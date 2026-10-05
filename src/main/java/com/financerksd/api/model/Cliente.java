package com.financerksd.api.model;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "clientes")
public class Cliente extends Usuario {

    private String telefono;

    // Asesor que guia a este cliente. Puede ser null al registrarse.
    @ManyToOne
    private Asesor asesorAsignado;

    @Override
    public String getTipoUsuario() { return "Cliente"; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public Asesor getAsesorAsignado() { return asesorAsignado; }
    public void setAsesorAsignado(Asesor asesorAsignado) { this.asesorAsignado = asesorAsignado; }
}
