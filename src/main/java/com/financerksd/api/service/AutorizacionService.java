package com.financerksd.api.service;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.financerksd.api.exception.AccesoDenegadoException;
import com.financerksd.api.model.Asesor;

// Reglas de "dueño del recurso":
//  - LECTURA: un asesor puede ver a cualquier cliente; un cliente solo lo suyo.
//  - ESCRITURA de asesor: solo sobre clientes asignados a el (o que aun no tienen asesor).
@Service
public class AutorizacionService {

    private final ClienteService clientes;

    public AutorizacionService(ClienteService clientes) {
        this.clientes = clientes;
    }

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

    /** Solo el asesor asignado al cliente (o cualquiera si todavia no tiene asesor). */
    public void exigirAsesorResponsable(Authentication auth, int idCliente) {
        if (!esAsesor(auth)) {
            throw new AccesoDenegadoException("Solo un asesor puede hacer esto.");
        }
        Asesor asignado = clientes.buscar(idCliente).getAsesorAsignado();
        Integer idAsignado = asignado == null ? null : asignado.getIdUsuario();
        if (!ReglasAcceso.asesorResponsable(idAsignado, idActual(auth))) {
            throw new AccesoDenegadoException("Este cliente está asignado a otro asesor.");
        }
    }

    /** Modificar datos del cliente: el propio cliente (sus metas) o su asesor responsable. */
    public void exigirEscritura(Authentication auth, int idCliente) {
        if (esAsesor(auth)) exigirAsesorResponsable(auth, idCliente);
        else exigirAccesoACliente(auth, idCliente);
    }
}
