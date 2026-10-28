package com.financerksd.api.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class PlanMetasVencidasTest {

    private static final LocalDate HOY = LocalDate.of(2026, 10, 28);

    private MetaFinanciera meta(LocalDate limite, boolean hecha) {
        MetaFinanciera m = new MetaFinanciera();
        m.setDescripcion("meta");
        m.setFechaLimite(limite);
        if (hecha) m.marcarCompletada(HOY);
        return m;
    }

    @Test
    void cuentaSoloLasPendientesConFechaYaPasada() {
        PlanMejora p = new PlanMejora();
        p.agregarMeta(meta(HOY.minusDays(1), false));   // vencida
        p.agregarMeta(meta(HOY.minusDays(10), false));  // vencida
        p.agregarMeta(meta(HOY.minusDays(5), true));    // vencida pero ya cumplida: no cuenta
        p.agregarMeta(meta(HOY, false));                // vence hoy: todavia no esta vencida
        p.agregarMeta(meta(HOY.plusDays(3), false));    // a tiempo
        p.agregarMeta(meta(null, false));               // sin fecha limite
        assertEquals(2, p.metasVencidas(HOY));
    }

    @Test
    void planSinMetasNoTieneVencidas() {
        assertEquals(0, new PlanMejora().metasVencidas(HOY));
    }
}
