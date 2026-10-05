package com.financerksd.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.financerksd.api.model.PlanMejora;

public interface PlanMejoraRepository extends JpaRepository<PlanMejora, Integer> {
    List<PlanMejora> findByCliente_IdUsuarioOrderByFechaCreacionDescIdPlanDesc(Integer idCliente);
    Optional<PlanMejora> findFirstByCliente_IdUsuarioOrderByFechaCreacionDescIdPlanDesc(Integer idCliente);
    List<PlanMejora> findByAsesor_IdUsuario(Integer idAsesor);
}
