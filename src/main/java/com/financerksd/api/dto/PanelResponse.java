package com.financerksd.api.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.financerksd.api.model.EstadoCliente;
import com.financerksd.api.service.AlertasCliente.Alerta;

public record PanelResponse(Resumen resumen, List<ClientePanel> clientes, int pagina, int tamano, long total, int totalPaginas) {

    /** Se calcula sobre el alcance elegido (todos / mios / sin asesor), ANTES de buscar o filtrar por estado. */
    public record Resumen(long totalClientes, Map<EstadoCliente, Long> porEstado, long conAlertas, long sinAsesor,
                          Map<String, Long> alertasPorTipo) {}

    public record PlanResumen(Integer idPlan, String titulo, String periodo, double progreso,
                              int metasTotal, int metasPendientes, long metasVencidas) {}

    public record ClientePanel(Integer idCliente, String nombre, String apellido, String correo, String telefono,
                               EstadoCliente estadoCliente, Integer idAsesor, String nombreAsesor,
                               PlanResumen planActivo, LocalDate ultimoDiagnostico, Long diasSinDiagnostico,
                               long cantidadNotas, List<Alerta> alertas) {}
}
