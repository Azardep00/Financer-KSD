package com.financerksd.api.dto;

// Todo opcional. Por defecto: crea el plan del mes siguiente y arrastra las metas pendientes.
public record CierreRequest(
        String notaCierre,
        Boolean crearSiguiente,
        Boolean arrastrarPendientes,
        String tituloSiguiente) {}
