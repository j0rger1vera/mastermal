package com.facturacion.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
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
}
