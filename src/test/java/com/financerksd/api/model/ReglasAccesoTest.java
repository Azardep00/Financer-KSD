package com.financerksd.api.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ReglasAccesoTest {

    @Test
    void elAsesorAsignadoPuedeEscribir() {
        assertTrue(ReglasAcceso.asesorResponsable(7, 7));
    }

    @Test
    void otroAsesorNoPuedeEscribirSobreUnClienteAjeno() {
        assertFalse(ReglasAcceso.asesorResponsable(7, 8));
    }

    @Test
    void cualquierAsesorPuedeTomarUnClienteSinAsesor() {
        assertTrue(ReglasAcceso.asesorResponsable(null, 8));
    }

    @Test
    void elClienteEscribeSoloLoSuyo() {
        assertTrue(ReglasAcceso.puedeEscribir(false, 20, 20, 7));
        assertFalse(ReglasAcceso.puedeEscribir(false, 21, 20, 7));
    }

    @Test
    void unAsesorEscribeSegunSuAsignacion_noPorSerAsesor() {
        assertTrue(ReglasAcceso.puedeEscribir(true, 7, 20, 7));
        assertFalse(ReglasAcceso.puedeEscribir(true, 8, 20, 7));
        assertTrue(ReglasAcceso.puedeEscribir(true, 8, 20, null));
    }
}
