package com.financerksd.api.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.financerksd.api.exception.AccesoDenegadoException;
import com.financerksd.api.exception.RecursoNoEncontradoException;
import com.financerksd.api.model.Asesor;
import com.financerksd.api.model.Cliente;
import com.financerksd.api.model.NotaAsesor;
import com.financerksd.api.repository.AsesorRepository;
import com.financerksd.api.repository.NotaAsesorRepository;

// Notas privadas del asesor sobre un cliente. El cliente nunca las ve (las rutas son solo de ASESOR).
@Service
public class NotaService {

    private final NotaAsesorRepository notas;
    private final ClienteService clienteService;
    private final AsesorRepository asesores;

    public NotaService(NotaAsesorRepository notas, ClienteService clienteService, AsesorRepository asesores) {
        this.notas = notas;
        this.clienteService = clienteService;
        this.asesores = asesores;
    }

    @Transactional(readOnly = true)
    public List<NotaAsesor> listar(int idCliente) {
        clienteService.buscar(idCliente);
        return notas.findByCliente_IdUsuarioOrderByFechaCreacionDescIdNotaDesc(idCliente);
    }

    @Transactional
    public NotaAsesor crear(int idCliente, int idAsesor, String texto) {
        Cliente cliente = clienteService.buscar(idCliente);
        Asesor asesor = asesores.findById(idAsesor)
                .orElseThrow(() -> new RecursoNoEncontradoException("Asesor no encontrado."));
        NotaAsesor n = new NotaAsesor();
        n.setCliente(cliente);
        n.setAsesor(asesor);
        n.setTexto(texto.trim());
        return notas.save(n);
    }

    /** Solo el asesor que escribio la nota puede borrarla. */
    @Transactional
    public void eliminar(int idNota, int idAsesor) {
        NotaAsesor n = notas.findById(idNota)
                .orElseThrow(() -> new RecursoNoEncontradoException("Nota no encontrada."));
        if (!n.getAsesor().getIdUsuario().equals(idAsesor)) {
            throw new AccesoDenegadoException("Solo quien escribió la nota puede borrarla.");
        }
        notas.delete(n);
    }
}
