package com.facturacion.controller;

import com.facturacion.dto.AbonoDTO;
import com.facturacion.dto.ActualizarFacturaRequestDTO;
import com.facturacion.dto.CrearFacturaRequestDTO;
import com.facturacion.dto.CabFacturaResponseDTO;
import com.facturacion.dto.FacturaDetalleResponseDTO;
import com.facturacion.dto.FacturacionGeneralDTO;
import com.facturacion.service.AbonoService;
import com.facturacion.service.CabFacturaService;
import com.facturacion.util.ResponseMessage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/facturas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CabFacturaController {

    private final CabFacturaService cabFacturaService;
    private final AbonoService abonoService;

    @PostMapping
    public ResponseEntity<CabFacturaResponseDTO> guardarFactura(
            @RequestBody CrearFacturaRequestDTO request) {

        CabFacturaResponseDTO response = cabFacturaService.crearFactura(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<FacturacionGeneralDTO>> listarFacturas() {
        return ResponseEntity.ok(cabFacturaService.listarFacturas());
    }

    @GetMapping("/cliente/{nit}")
    public ResponseEntity<List<FacturacionGeneralDTO>> listarPorCliente(
            @PathVariable("nit") String nitCliente) {
        return ResponseEntity.ok(cabFacturaService.listarPorCliente(nitCliente));
    }

    @GetMapping("/numfactura")
    public ResponseEntity<Integer> generaNumeroDeFactura() {
        return ResponseEntity.ok(cabFacturaService.generaFactura());
    }

    @PostMapping("/abonos")
    public ResponseEntity<ResponseMessage> registrarAbono(
            @Valid @RequestBody AbonoDTO abonoDTO) {

        abonoService.registrarAbono(abonoDTO);
        return ResponseEntity.ok(new ResponseMessage("Abono registrado correctamente"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CabFacturaResponseDTO> obtenerPorId(
            @PathVariable("id") Integer id) {

        return cabFacturaService.obtenerPorId(id)
                .map(factura -> ResponseEntity.ok(
                        cabFacturaService.mapToResponseDTO(factura)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/detalle")
    public ResponseEntity<FacturaDetalleResponseDTO> obtenerDetalleFactura(
            @PathVariable("id") Integer id) {
        return ResponseEntity.ok(cabFacturaService.obtenerDetalleFactura(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarFacturaPorId(@PathVariable("id") Integer id) {
        cabFacturaService.eliminarPorId(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cabeceras")
    public ResponseEntity<List<FacturacionGeneralDTO>> obtenerTodasCabeceras() {
        return ResponseEntity.ok(cabFacturaService.listarFacturas());
    }

    @PutMapping("/actualizar")
    public ResponseEntity<CabFacturaResponseDTO> actualizarFactura(
            @RequestBody ActualizarFacturaRequestDTO request) {

        CabFacturaResponseDTO response = cabFacturaService.actualizarFactura(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/saldos")
    public ResponseEntity<List<FacturacionGeneralDTO>> obtenerFacturasConSaldos() {
        return ResponseEntity.ok(cabFacturaService.obtenerFacturasConSaldos());
    }

    @GetMapping("/porcobrar")
    public ResponseEntity<List<FacturacionGeneralDTO>> saldosPorCobrar() {
        return ResponseEntity.ok(cabFacturaService.consultarSaldosPorCobrar());
    }

}
