package com.facturacion.service;

import com.facturacion.dto.HistorialAbonosDTO;
import com.facturacion.entity.Abono;
import com.facturacion.entity.CabFactura;
import com.facturacion.repository.AbonoRepository;
import com.facturacion.repository.CabFacturaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor
public class AbonoService {

    private static final String FUNCION_ABONOS = "Abonos";
    private static final String OPERACION_AGREGAR_ABONO = "Agregar abono";
    private final AbonoRepository abonoRepository;
    private final CabFacturaRepository cabFacturaRepository;
    private final AuditarService auditarService;

    public void abonarAFactura(CabFactura cabFactura) {
        if ((Objects.nonNull(cabFactura.getValAbonoIngresado()) &&
                cabFactura.getValAbonoIngresado().compareTo(BigDecimal.ZERO) > 0) ||
                (Objects.nonNull(cabFactura.getAbono()) &&
                        cabFactura.getAbono().compareTo(BigDecimal.ZERO) > 0)) {
            Abono logAbono = traducirFacturaToAbono(cabFactura);
            registrarAbono(logAbono, cabFactura);
        }
    }

    private Abono traducirFacturaToAbono(CabFactura cabFactura){
        Abono abono = Abono.builder()
                .valorAbono(cabFactura.getValAbonoIngresado().compareTo(BigDecimal.ZERO)>0 ? cabFactura.getValAbonoIngresado() : cabFactura.getAbono())
                .valAnterior(cabFactura.getValAbonoAnterior())
                .totalFacturaOriginal(cabFactura.getTotal())
                .pkCabFactura(Objects.isNull(cabFactura.getIdFactura()) ? cabFacturaRepository.generaFactura() : cabFactura.getIdFactura())
                .fechaAbono(LocalDateTime.now())
                .build();

        return abono;
    }

    public Abono registrarAbono(Abono abono, CabFactura cabFactura) {
        Abono abonoCreado = this.abonoRepository.save(abono);
        auditarService.registrarMovimiento(abonoCreado, FUNCION_ABONOS, OPERACION_AGREGAR_ABONO);
        if (Objects.nonNull(cabFactura)) {
            cabFactura.setAbono(Objects.isNull(cabFactura.getAbono()) ?
                    new BigDecimal(0).add(abono.getValorAbono()) :
                    cabFactura.getValAbonoAnterior().add(abono.getValorAbono()));
            this.cabFacturaRepository.save(cabFactura);
        }
        return abonoCreado;
    }

    public List<HistorialAbonosDTO> obtenerHistorialAbonos( ) {
        return this.abonoRepository.getAbonos();
    }
}
