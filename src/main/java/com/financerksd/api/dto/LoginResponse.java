package com.financerksd.api.dto;

import com.financerksd.api.model.Usuario;

public record LoginResponse(
        Integer idUsuario, String nombre, String apellido, String correo, String tipoUsuario, String token) {

    public static LoginResponse desde(Usuario u) {
        return new LoginResponse(
                u.getIdUsuario(), u.getNombre(), u.getApellido(), u.getCorreo(), u.getTipoUsuario(), null);
    }

    public static LoginResponse desde(Usuario u, String token) {
        return new LoginResponse(
                u.getIdUsuario(), u.getNombre(), u.getApellido(), u.getCorreo(), u.getTipoUsuario(), token);
    }
}
