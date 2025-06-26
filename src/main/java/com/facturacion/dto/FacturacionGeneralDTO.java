package com.facturacion.dto;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
        private String nitCliente;
        private String nombreCliente;
        private BigDecimal saldo;
        private BigDecimal abono;
        private BigDecimal total;
        private String detalle;
        private String fechaFacturada;
}
