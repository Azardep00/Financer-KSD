package com.financerksd.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroAsesorRequest(
        @NotBlank String nombre,
        @NotBlank String apellido,
        String especialidad,
        @Email @NotBlank String correo,
        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres.") String contrasena) {}
