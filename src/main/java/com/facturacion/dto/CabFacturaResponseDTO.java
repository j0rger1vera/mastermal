package com.facturacion.dto;

import com.facturacion.enums.EstadoFactura;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class CabFacturaResponseDTO {

    private Integer idFactura;
    private LocalDateTime fecha;
    private String detalle;
    private BigDecimal total;
    private BigDecimal abono;
    private BigDecimal saldo;
    private EstadoFactura estado;
}
