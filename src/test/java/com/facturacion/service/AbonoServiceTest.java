package com.facturacion.service;

import com.facturacion.dto.AbonoDTO;
import com.facturacion.entity.Abono;
import com.facturacion.entity.CabFactura;
import com.facturacion.enums.EstadoFactura;
import com.facturacion.exception.AbonoInvalidoException;
import com.facturacion.repository.AbonoRepository;
import com.facturacion.repository.CabFacturaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AbonoServiceTest {

    @Mock
    private AbonoRepository abonoRepository;

    @Mock
    private CabFacturaRepository cabFacturaRepository;

    @Mock
    private AuditarService auditarService;

    @InjectMocks
    private AbonoService abonoService;

    @Test
    void debeRegistrarAbonoEnFacturaParcial() {

        // given
        CabFactura factura = new CabFactura();
        factura.setIdFactura(1);
        factura.setTotal(new BigDecimal("100.00"));
        factura.setAbono(new BigDecimal("20.00"));
        factura.setSaldo(new BigDecimal("80.00"));
        factura.setEstado(EstadoFactura.PARCIAL);

        AbonoDTO dto = new AbonoDTO();
        dto.setIdFactura(1);
        dto.setValorAbono(new BigDecimal("30.00"));

        when(cabFacturaRepository.findById(1))
                .thenReturn(Optional.of(factura));

        when(abonoRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Abono resultado = abonoService.registrarAbono(dto);

        // then
        assertThat(factura.getAbono())
                .isEqualByComparingTo("50.00");

        assertThat(factura.getSaldo())
                .isEqualByComparingTo("50.00");

        assertThat(factura.getEstado())
                .isEqualTo(EstadoFactura.PARCIAL);

        verify(abonoRepository, times(1)).save(any());
        verify(auditarService, times(1))
                .registrarMovimiento(any(), any(), any());
    }

    @Test
    void debeMarcarFacturaComoPagadaCuandoSaldoLlegaACero() {

        // given
        CabFactura factura = new CabFactura();
        factura.setIdFactura(1);
        factura.setTotal(new BigDecimal("100.00"));
        factura.setAbono(new BigDecimal("70.00"));
        factura.setSaldo(new BigDecimal("30.00"));
        factura.setEstado(EstadoFactura.PARCIAL);

        AbonoDTO dto = new AbonoDTO();
        dto.setIdFactura(1);
        dto.setValorAbono(new BigDecimal("30.00"));

        when(cabFacturaRepository.findById(1))
                .thenReturn(Optional.of(factura));

        // when
        abonoService.registrarAbono(dto);

        // then
        assertThat(factura.getSaldo())
                .isEqualByComparingTo("0.00");

        assertThat(factura.getEstado())
                .isEqualTo(EstadoFactura.PAGADA);
    }

    @Test
    void debeLanzarExcepcionSiAbonoEsMayorAlSaldo() {

        // given
        CabFactura factura = new CabFactura();
        factura.setIdFactura(1);
        factura.setSaldo(new BigDecimal("20.00"));
        factura.setEstado(EstadoFactura.PARCIAL);

        AbonoDTO dto = new AbonoDTO();
        dto.setIdFactura(1);
        dto.setValorAbono(new BigDecimal("50.00"));

        when(cabFacturaRepository.findById(1))
                .thenReturn(Optional.of(factura));

        // when / then
        assertThatThrownBy(() -> abonoService.registrarAbono(dto))
                .isInstanceOf(AbonoInvalidoException.class)
                .hasMessageContaining("saldo");

        verify(abonoRepository, never()).save(any());
    }

    @Test
    void noDebePermitirAbonarFacturaPagada() {

        // given
        CabFactura factura = new CabFactura();
        factura.setIdFactura(1);
        factura.setEstado(EstadoFactura.PAGADA);
        factura.setSaldo(BigDecimal.ZERO);

        AbonoDTO dto = new AbonoDTO();
        dto.setIdFactura(1);
        dto.setValorAbono(new BigDecimal("10.00"));

        when(cabFacturaRepository.findById(1))
                .thenReturn(Optional.of(factura));

        // when / then
        assertThatThrownBy(() -> abonoService.registrarAbono(dto))
                .isInstanceOf(AbonoInvalidoException.class)
                .hasMessageContaining("pagada");
    }

    @Test
    void debeRevertirAbonoCorrectamente() {

        // given
        CabFactura factura = new CabFactura();
        factura.setIdFactura(1);
        factura.setAbono(new BigDecimal("50.00"));
        factura.setSaldo(new BigDecimal("50.00"));
        factura.setEstado(EstadoFactura.PARCIAL);

        Abono abono = new Abono();
        abono.setIdAbono(10);
        abono.setValorAbono(new BigDecimal("20.00"));
        abono.setPkCabFactura(1);

        when(abonoRepository.findById(10))
                .thenReturn(Optional.of(abono));

        when(cabFacturaRepository.findById(1))
                .thenReturn(Optional.of(factura));

        // when
        abonoService.eliminarAbonoPorId(10);

        // then
        assertThat(factura.getAbono())
                .isEqualByComparingTo("30.00");

        assertThat(factura.getSaldo())
                .isEqualByComparingTo("70.00");

        assertThat(factura.getEstado())
                .isEqualTo(EstadoFactura.PARCIAL);

        verify(abonoRepository, times(1)).delete(abono);
    }
}

