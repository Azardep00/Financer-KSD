package com.financerksd.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.financerksd.api.model.NotaAsesor;

public interface NotaAsesorRepository extends JpaRepository<NotaAsesor, Integer> {

    /** [idCliente, cantidad de notas] para los clientes que tienen alguna. */
    @Query("select n.cliente.idUsuario, count(n) from NotaAsesor n group by n.cliente.idUsuario")
    List<Object[]> contarPorCliente();

    List<NotaAsesor> findByCliente_IdUsuarioOrderByFechaCreacionDescIdNotaDesc(int idCliente);
}
