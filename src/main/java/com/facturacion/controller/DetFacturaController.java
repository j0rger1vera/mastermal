package com.facturacion.controller;

import com.facturacion.dto.DetFacturaDTO;
import com.facturacion.entity.DetFactura;
import com.facturacion.service.DetFacturaService;
import com.facturacion.util.ResponseMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@SuppressWarnings({"checkstyle:MissingJavadocType", "checkstyle:Indentation"})
@RestController
@RequestMapping("/det-factura")
public class DetFacturaController {

  private final DetFacturaService detFacturaService;

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public DetFacturaController(DetFacturaService detFacturaService) {
        this.detFacturaService = detFacturaService;
    }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  @PostMapping("/guardar")
    public ResponseEntity<ResponseMessage> guardarDetallesFactura(@RequestBody List<DetFacturaDTO> detallesFacturaDto) {
      detallesFacturaDto.get(0).setValTotalProd( new BigDecimal(detallesFacturaDto.get(0).getCantidad() * detallesFacturaDto.get(0).getValUnitarioProd().intValue()));
    detFacturaService.insertarProducto(detallesFacturaDto);
    return ResponseEntity.ok(new ResponseMessage(200, "Detalles de factura guardados exitosamente"));
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  @PutMapping("/actualizardeta")
    public ResponseEntity<Void> actualizarDetalDeFactura(@RequestBody List<DetFacturaDTO> detallesFacturaDto) {
        this.detFacturaService.actualizarDetalles(detallesFacturaDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

  @GetMapping("/planchas/{idFactura}")
  public ResponseEntity<List<DetFacturaDTO>> obtenerProductosPorFactura(@PathVariable("idFactura") String idFactura) {
    List<DetFacturaDTO> productos = detFacturaService.obtenerProductosPorFactura(idFactura);
    return new ResponseEntity<>(productos, HttpStatus.OK);
  }
}
