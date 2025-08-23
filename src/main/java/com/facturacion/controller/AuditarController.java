package com.facturacion.controller;

import com.facturacion.entity.Auditoria;
import com.facturacion.entity.Cliente;
import com.facturacion.service.AuditarService;
import com.facturacion.service.ClienteService;
import com.facturacion.util.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/movimientos")
public class AuditarController {

    @Autowired
    private AuditarService auditoriaService;

    @GetMapping
    public List<Auditoria> listarMovimientos() {
        return this.auditoriaService.listaRegistroMovimientos();
    }

    @PostMapping("/guardar")
    public ResponseEntity<Auditoria> registrarMovimiento(@RequestBody Auditoria movimiento) {
        movimiento.setFecha(LocalDate.now());
        Auditoria nuevoRegistro = this.auditoriaService.registrarMovimiento(movimiento);
        return new ResponseEntity<>(nuevoRegistro, HttpStatus.CREATED);
    }
}
