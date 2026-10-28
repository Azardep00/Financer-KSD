package com.financerksd.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PanelFiltrosTest {

    @Test
    void normalizarQuitaTildesYMayusculas() {
        assertEquals("juan perez", PanelFiltros.normalizar("  JUAN Pérez "));
        assertEquals("munoz", PanelFiltros.normalizar("Muñoz"));
        assertEquals("", PanelFiltros.normalizar(null));
    }

    @Test
    void busquedaVaciaONulaCoincideConTodos() {
        assertTrue(PanelFiltros.coincide(null, "Ana", "Gómez", "ana@mail.com", null));
        assertTrue(PanelFiltros.coincide("   ", "Ana", "Gómez", "ana@mail.com", null));
    }

    @Test
    void ignoraTildesYMayusculas() {
        assertTrue(PanelFiltros.coincide("juan perez", "Juan", "Pérez", "j@mail.com", null));
        assertTrue(PanelFiltros.coincide("PEREZ", "Juan", "Pérez", "j@mail.com", null));
        assertTrue(PanelFiltros.coincide("pérez", "Juan", "Perez", "j@mail.com", null));
        assertTrue(PanelFiltros.coincide("munoz", "Luis", "Muñoz", "l@mail.com", null));
    }

    @Test
    void buscaEnCorreoYTelefono() {
        assertTrue(PanelFiltros.coincide("gmail", "Ana", "Gómez", "ana@gmail.com", null));
        assertTrue(PanelFiltros.coincide("3001234", "Ana", "Gómez", "ana@gmail.com", "3001234567"));
    }

    @Test
    void todasLasPalabrasDebenAparecer() {
        assertFalse(PanelFiltros.coincide("juan lopez", "Juan", "Pérez", "j@mail.com", null));
        assertFalse(PanelFiltros.coincide("zzz", "Juan", "Pérez", "j@mail.com", null));
    }

    @Test
    void telefonoNuloNoRompe() {
        assertFalse(PanelFiltros.coincide("300", "Juan", "Pérez", "j@mail.com", null));
    }
}
