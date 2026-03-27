package com.facturacion.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class DetFacturaResponseDTO {

    private Integer id;
    private Integer codigoProducto;
    private Integer cantidad;
    private BigDecimal valUnitarioProducto;
    private BigDecimal valTotalProducto;
}
