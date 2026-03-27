package com.facturacion.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HistorialAbonosDTO {

        private Integer idAbono;

        private Integer numeroFactura;

        private BigDecimal abono;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime fechaAbono;

        private BigDecimal abonoAnterior;

        private BigDecimal totalFactura;

        private String nombreCliente;
}
