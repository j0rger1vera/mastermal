package com.facturacion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class ParametroRegistro {
    private String nombresCampos;
    private String valoresCampos;

}
