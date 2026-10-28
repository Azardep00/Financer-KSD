package com.financerksd.api.service;

import java.text.Normalizer;
import java.util.Locale;

// Busqueda del panel en Java puro: ignora mayusculas y tildes, y cada palabra debe aparecer
// en nombre, apellido, correo o telefono ("juan perez" encuentra a Juan Pérez).
public final class PanelFiltros {

    private PanelFiltros() {}

    public static String normalizar(String s) {
        if (s == null) return "";
        return Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT).trim();
    }

    public static boolean coincide(String q, String nombre, String apellido, String correo, String telefono) {
        String busqueda = normalizar(q);
        if (busqueda.isEmpty()) return true;
        String pajar = normalizar(nombre) + " " + normalizar(apellido) + " " + normalizar(correo) + " " + normalizar(telefono);
        for (String palabra : busqueda.split("\\s+")) {
            if (!pajar.contains(palabra)) return false;
        }
        return true;
    }
}
