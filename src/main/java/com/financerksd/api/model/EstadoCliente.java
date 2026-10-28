package com.financerksd.api.model;

public enum EstadoCliente {
    PROSPECTO,   // se registro pero aun no empieza el proceso
    ACTIVO,      // en acompanamiento
    PAUSADO,     // detenido temporalmente
    FINALIZADO   // termino el proceso
}
