package com.facturacion.service;

import com.facturacion.entity.CabFactura;
import com.facturacion.enums.EstadoFactura;
import com.facturacion.exception.AbonoInvalidoException;
import com.facturacion.repository.CabFacturaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CabFacturaServiceTest {

    @Mock
    private CabFacturaRepository cabFacturaRepository;

    @Mock
    private AuditarService auditarService;

    @Mock
    private AbonoService abonoService;

    @InjectMocks
    private CabFacturaService cabFacturaService;
/*
    @Test
    void debeCrearFacturaSinAbonoInicial() {

        // given
        CabFactura factura = new CabFactura();
        factura.setTotal(new BigDecimal("100.00"));
        factura.setAbono(BigDecimal.ZERO);

        when(cabFacturaRepository.save(any(CabFactura.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        CabFactura resultado = cabFacturaService.crearFactura(factura);

        // then
        assertThat(resultado.getSaldo())
                .isEqualByComparingTo("100.00");

        assertThat(resultado.getEstado())
                .isEqualTo(EstadoFactura.PARCIAL);

        verify(abonoService, never()).registrarAbonoInicial(any());
        verify(auditarService, times(1))
                .registrarMovimiento(any(), any(), any());
    }

    @Test
    void debeCrearFacturaConAbonoInicialValido() {

        // given
        CabFactura factura = new CabFactura();
        factura.setTotal(new BigDecimal("100.00"));
        factura.setAbono(new BigDecimal("30.00"));

        when(cabFacturaRepository.save(any(CabFactura.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        CabFactura resultado = cabFacturaService.crearFactura(factura);

        // then
        assertThat(resultado.getSaldo())
                .isEqualByComparingTo("70.00");

        assertThat(resultado.getEstado())
                .isEqualTo(EstadoFactura.PARCIAL);

        verify(abonoService, times(1))
                .registrarAbonoInicial(any());
    }

    @Test
    void debeCrearFacturaPagadaCuandoAbonoEsIgualAlTotal() {

        // given
        CabFactura factura = new CabFactura();
        factura.setTotal(new BigDecimal("100.00"));
        factura.setAbono(new BigDecimal("100.00"));

        when(cabFacturaRepository.save(any(CabFactura.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        CabFactura resultado = cabFacturaService.crearFactura(factura);

        // then
        assertThat(resultado.getSaldo())
                .isEqualByComparingTo("0.00");

        assertThat(resultado.getEstado())
                .isEqualTo(EstadoFactura.PAGADA);

        verify(abonoService, times(1))
                .registrarAbonoInicial(any());
    }

    @Test
    void debeLanzarExcepcionCuandoAbonoEsMayorAlTotal() {

        // given
        CabFactura factura = new CabFactura();
        factura.setTotal(new BigDecimal("100.00"));
        factura.setAbono(new BigDecimal("150.00"));

        // when / then
        assertThatThrownBy(() -> cabFacturaService.crearFactura(factura))
                .isInstanceOf(AbonoInvalidoException.class)
                .hasMessageContaining("abono");

        verify(cabFacturaRepository, never()).save(any());
        verify(abonoService, never()).registrarAbonoInicial(any());
    }

    @Test
    void actualizarFactura_noDebeModificarAbonos() {

        // given
        CabFactura facturaBD = new CabFactura();
        facturaBD.setIdFactura(1);
        facturaBD.setTotal(new BigDecimal("100.00"));
        facturaBD.setAbono(new BigDecimal("30.00"));
        facturaBD.setSaldo(new BigDecimal("70.00"));
        facturaBD.setEstado(EstadoFactura.PARCIAL);
        facturaBD.setDetalle("detalle viejo");

        CabFactura facturaActualizar = new CabFactura();
        facturaActualizar.setIdFactura(1);
        facturaActualizar.setDetalle("detalle nuevo");
        facturaActualizar.setTotal(new BigDecimal("120.00"));
        facturaActualizar.setSaldo(new BigDecimal("70.00"));

        when(cabFacturaRepository.findById(1))
                .thenReturn(java.util.Optional.of(facturaBD));

        when(cabFacturaRepository.save(any(CabFactura.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        CabFactura resultado = cabFacturaService.actualizarFactura(facturaActualizar);

        // then
        assertThat(resultado.getAbono())
                .isEqualByComparingTo("30.00");

        assertThat(resultado.getSaldo())
                .isEqualByComparingTo("70.00");

        assertThat(resultado.getEstado())
                .isEqualTo(EstadoFactura.PARCIAL);

        assertThat(resultado.getDetalle())
                .isEqualTo("detalle nuevo");

        verify(abonoService, never()).registrarAbonoInicial(any());
        verify(auditarService, times(1))
                .registrarMovimiento(any(), any(), any());
    }

    @Test
    void debeCrearFacturaConValoresNulosComoCero() {

        // given
        CabFactura factura = new CabFactura();
        factura.setTotal(null);
        factura.setAbono(null);

        when(cabFacturaRepository.save(any(CabFactura.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        CabFactura resultado = cabFacturaService.crearFactura(factura);

        // then
        assertThat(resultado.getTotal()).isEqualByComparingTo("0.00");
        assertThat(resultado.getAbono()).isEqualByComparingTo("0.00");
        assertThat(resultado.getSaldo()).isEqualByComparingTo("0.00");
        assertThat(resultado.getEstado()).isEqualTo(EstadoFactura.PAGADA);
    }

    @Test
    void actualizarFactura_cuandoSaldoEsCero_debeQuedarPagada() {

        // given
        CabFactura facturaBD = new CabFactura();
        facturaBD.setIdFactura(1);
        facturaBD.setSaldo(BigDecimal.ZERO);

        CabFactura actualizar = new CabFactura();
        actualizar.setIdFactura(1);
        actualizar.setSaldo(BigDecimal.ZERO);

        when(cabFacturaRepository.findById(1))
                .thenReturn(java.util.Optional.of(facturaBD));

        when(cabFacturaRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        CabFactura resultado = cabFacturaService.actualizarFactura(actualizar);

        // then
        assertThat(resultado.getEstado())
                .isEqualTo(EstadoFactura.PAGADA);
    }


*/
}
