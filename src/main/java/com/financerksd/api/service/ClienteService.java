package com.financerksd.api.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.financerksd.api.exception.RecursoNoEncontradoException;
import com.financerksd.api.model.Asesor;
import com.financerksd.api.model.Cliente;
import com.financerksd.api.model.EstadoCliente;
import com.financerksd.api.repository.AsesorRepository;
import com.financerksd.api.repository.ClienteRepository;

@Service
public class ClienteService {

    private final ClienteRepository clientes;
    private final AsesorRepository asesores;

    public ClienteService(ClienteRepository clientes, AsesorRepository asesores) {
        this.clientes = clientes;
        this.asesores = asesores;
    }

    public List<Cliente> listar(Integer soloDelAsesor) {
        return soloDelAsesor == null ? clientes.findAll() : clientes.findByAsesorAsignado_IdUsuario(soloDelAsesor);
    }

    public Cliente buscar(int idCliente) {
        return clientes.findById(idCliente)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado."));
    }

    @Transactional
    public Cliente asignarAsesor(int idCliente, int idAsesor) {
        Cliente c = buscar(idCliente);
        Asesor a = asesores.findById(idAsesor)
                .orElseThrow(() -> new RecursoNoEncontradoException("Asesor no encontrado."));
        c.setAsesorAsignado(a);
        return clientes.save(c);
    }

    /** Si el cliente aun no tiene asesor, queda con este. Si ya tiene uno, no cambia nada. */
    @Transactional
    public void asignarSiNoTiene(int idCliente, int idAsesor) {
        Cliente c = buscar(idCliente);
        if (c.getAsesorAsignado() == null) asignarAsesor(idCliente, idAsesor);
    }

    @Transactional
    public Cliente cambiarEstado(int idCliente, EstadoCliente estado) {
        Cliente c = buscar(idCliente);
        c.setEstadoCliente(estado);
        return clientes.save(c);
    }
}
