package com.facturacion.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DetFacturaDTO {

    private Integer idProducto;
    private String codigoProducto;
    private Integer cantidad;
    private Integer pkCabFactura;
    private BigDecimal valUnitarioProd;
    private BigDecimal valTotalProd;
    private String nombreProducto;
}
