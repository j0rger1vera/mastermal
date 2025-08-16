package com.facturacion.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class HistorialAbonosDTO {
        private Integer idAbono;
        private Integer numeroFactura;
        private String nombreCliente;
        private BigDecimal abono;
        private String fechaAbono;
        private BigDecimal abonoAnterior;
        private BigDecimal totalFactura;
}
