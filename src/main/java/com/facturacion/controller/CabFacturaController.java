package com.facturacion.controller;

import com.facturacion.dto.FacturacionGeneralDTO;
import com.facturacion.dto.DetFacturaDTO;
import com.facturacion.dto.HistorialAbonosDTO;
import com.facturacion.entity.Abono;
import com.facturacion.entity.CabFactura;
import com.facturacion.entity.Cliente;
import com.facturacion.service.CabFacturaService;
import com.facturacion.util.ResponseMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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

    @GetMapping("/numfactura")
    public ResponseEntity<ResponseMessage> generaFactura() {
        Integer newNumeroFactura = cabFacturaService.generaFactura();
        return new ResponseEntity(newNumeroFactura, HttpStatus.OK);
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

    @GetMapping("/porcobrar")
    public ResponseEntity<List<FacturacionGeneralDTO>> saldosPorCobrar() {
        List<FacturacionGeneralDTO> cabeceras = cabFacturaService.consultarSaldosPorCobrar();
        return new ResponseEntity<>(cabeceras, HttpStatus.OK);
    }

    @PutMapping("/abonar")
    public ResponseEntity<Void> abonarAFactura(@RequestBody CabFactura cabFactura) {
        this.cabFacturaService.abonarAFactura(cabFactura);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/registrar-abono")
    public ResponseEntity<Abono> registrarAbono(@RequestBody Abono abono) {
        Abono abonoRegistrado = cabFacturaService.registrarAbono(abono);
        return new ResponseEntity<>(abonoRegistrado, HttpStatus.CREATED);
    }

    @GetMapping("/historial-abonos")
    public ResponseEntity<List<HistorialAbonosDTO>> obtenerHistoricoAbonos() {
        List<HistorialAbonosDTO> cabeceras = cabFacturaService.obtenerHistorialAbonos();
        return new ResponseEntity<>(cabeceras, HttpStatus.OK);
    }
}
