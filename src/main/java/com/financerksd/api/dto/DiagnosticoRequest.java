package com.financerksd.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

// "fecha" es opcional: si no viene, se usa hoy.
public record DiagnosticoRequest(
        LocalDate fecha,
        @NotNull @PositiveOrZero BigDecimal ingresoMensual,
        @NotNull @PositiveOrZero BigDecimal gastoMensual,
        @NotNull @PositiveOrZero BigDecimal deudaTotal,
        @NotNull @PositiveOrZero BigDecimal ahorroActual) {}
