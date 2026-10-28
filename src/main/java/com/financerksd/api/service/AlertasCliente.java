package com.financerksd.api.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import com.financerksd.api.model.EstadoCliente;

// Reglas de alertas del panel en Java puro (sin Spring) para poder probarlas.
// Solo se alerta sobre clientes ACTIVOS: un prospecto, un pausado o uno finalizado no "se atrasan".
public final class AlertasCliente {

    public static final int DIAS_DIAGNOSTICO_VIEJO = 30;

    public enum Tipo { SIN_DIAGNOSTICO, DIAGNOSTICO_VIEJO, SIN_PLAN_ACTIVO, MES_SIN_CERRAR, METAS_VENCIDAS }

    public record Alerta(Tipo tipo, String mensaje) {}

    private AlertasCliente() {}

    public static List<Alerta> calcular(EstadoCliente estado, LocalDate ultimoDiagnostico, boolean tienePlanActivo,
                                        LocalDate finPeriodoPlan, String periodoPlan, long metasVencidas, LocalDate hoy) {
        List<Alerta> alertas = new ArrayList<>();
        if (estado != EstadoCliente.ACTIVO) return alertas;

        if (ultimoDiagnostico == null) {
            alertas.add(new Alerta(Tipo.SIN_DIAGNOSTICO, "Aún no tiene diagnóstico."));
        } else {
            long dias = ChronoUnit.DAYS.between(ultimoDiagnostico, hoy);
            if (dias >= DIAS_DIAGNOSTICO_VIEJO) {
                alertas.add(new Alerta(Tipo.DIAGNOSTICO_VIEJO, "Sin diagnóstico hace " + dias + " días."));
            }
        }
        if (!tienePlanActivo) {
            alertas.add(new Alerta(Tipo.SIN_PLAN_ACTIVO, "No tiene un plan activo."));
        } else {
            if (finPeriodoPlan != null && finPeriodoPlan.isBefore(hoy)) {
                alertas.add(new Alerta(Tipo.MES_SIN_CERRAR, "El plan de " + periodoPlan + " ya terminó y no se ha cerrado."));
            }
            if (metasVencidas > 0) {
                alertas.add(new Alerta(Tipo.METAS_VENCIDAS,
                        metasVencidas + (metasVencidas == 1 ? " meta vencida." : " metas vencidas.")));
            }
        }
        return alertas;
    }
}
