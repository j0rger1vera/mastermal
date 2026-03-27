package com.facturacion.controller;

import com.facturacion.dto.AbonoDTO;
import com.facturacion.dto.HistorialAbonosDTO;
import com.facturacion.entity.Abono;
import com.facturacion.repository.HistorialAbonosView;
import com.facturacion.service.AbonoService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/abonos")
@AllArgsConstructor
public class AbonoController {

    private final AbonoService abonoService;

    @PostMapping
    public ResponseEntity<Abono> registrarAbono(
            @Valid @RequestBody AbonoDTO abonoDTO) {
        return ResponseEntity.ok(abonoService.registrarAbono(abonoDTO));
    }

    @GetMapping
    public ResponseEntity<List<HistorialAbonosView>> obtenerHistorial() {
        return ResponseEntity.ok(abonoService.obtenerHistorialAbonos());
    }


    @GetMapping("/factura/{numFactura}")
    public ResponseEntity<List<HistorialAbonosDTO>> obtenerHistorialPorFactura(
            @PathVariable("numFactura") Integer numFactura) {
        return ResponseEntity.ok(abonoService.obtenerHistorialPorFactura(numFactura));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarAbono(
            @PathVariable Integer id) {
        abonoService.eliminarAbonoPorId(id);
        return ResponseEntity.noContent().build();
    }
}
