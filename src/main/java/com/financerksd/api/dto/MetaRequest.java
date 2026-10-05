package com.financerksd.api.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;

public record MetaRequest(@NotBlank String descripcion, LocalDate fechaLimite) {}
