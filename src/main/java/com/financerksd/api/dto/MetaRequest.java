package com.financerksd.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.financerksd.api.model.TipoMeta;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

// tipo, montoObjetivo y valorInicial son opcionales: sin ellos la meta es de tipo TEXTO (como antes).
// Si el tipo no es TEXTO, montoObjetivo y valorInicial son obligatorios.
public record MetaRequest(
        @NotBlank String descripcion,
        LocalDate fechaLimite,
        TipoMeta tipo,
        @PositiveOrZero BigDecimal montoObjetivo,
        @PositiveOrZero BigDecimal valorInicial) {

    public MetaRequest(String descripcion, LocalDate fechaLimite) {
        this(descripcion, fechaLimite, null, null, null);
    }
}
