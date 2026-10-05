package com.financerksd.api.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

// "metas" es opcional al crear/editar el plan (se pueden agregar despues).
public record PlanRequest(
        @NotBlank String titulo,
        String descripcion,
        @Valid List<MetaRequest> metas) {}
