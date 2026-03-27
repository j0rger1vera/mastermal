package com.facturacion.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class CrearFacturaRequestDTO {

    private String rucCliente;
    private String detalle;
    private List<DetFacturaRequestDTO> detalles;
    private BigDecimal abonoInicial;

}