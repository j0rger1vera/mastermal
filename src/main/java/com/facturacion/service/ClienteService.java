package com.facturacion.service;

import com.facturacion.entity.Auditoria;
import com.facturacion.entity.Cliente;
import com.facturacion.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {
    public ClienteService(ClienteRepository clienteRepository, AuditarService auditarService) {
        this.clienteRepository = clienteRepository;
        this.auditarService = auditarService;
    }

    @Autowired
    private ClienteRepository clienteRepository;

    private AuditarService auditarService;

    public List<Cliente> listarClientes() {
        return this.clienteRepository.getClientes();
    }

    public Optional<Cliente> obtenerClientePorId(Integer id) {
        return this.clienteRepository.findById(id);
    }

    public Cliente crearCliente(Cliente cliente) {
        Cliente clienteGuardado = this.clienteRepository.save(cliente);
        auditarService.registrarMovimiento(clienteGuardado, "Cliente", "Crear cliente");
        return clienteGuardado;
    }

    public void actualizarCliente(Cliente cliente) {
        this.clienteRepository.save(cliente);
        auditarService.registrarMovimiento(cliente, "Cliente", "Modificar cliente");
    }

    public void eliminarCliente(Integer id) {
        this.clienteRepository.deleteById(id);
    }

    public String verificarSiExiteCliente(String ruc_dni) {
        return this.clienteRepository.verificarSiExiteCliente(ruc_dni);
    }

    public Integer generaNit() {
        return this.clienteRepository.generaNit();
    }
}
