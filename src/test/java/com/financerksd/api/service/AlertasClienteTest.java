package com.financerksd.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.financerksd.api.model.EstadoCliente;
import com.financerksd.api.service.AlertasCliente.Alerta;
import com.financerksd.api.service.AlertasCliente.Tipo;

class AlertasClienteTest {

    private static final LocalDate HOY = LocalDate.of(2026, 10, 28);

    private List<Tipo> tipos(List<Alerta> alertas) {
        return alertas.stream().map(Alerta::tipo).toList();
    }

    /** Cliente activo y al dia: diagnostico reciente, plan vigente, sin metas vencidas. */
    private List<Alerta> alDia() {
        return AlertasCliente.calcular(EstadoCliente.ACTIVO, HOY.minusDays(5), true,
                LocalDate.of(2026, 10, 31), "2026-10", 0, HOY);
    }

    @Test
    void clienteActivoAlDiaNoTieneAlertas() {
        assertTrue(alDia().isEmpty());
    }

    @Test
    void soloLosActivosGeneranAlertas() {
        for (EstadoCliente e : new EstadoCliente[] {EstadoCliente.PROSPECTO, EstadoCliente.PAUSADO, EstadoCliente.FINALIZADO}) {
            // todo mal a proposito: sin diagnostico, sin plan
            assertTrue(AlertasCliente.calcular(e, null, false, null, null, 0, HOY).isEmpty(), "estado " + e);
        }
    }

    @Test
    void sinDiagnostico() {
        var alertas = AlertasCliente.calcular(EstadoCliente.ACTIVO, null, true,
                LocalDate.of(2026, 10, 31), "2026-10", 0, HOY);
        assertEquals(List.of(Tipo.SIN_DIAGNOSTICO), tipos(alertas));
    }

    @Test
    void diagnosticoViejoEmpiezaEnLosTreintaDias() {
        var a29 = AlertasCliente.calcular(EstadoCliente.ACTIVO, HOY.minusDays(29), true,
                LocalDate.of(2026, 10, 31), "2026-10", 0, HOY);
        var a30 = AlertasCliente.calcular(EstadoCliente.ACTIVO, HOY.minusDays(30), true,
                LocalDate.of(2026, 10, 31), "2026-10", 0, HOY);
        assertTrue(a29.isEmpty());
        assertEquals(List.of(Tipo.DIAGNOSTICO_VIEJO), tipos(a30));
        assertEquals("Sin diagnóstico hace 30 días.", a30.get(0).mensaje());
    }

    @Test
    void sinPlanActivo_noEvaluaMesNiMetas() {
        // aunque llegaran datos de plan y metas vencidas, sin plan activo solo avisa SIN_PLAN_ACTIVO
        var alertas = AlertasCliente.calcular(EstadoCliente.ACTIVO, HOY.minusDays(1), false,
                LocalDate.of(2026, 9, 30), "2026-09", 3, HOY);
        assertEquals(List.of(Tipo.SIN_PLAN_ACTIVO), tipos(alertas));
    }

    @Test
    void mesSinCerrarSoloCuandoElPeriodoYaPaso() {
        var elUltimoDia = AlertasCliente.calcular(EstadoCliente.ACTIVO, HOY.minusDays(1), true, HOY, "2026-10", 0, HOY);
        var ayerTermino = AlertasCliente.calcular(EstadoCliente.ACTIVO, HOY.minusDays(1), true, HOY.minusDays(1), "2026-10", 0, HOY);
        assertTrue(elUltimoDia.isEmpty());
        assertEquals(List.of(Tipo.MES_SIN_CERRAR), tipos(ayerTermino));
        assertEquals("El plan de 2026-10 ya terminó y no se ha cerrado.", ayerTermino.get(0).mensaje());
    }

    @Test
    void metasVencidasSingularYPlural() {
        var una = AlertasCliente.calcular(EstadoCliente.ACTIVO, HOY.minusDays(1), true,
                LocalDate.of(2026, 10, 31), "2026-10", 1, HOY);
        var varias = AlertasCliente.calcular(EstadoCliente.ACTIVO, HOY.minusDays(1), true,
                LocalDate.of(2026, 10, 31), "2026-10", 3, HOY);
        assertEquals("1 meta vencida.", una.get(0).mensaje());
        assertEquals("3 metas vencidas.", varias.get(0).mensaje());
        assertEquals(List.of(Tipo.METAS_VENCIDAS), tipos(varias));
    }

    @Test
    void variasAlertasSeAcumulanEnOrden() {
        var alertas = AlertasCliente.calcular(EstadoCliente.ACTIVO, HOY.minusDays(45), true,
                HOY.minusDays(1), "2026-10", 2, HOY);
        assertEquals(List.of(Tipo.DIAGNOSTICO_VIEJO, Tipo.MES_SIN_CERRAR, Tipo.METAS_VENCIDAS), tipos(alertas));
    }
}
