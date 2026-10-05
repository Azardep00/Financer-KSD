package com.financerksd.api.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "asesores")
public class Asesor extends Usuario {

    private String especialidad; // ej: "Deudas", "Ahorro e inversion", "General"

    @Override
    public String getTipoUsuario() { return "Asesor"; }

    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }
}
