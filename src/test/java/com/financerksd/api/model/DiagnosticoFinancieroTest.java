package com.financerksd.api.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class DiagnosticoFinancieroTest {

    private DiagnosticoFinanciero diagnostico(String ingreso, String gasto, String deuda, String ahorro) {
        DiagnosticoFinanciero d = new DiagnosticoFinanciero();
        d.setIngresoMensual(new BigDecimal(ingreso));
        d.setGastoMensual(new BigDecimal(gasto));
        d.setDeudaTotal(new BigDecimal(deuda));
        d.setAhorroActual(new BigDecimal(ahorro));
        return d;
    }

    @Test
    void calculaMetricasClave() {
        DiagnosticoFinanciero d = diagnostico("4000000", "3000000", "12000000", "6000000");

        assertEquals(0, new BigDecimal("1000000").compareTo(d.getFlujoLibreMensual()));
        assertEquals(0, new BigDecimal("25").compareTo(d.getTasaAhorro()));          // 1M / 4M
        assertEquals(0, new BigDecimal("25").compareTo(d.getRelacionDeudaIngreso())); // 12M / 48M
        assertEquals(0, new BigDecimal("2.0").compareTo(d.getMesesFondoEmergencia())); // 6M / 3M
    }

    @Test
    void ingresoOGastoCeroNoDivideEntreCero() {
        DiagnosticoFinanciero d = diagnostico("0", "0", "500", "100");

        assertEquals(0, BigDecimal.ZERO.compareTo(d.getTasaAhorro()));
        assertEquals(0, BigDecimal.ZERO.compareTo(d.getRelacionDeudaIngreso()));
        assertEquals(0, BigDecimal.ZERO.compareTo(d.getMesesFondoEmergencia()));
    }
}
