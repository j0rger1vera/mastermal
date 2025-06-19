package com.facturacion.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class FacturacionGeneralDTO {
        private Integer numeroFactura;
        private String nitCliente;
        private String nombreCliente;
        private BigDecimal saldo;
        private BigDecimal abono;
        private BigDecimal total;
        private String fechaFacturada;
}
