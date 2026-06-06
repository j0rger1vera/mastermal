package com.facturacion.controller;

import com.facturacion.dto.HistorialAbonosDTO;
import com.facturacion.entity.Abono;
import com.facturacion.entity.CabFactura;
import com.facturacion.service.AbonoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/abonos")
public class AbonoController {

    private final AbonoService abonoService;

    public AbonoController(AbonoService abonoService) {
        this.abonoService = abonoService;
    }

    @PutMapping("/factura")
    public ResponseEntity<Void> abonarFactura(@RequestBody CabFactura cabFactura) {
        abonoService.abonarAFactura(cabFactura);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping()
    public ResponseEntity<Abono> registrarAbono(@RequestBody Abono abono) {
        Abono abonoRegistrado = abonoService.registrarAbono(abono);
        return new ResponseEntity<>(abonoRegistrado, HttpStatus.CREATED);
    }

    @GetMapping("/historial")
    public ResponseEntity<List<HistorialAbonosDTO>> obtenerHistoricoAbonos() {
        return ResponseEntity.ok(abonoService.obtenerHistorialAbonos());
    }
}
