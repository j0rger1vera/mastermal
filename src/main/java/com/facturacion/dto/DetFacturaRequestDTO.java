package com.facturacion.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DetFacturaRequestDTO {

    private Integer codigoProducto;
    private Integer cantidad;
    private BigDecimal valUnitarioProducto;
}