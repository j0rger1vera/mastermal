package com.facturacion.controller;

import com.facturacion.dto.FacturacionGeneralDTO;
import com.facturacion.entity.CabFactura;
import com.facturacion.entity.Cliente;
import com.facturacion.service.CabFacturaService;
import com.facturacion.util.ResponseMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cab-factura")
public class CabFacturaController {

    private final CabFacturaService cabFacturaService;

    public CabFacturaController(CabFacturaService cabFacturaService) {
        this.cabFacturaService = cabFacturaService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<CabFactura> obtenerFacturaPorId(@PathVariable("id") Integer id) {
        return cabFacturaService.obtenerPorId(id)
                .map(factura -> new ResponseEntity<>(factura, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<CabFactura> guardarFactura(@RequestBody CabFactura cabFactura) {
        CabFactura facturaGuardada = cabFacturaService.guardarCabFactura(cabFactura);
        return new ResponseEntity<>(facturaGuardada, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarFacturaPorId(@PathVariable("id") Integer id) {
        cabFacturaService.eliminarPorId(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/genera-factura")
    public ResponseEntity<ResponseMessage> generaFactura() {
        return ResponseEntity.ok(new ResponseMessage(200, this.cabFacturaService.generaFactura()));
    }

    @GetMapping("/facturacion")
    public ResponseEntity<List<FacturacionGeneralDTO>> obtenerBalanceGeneral() {
        List<FacturacionGeneralDTO> cabeceras = cabFacturaService.obtenerBalanceGeneral();
        return new ResponseEntity<>(cabeceras, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<FacturacionGeneralDTO>> obtenerTodasCabeceras() {
        List<FacturacionGeneralDTO> cabeceras = cabFacturaService.obtenerTodasFacturas();
        return new ResponseEntity<>(cabeceras, HttpStatus.OK);
    }

    @PutMapping("/actualizar")
    public ResponseEntity<Void> actualizarFactura(@RequestBody CabFactura cabFactura) {
        this.cabFacturaService.actualizarFactura(cabFactura);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/cliente/{nit}")
    public ResponseEntity<List<FacturacionGeneralDTO>> obtenerFacturaPorCliente(@PathVariable("nit") String nitCliente) {
        List<FacturacionGeneralDTO> cabeceras = cabFacturaService.obtenerPorNitCliente(nitCliente);
        return new ResponseEntity<>(cabeceras, HttpStatus.OK);
    }

    @GetMapping("/saldos")
    public ResponseEntity<List<FacturacionGeneralDTO>> obtenerFacturasConSaldos() {
        List<FacturacionGeneralDTO> cabeceras = cabFacturaService.obtenerFacturasConSaldos();
        return new ResponseEntity<>(cabeceras, HttpStatus.OK);
    }

}
