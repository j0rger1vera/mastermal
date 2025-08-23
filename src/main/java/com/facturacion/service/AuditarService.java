package com.facturacion.service;

import com.facturacion.entity.Auditoria;
import com.facturacion.entity.Cliente;
import com.facturacion.repository.AuditarRepository;
import com.facturacion.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AuditarService {

    @Autowired
    private AuditarRepository auditoriaRepository;

    public List<Auditoria> listaRegistroMovimientos() {
        return this.auditoriaRepository.getMovimientos();
    }

    public Auditoria registrarMovimiento(Auditoria movimiento) {
        return this.auditoriaRepository.save(movimiento);
    }
}
