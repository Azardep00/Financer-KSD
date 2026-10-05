package com.financerksd.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.financerksd.api.model.DiagnosticoFinanciero;

// Todo lo que el dashboard de progreso necesita en una sola llamada.
public record ProgresoResponse(
        Integer idCliente,
        List<PuntoHistorico> historial,   // ordenado de mas antiguo a mas reciente (listo para graficar)
        Variacion variacion,              // ultimo diagnostico - primer diagnostico (null si hay < 2)
        int metasCompletadas,
        int metasTotales,
        double progresoPlan) {

    public record PuntoHistorico(
            LocalDate fecha,
            BigDecimal ingresoMensual,
            BigDecimal gastoMensual,
            BigDecimal deudaTotal,
            BigDecimal ahorroActual,
            BigDecimal tasaAhorro,
            BigDecimal relacionDeudaIngreso,
            BigDecimal mesesFondoEmergencia) {

        public static PuntoHistorico desde(DiagnosticoFinanciero d) {
            return new PuntoHistorico(
                    d.getFecha(), d.getIngresoMensual(), d.getGastoMensual(), d.getDeudaTotal(),
                    d.getAhorroActual(), d.getTasaAhorro(), d.getRelacionDeudaIngreso(),
                    d.getMesesFondoEmergencia());
        }
    }

    public record Variacion(
            BigDecimal tasaAhorro,
            BigDecimal relacionDeudaIngreso,
            BigDecimal mesesFondoEmergencia,
            BigDecimal deudaTotal,
            BigDecimal ahorroActual) {

        public static Variacion entre(DiagnosticoFinanciero primero, DiagnosticoFinanciero ultimo) {
            return new Variacion(
                    ultimo.getTasaAhorro().subtract(primero.getTasaAhorro()),
                    ultimo.getRelacionDeudaIngreso().subtract(primero.getRelacionDeudaIngreso()),
                    ultimo.getMesesFondoEmergencia().subtract(primero.getMesesFondoEmergencia()),
                    ultimo.getDeudaTotal().subtract(primero.getDeudaTotal()),
                    ultimo.getAhorroActual().subtract(primero.getAhorroActual()));
        }
    }
}
