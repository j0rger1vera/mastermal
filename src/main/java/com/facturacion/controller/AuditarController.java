package com.facturacion.controller;

import com.facturacion.entity.Auditoria;
import com.facturacion.service.AuditarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

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
    public ResponseEntity<Auditoria> registrarMovimiento(@RequestBody Auditoria movimiento, String funcion, String operacion) {
        movimiento.setFecha(LocalDateTime.now());
        Auditoria nuevoRegistro = this.auditoriaService.registrarMovimiento(movimiento, funcion, operacion);
        return new ResponseEntity<>(nuevoRegistro, HttpStatus.CREATED);
    }
}
