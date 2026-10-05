package com.financerksd.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.financerksd.api.model.DiagnosticoFinanciero;

public interface DiagnosticoFinancieroRepository extends JpaRepository<DiagnosticoFinanciero, Integer> {
    List<DiagnosticoFinanciero> findByCliente_IdUsuarioOrderByFechaDescIdDiagnosticoDesc(Integer idCliente);
    List<DiagnosticoFinanciero> findByCliente_IdUsuarioOrderByFechaAscIdDiagnosticoAsc(Integer idCliente);
    Optional<DiagnosticoFinanciero> findFirstByCliente_IdUsuarioOrderByFechaDescIdDiagnosticoDesc(Integer idCliente);
}
