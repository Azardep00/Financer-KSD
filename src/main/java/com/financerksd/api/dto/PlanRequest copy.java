package com.financerksd.api.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

// "metas" es opcional al crear/editar el plan (se pueden agregar despues).
// "periodo": cualquier dia del mes al que pertenece el plan; si no se envia, es el mes actual.
public record PlanRequest(
        @NotBlank String titulo,
        String descripcion,
        @Valid List<MetaRequest> metas,
        LocalDate periodo) {}
