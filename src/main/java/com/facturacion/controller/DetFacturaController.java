package com.facturacion.controller;

import com.facturacion.dto.DetFacturaDTO;
import com.facturacion.entity.DetFactura;
import com.facturacion.service.DetFacturaService;
import com.facturacion.util.ResponseMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/det-factura")
public class DetFacturaController {

    private final DetFacturaService detFacturaService;

    public DetFacturaController(DetFacturaService detFacturaService) {
        this.detFacturaService = detFacturaService;
    }

    @PostMapping("/guardar")
    public ResponseEntity<ResponseMessage> guardarDetallesFactura(@RequestBody List<DetFacturaDTO> detallesFacturaDTO) {
        detallesFacturaDTO.get(0).setValTotalProd( String.valueOf(detallesFacturaDTO.get(0).getCantidad() *
            Integer.valueOf(detallesFacturaDTO.get(0).getValUnitarioProd())) );
        detFacturaService.insertarProducto(detallesFacturaDTO);
        return ResponseEntity.ok(new ResponseMessage(200, "Detalles de factura guardados exitosamente"));
    }


    @PutMapping("/actualizardeta")
    public ResponseEntity<Void> actualizarDetalDeFactura(@RequestBody List<DetFacturaDTO> detallesFacturaDTO) {
        this.detFacturaService.actualizarDetalles(detallesFacturaDTO);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
