package com.financerksd.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

import com.financerksd.api.model.EstadoPlan;
import com.financerksd.api.model.PlanMejora;

public interface PlanMejoraRepository extends JpaRepository<PlanMejora, Integer> {
    List<PlanMejora> findByCliente_IdUsuarioOrderByFechaCreacionDescIdPlanDesc(Integer idCliente);
    Optional<PlanMejora> findFirstByCliente_IdUsuarioOrderByFechaCreacionDescIdPlanDesc(Integer idCliente);
    List<PlanMejora> findByAsesor_IdUsuario(Integer idAsesor);

    /** Planes de un estado con sus metas ya cargadas (evita una consulta por plan en el panel). */
    @EntityGraph(attributePaths = "metas")
    List<PlanMejora> findByEstado(EstadoPlan estado);

    Optional<PlanMejora> findFirstByCliente_IdUsuarioAndEstadoOrderByPeriodoInicioDescIdPlanDesc(Integer idCliente, EstadoPlan estado);
    boolean existsByCliente_IdUsuarioAndEstado(Integer idCliente, EstadoPlan estado);
    boolean existsByCliente_IdUsuarioAndPeriodoInicioAndEstadoNot(Integer idCliente, LocalDate periodoInicio, EstadoPlan estado);
}
