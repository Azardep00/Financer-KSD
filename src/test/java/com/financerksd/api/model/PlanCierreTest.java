package com.financerksd.api.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class PlanCierreTest {

    private static final LocalDate HOY = LocalDate.of(2026, 10, 28);

    private MetaFinanciera texto(String desc, boolean hecha) {
        MetaFinanciera m = new MetaFinanciera();
        m.setDescripcion(desc);
        if (hecha) m.marcarCompletada(HOY);
        return m;
    }

    private MetaFinanciera ahorro(String inicial, String objetivo) {
        MetaFinanciera m = new MetaFinanciera();
        m.setDescripcion("fondo");
        m.setTipo(TipoMeta.AHORRO);
        m.setValorInicial(new BigDecimal(inicial));
        m.setMontoObjetivo(new BigDecimal(objetivo));
        return m;
    }

    private DiagnosticoFinanciero diag(String ahorro, String deuda, String gasto) {
        DiagnosticoFinanciero d = new DiagnosticoFinanciero();
        d.setIngresoMensual(new BigDecimal("4000000"));
        d.setGastoMensual(new BigDecimal(gasto));
        d.setDeudaTotal(new BigDecimal(deuda));
        d.setAhorroActual(new BigDecimal(ahorro));
        return d;
    }

    private PlanMejora plan(LocalDate cualquierDia) {
        PlanMejora p = new PlanMejora();
        p.setTitulo("Plan");
        p.setFechaCreacion(cualquierDia);
        p.definirPeriodo(cualquierDia);
        return p;
    }

    @Test
    void cerrarMarcaEstadoYFecha_yNoSePuedeCerrarDosVeces() {
        PlanMejora p = plan(HOY);
        p.cerrar("buen mes", HOY);
        assertEquals(EstadoPlan.CERRADO, p.getEstado());
        assertEquals(HOY, p.getFechaCierre());
        assertEquals("buen mes", p.getNotaCierre());
        assertThrows(IllegalArgumentException.class, () -> p.cerrar("otra vez", HOY));
        assertThrows(IllegalArgumentException.class, () -> p.cancelar("x", HOY));
        assertThrows(IllegalArgumentException.class, p::exigirActivo);
    }

    @Test
    void cancelarGuardaElMotivo() {
        PlanMejora p = plan(HOY);
        p.cancelar("el cliente se pausó", HOY);
        assertEquals(EstadoPlan.CANCELADO, p.getEstado());
        assertEquals("el cliente se pausó", p.getNotaCierre());
    }

    @Test
    void siguientePlanArrastraSoloLasPendientes() {
        PlanMejora p = plan(HOY);
        p.agregarMeta(texto("hecha", true));
        p.agregarMeta(texto("pendiente", false));
        PlanMejora sig = p.crearSiguiente(null, true, HOY);

        assertEquals("2026-11", sig.getPeriodo());
        assertEquals(LocalDate.of(2026, 11, 30), sig.getPeriodoFin());
        assertEquals("Plan 2026-11", sig.getTitulo());
        assertEquals(EstadoPlan.ACTIVO, sig.getEstado());
        assertEquals(1, sig.getMetas().size());
        assertEquals("pendiente", sig.getMetas().get(0).getDescripcion());
        assertFalse(sig.getMetas().get(0).isCompletada());
    }

    @Test
    void siguientePlanSinArrastrarQuedaVacio() {
        PlanMejora p = plan(HOY);
        p.agregarMeta(texto("pendiente", false));
        assertEquals(0, p.crearSiguiente("Mi plan", false, HOY).getMetas().size());
    }

    @Test
    void diciembreSaltaAEneroDelAnioSiguiente() {
        PlanMejora sig = plan(LocalDate.of(2026, 12, 10)).crearSiguiente(null, true, LocalDate.of(2026, 12, 31));
        assertEquals("2027-01", sig.getPeriodo());
    }

    @Test
    void metaArrastradaConserva_puntoDePartida_yMueveFechaVencida() {
        PlanMejora p = plan(HOY);
        MetaFinanciera m = ahorro("1000000", "3000000");
        m.setValorActual(new BigDecimal("2000000"));
        m.setFechaLimite(LocalDate.of(2026, 10, 31)); // ya no sirve en noviembre
        p.agregarMeta(m);

        MetaFinanciera copia = p.crearSiguiente(null, true, HOY).getMetas().get(0);
        assertEquals(0, new BigDecimal("1000000").compareTo(copia.getValorInicial()));
        assertEquals(50.0, copia.getProgreso(), 0.01);                 // sigue en 50%, no reinicia
        assertEquals(LocalDate.of(2026, 11, 30), copia.getFechaLimite());
    }

    @Test
    void fechaLimiteFuturaSeRespeta() {
        PlanMejora p = plan(HOY);
        MetaFinanciera m = texto("larga", false);
        m.setFechaLimite(LocalDate.of(2027, 3, 15));
        p.agregarMeta(m);
        assertEquals(LocalDate.of(2027, 3, 15), p.crearSiguiente(null, true, HOY).getMetas().get(0).getFechaLimite());
    }

    @Test
    void diagnosticoActualizaMetasMedibles_yLasCompletaAlLlegar() {
        PlanMejora p = plan(HOY);
        MetaFinanciera a = ahorro("1000000", "3000000");
        p.agregarMeta(a);
        p.agregarMeta(texto("sin monto", false));

        assertEquals(1, p.sincronizarConDiagnostico(diag("2000000", "9000000", "3000000"), HOY));
        assertEquals(50.0, a.getProgreso(), 0.01);
        assertFalse(a.isCompletada());

        p.sincronizarConDiagnostico(diag("3500000", "9000000", "3000000"), HOY);
        assertTrue(a.isCompletada());
        assertEquals(HOY, a.getFechaCompletada());

        // una vez completada, un diagnostico peor no la reabre
        assertEquals(0, p.sincronizarConDiagnostico(diag("500000", "9000000", "3000000"), HOY));
        assertTrue(a.isCompletada());
    }

    @Test
    void deudaYGastoTomanSuPropioValor() {
        MetaFinanciera deuda = new MetaFinanciera();
        deuda.setTipo(TipoMeta.REDUCCION_DEUDA);
        deuda.setValorInicial(new BigDecimal("10000000"));
        deuda.setMontoObjetivo(new BigDecimal("4000000"));
        MetaFinanciera gasto = new MetaFinanciera();
        gasto.setTipo(TipoMeta.REDUCCION_GASTO);
        gasto.setValorInicial(new BigDecimal("3000000"));
        gasto.setMontoObjetivo(new BigDecimal("2000000"));

        DiagnosticoFinanciero d = diag("0", "7000000", "2500000");
        assertTrue(deuda.sincronizarCon(d, HOY));
        assertTrue(gasto.sincronizarCon(d, HOY));
        assertEquals(50.0, deuda.getProgreso(), 0.01);
        assertEquals(50.0, gasto.getProgreso(), 0.01);
    }
}
