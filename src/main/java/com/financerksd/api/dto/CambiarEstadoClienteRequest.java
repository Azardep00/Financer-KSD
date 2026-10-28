package com.financerksd.api.dto;

import com.financerksd.api.model.EstadoCliente;

import jakarta.validation.constraints.NotNull;

public record CambiarEstadoClienteRequest(@NotNull EstadoCliente estado) {}
