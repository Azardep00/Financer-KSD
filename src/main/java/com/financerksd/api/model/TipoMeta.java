package com.financerksd.api.model;

/** Como se mide una meta. TEXTO = sin monto (se marca a mano, como siempre). */
public enum TipoMeta {
    TEXTO,
    AHORRO,           // el ahorro debe SUBIR hasta el monto objetivo
    REDUCCION_DEUDA,  // la deuda debe BAJAR hasta el monto objetivo
    REDUCCION_GASTO   // el gasto mensual debe BAJAR hasta el monto objetivo
}
