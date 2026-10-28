package com.financerksd.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NotaRequest(@NotBlank @Size(max = 2000) String texto) {}
