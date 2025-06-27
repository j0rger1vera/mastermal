package com.facturacion.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class FacturacionGeneralDTO {
        private Integer idFactura;
        private Integer numeroFactura;
        private String rucCliente;
        private String nombreCliente;
        private BigDecimal saldo;
        private BigDecimal abono;
        private BigDecimal total;
        private BigDecimal subtotal;
        private String detalle;
        private String fecha;
}
