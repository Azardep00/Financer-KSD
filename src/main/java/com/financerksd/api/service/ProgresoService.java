package com.financerksd.api.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.financerksd.api.dto.ProgresoResponse;
import com.financerksd.api.dto.ProgresoResponse.PuntoHistorico;
import com.financerksd.api.dto.ProgresoResponse.Variacion;
import com.financerksd.api.model.DiagnosticoFinanciero;
import com.financerksd.api.model.PlanMejora;
import com.financerksd.api.repository.PlanMejoraRepository;

// Junta historial de diagnosticos + avance del plan en una sola respuesta
// para alimentar el dashboard de progreso (cliente y asesor).
@Service
public class ProgresoService {

    private final DiagnosticoService diagnosticos;
    private final PlanMejoraRepository planes;

    public ProgresoService(DiagnosticoService diagnosticos, PlanMejoraRepository planes) {
        this.diagnosticos = diagnosticos;
        this.planes = planes;
    }

    @Transactional(readOnly = true)
    public ProgresoResponse calcular(int idCliente) {
        List<DiagnosticoFinanciero> historial = diagnosticos.historialCronologico(idCliente);

        List<PuntoHistorico> puntos = historial.stream().map(PuntoHistorico::desde).toList();
        Variacion variacion = historial.size() >= 2
                ? Variacion.entre(historial.get(0), historial.get(historial.size() - 1))
                : null;

        PlanMejora plan = planes.findFirstByCliente_IdUsuarioOrderByFechaCreacionDescIdPlanDesc(idCliente).orElse(null);
        int total = plan == null ? 0 : plan.getMetas().size();
        int hechas = plan == null ? 0 : (int) plan.getMetas().stream().filter(m -> m.isCompletada()).count();
        double progreso = plan == null ? 0 : plan.getProgreso();

        return new ProgresoResponse(idCliente, puntos, variacion, hechas, total, progreso);
    }
}
