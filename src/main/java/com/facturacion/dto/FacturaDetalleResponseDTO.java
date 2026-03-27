package com.facturacion.dto;


import com.facturacion.enums.EstadoFactura;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class FacturaDetalleResponseDTO {

    private Integer idFactura;
    private Long numeroFactura;
    private LocalDateTime fecha;
    private String rucCliente;
    private String detalle;
    private BigDecimal subtotal;
    private BigDecimal igv;
    private BigDecimal total;
    private BigDecimal abono;
    private BigDecimal saldo;
    private EstadoFactura estado;

    private List<DetFacturaResponseDTO> detalles;
    private List<HistorialAbonosDTO> abonos;

}
