package com.financerksd.api.model;

/** Ciclo de vida de un plan mensual. */
public enum EstadoPlan {
    ACTIVO,     // plan vigente del cliente (solo uno a la vez)
    CERRADO,    // el mes termino y se hizo el cierre
    CANCELADO   // se descarto antes de terminar
}
