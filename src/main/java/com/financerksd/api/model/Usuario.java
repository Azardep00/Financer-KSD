package com.financerksd.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import jakarta.persistence.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

// Usuario es la base; Asesor y Cliente son las dos variantes reales.
// EXISTING_PROPERTY: el JSON usa "tipoUsuario" (que sale del getter) sin duplicarlo.
@Entity
@Table(name = "usuarios")
@Inheritance(strategy = InheritanceType.JOINED)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "tipoUsuario")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Asesor.class, name = "Asesor"),
        @JsonSubTypes.Type(value = Cliente.class, name = "Cliente"),
})
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Usuario {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idUsuario;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @Column(nullable = false, unique = true)
    private String correo;

    @Column(nullable = false)
    private String contrasenaHash;

    @Column(nullable = false)
    private boolean estado = true;

    public abstract String getTipoUsuario();

    public void establecerContrasena(String plano) {
        this.contrasenaHash = ENCODER.encode(plano);
    }

    public boolean verificarContrasena(String plano) {
        return ENCODER.matches(plano, this.contrasenaHash);
    }

    public void cambiarContrasena(String actualPlano, String nuevaPlano) {
        if (!verificarContrasena(actualPlano)) {
            throw new IllegalArgumentException("La contraseña actual no es correcta.");
        }
        establecerContrasena(nuevaPlano);
    }

    public Integer getIdUsuario() { return idUsuario; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public boolean isEstado() { return estado; }
    public void setEstado(boolean estado) { this.estado = estado; }
}
