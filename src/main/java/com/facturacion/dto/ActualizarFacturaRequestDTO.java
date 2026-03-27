package com.facturacion.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ActualizarFacturaRequestDTO {

    @NotNull
    private Integer idFactura;

    private String detalle;

    @NotEmpty(message = "Debe enviar al menos un detalle")
    private List<DetFacturaRequestDTO> detalles;
}
