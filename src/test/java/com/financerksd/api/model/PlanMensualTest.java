package com.financerksd.api.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class PlanMensualTest {

    private MetaFinanciera medible(TipoMeta tipo, String inicial, String objetivo, String actual) {
        MetaFinanciera m = new MetaFinanciera();
        m.setDescripcion("meta");
        m.setTipo(tipo);
        m.setValorInicial(new BigDecimal(inicial));
        m.setMontoObjetivo(new BigDecimal(objetivo));
        m.setValorActual(actual == null ? null : new BigDecimal(actual));
        return m;
    }

    @Test
    void metaSinTipoSeComportaComoAntes() {
        MetaFinanciera m = new MetaFinanciera(); // meta "vieja": sin tipo ni montos
        assertEquals(TipoMeta.TEXTO, m.getTipo());
        assertFalse(m.isMedible());
        assertEquals(0, m.getProgreso());
        m.marcarCompletada();
        assertEquals(100, m.getProgreso());
    }

    @Test
    void ahorroSubeHaciaElObjetivo() {
        // de 1M a 3M, va en 2M => 50%
        assertEquals(50.0, medible(TipoMeta.AHORRO, "1000000", "3000000", "2000000").getProgreso(), 0.01);
    }

    @Test
    void deudaYGastoBajanHaciaElObjetivo() {
        // deuda de 10M a 4M, va en 7M => 50%
        assertEquals(50.0, medible(TipoMeta.REDUCCION_DEUDA, "10000000", "4000000", "7000000").getProgreso(), 0.01);
        assertEquals(100.0, medible(TipoMeta.REDUCCION_GASTO, "3000000", "2500000", "2400000").getProgreso(), 0.01); // se pasa: tope 100
    }

    @Test
    void sinValorActualElAvanceEsCero_yNuncaNegativo() {
        assertEquals(0, medible(TipoMeta.AHORRO, "1000000", "3000000", null).getProgreso());
        assertEquals(0, medible(TipoMeta.AHORRO, "1000000", "3000000", "500000").getProgreso()); // retrocedio
    }

    @Test
    void objetivoIncoherenteNoRompeNiDivideEntreCero() {
        assertEquals(0, medible(TipoMeta.AHORRO, "3000000", "3000000", "4000000").getProgreso());
        assertEquals(0, medible(TipoMeta.REDUCCION_DEUDA, "1000000", "5000000", "500000").getProgreso());
    }

    @Test
    void progresoDelPlanEsElPromedioDeSusMetas() {
        PlanMejora plan = new PlanMejora();
        plan.agregarMeta(medible(TipoMeta.AHORRO, "0", "100", "50"));   // 50
        MetaFinanciera texto = new MetaFinanciera();
        texto.setDescripcion("texto");
        texto.marcarCompletada();                                        // 100
        plan.agregarMeta(texto);
        assertEquals(75.0, plan.getProgreso(), 0.01);
    }

    @Test
    void periodoDelPlan() {
        PlanMejora plan = new PlanMejora();
        plan.definirPeriodo(LocalDate.of(2026, 2, 17));
        assertEquals(LocalDate.of(2026, 2, 1), plan.getPeriodoInicio());
        assertEquals(LocalDate.of(2026, 2, 28), plan.getPeriodoFin());
        assertEquals("2026-02", plan.getPeriodo());
        assertEquals(EstadoPlan.ACTIVO, plan.getEstado());
    }

    @Test
    void clienteNuevoEsProspecto() {
        assertEquals(EstadoCliente.PROSPECTO, new Cliente().getEstadoCliente());
        assertTrue(true);
    }
}
