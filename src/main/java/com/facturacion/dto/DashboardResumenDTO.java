package com.facturacion.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DashboardResumenDTO {

    private BigDecimal totalFacturado;
    private BigDecimal totalAbonado;
    private BigDecimal totalSaldo;
    private Long cantidadFacturas;
}