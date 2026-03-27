package com.facturacion.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AbonoDTO {


    @NotNull
    private Integer idFactura;

    @Positive(message = "El valor del abono debe ser mayor a cero")
    private BigDecimal valorAbono;
}
