package com.financerksd.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.financerksd.api.model.DiagnosticoFinanciero;

public interface DiagnosticoFinancieroRepository extends JpaRepository<DiagnosticoFinanciero, Integer> {
    List<DiagnosticoFinanciero> findByCliente_IdUsuarioOrderByFechaDescIdDiagnosticoDesc(Integer idCliente);
    List<DiagnosticoFinanciero> findByCliente_IdUsuarioOrderByFechaAscIdDiagnosticoAsc(Integer idCliente);
    /** [idCliente, fecha del ultimo diagnostico] para todos los clientes que tienen alguno. */
    @Query("select d.cliente.idUsuario, max(d.fecha) from DiagnosticoFinanciero d group by d.cliente.idUsuario")
    List<Object[]> ultimaFechaPorCliente();

    Optional<DiagnosticoFinanciero> findFirstByCliente_IdUsuarioOrderByFechaDescIdDiagnosticoDesc(Integer idCliente);
}
