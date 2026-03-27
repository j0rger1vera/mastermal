package com.facturacion.exception;

import com.facturacion.controller.AbonoController;
import com.facturacion.dto.AbonoDTO;
import com.facturacion.service.AbonoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GlobalExceptionHandlerTest {

    private final AbonoService abonoService = Mockito.mock(AbonoService.class);

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new AbonoController(abonoService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void debeRetornar404CuandoOcurreErrorDeNegocio() throws Exception {

        Mockito.when(abonoService.registrarAbono(Mockito.any()))
                .thenThrow(new IllegalArgumentException("Factura no encontrada"));

        AbonoDTO dto = new AbonoDTO();
        dto.setIdFactura(99);
        dto.setValorAbono(new BigDecimal("10.00"));

        mockMvc.perform(post("/abonos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("BUSINESS_ERROR"))
                .andExpect(jsonPath("$.message")
                        .value(containsString("Factura no encontrada")))
                .andExpect(jsonPath("$.path").value("/abonos"));
    }

    @Test
    void debeRetornar500CuandoOcurreErrorNoControlado() throws Exception {

        Mockito.when(abonoService.registrarAbono(Mockito.any()))
                .thenThrow(new RuntimeException("Boom"));

        AbonoDTO dto = new AbonoDTO();
        dto.setIdFactura(1);
        dto.setValorAbono(new BigDecimal("10.00"));

        mockMvc.perform(post("/abonos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message")
                        .value("Ocurrió un error inesperado"))
                .andExpect(jsonPath("$.path").value("/abonos"));
    }
}
