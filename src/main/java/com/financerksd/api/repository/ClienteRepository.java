package com.financerksd.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.financerksd.api.model.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    List<Cliente> findByAsesorAsignado_IdUsuario(Integer idAsesor);
}
