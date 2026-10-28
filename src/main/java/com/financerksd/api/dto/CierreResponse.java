package com.financerksd.api.dto;

import com.financerksd.api.model.PlanMejora;

/** Resultado del cierre de mes: resumen del plan cerrado y (si se pidio) el plan del mes siguiente. */
public record CierreResponse(Resumen cierre, PlanMejora siguientePlan) {

    public record Resumen(int idPlan, String periodo, double progreso,
                          int metasTotal, int metasCumplidas, int metasPendientes, int metasArrastradas) {}
}
