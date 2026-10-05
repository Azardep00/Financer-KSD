package com.financerksd.api.service;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.financerksd.api.exception.AccesoDenegadoException;

// Reglas de "dueño del recurso": un asesor puede ver a cualquier cliente;
// un cliente solo puede ver y modificar lo suyo.
@Service
public class AutorizacionService {

    public int idActual(Authentication auth) {
        return Integer.parseInt(String.valueOf(auth.getPrincipal()));
    }

    public boolean esAsesor(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ASESOR"));
    }

    public void exigirAccesoACliente(Authentication auth, int idCliente) {
        if (!esAsesor(auth) && idActual(auth) != idCliente) {
            throw new AccesoDenegadoException("No tienes permiso para acceder a la información de otro cliente.");
        }
    }
}
