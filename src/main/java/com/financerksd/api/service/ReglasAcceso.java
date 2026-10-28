package com.financerksd.api.service;

// Reglas de permiso en Java puro (sin Spring) para poder probarlas con tests unitarios.
public final class ReglasAcceso {

    private ReglasAcceso() {}

    /** Un asesor puede escribir sobre un cliente si es SU asesor asignado, o si el cliente aun no tiene asesor. */
    public static boolean asesorResponsable(Integer idAsesorAsignado, int idAsesorActual) {
        return idAsesorAsignado == null || idAsesorAsignado == idAsesorActual;
    }

    /** Escribir sobre los datos de un cliente: el propio cliente, o su asesor responsable. */
    public static boolean puedeEscribir(boolean esAsesor, int idActual, int idCliente, Integer idAsesorAsignado) {
        return esAsesor ? asesorResponsable(idAsesorAsignado, idActual) : idActual == idCliente;
    }
}
