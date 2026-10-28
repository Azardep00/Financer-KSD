package com.financerksd.api.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.financerksd.api.model.Asesor;
import com.financerksd.api.repository.AsesorRepository;

/** Lista de asesores: la necesita el asesor para traspasar un cliente (solo rol ASESOR, ver SecurityConfig). */
@RestController
@RequestMapping("/api/asesores")
public class AsesorController {

    private final AsesorRepository asesores;

    public AsesorController(AsesorRepository asesores) {
        this.asesores = asesores;
    }

    @GetMapping
    public List<Asesor> listar() {
        return asesores.findAll();
    }
}
