package com.facturacion.service;

import com.facturacion.dto.HistorialAbonosDTO;
import com.facturacion.entity.Abono;
import com.facturacion.entity.CabFactura;
import com.facturacion.repository.AbonoRepository;
import com.facturacion.repository.CabFacturaRepository;
import com.facturacion.util.TipoDataConverter;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor
public class AbonoService {

    private final AbonoRepository abonoRepository;
    private final CabFacturaRepository cabFacturaRepository;
    private final AuditarService auditarService;
    private final TipoDataConverter tipoDataConverter;

    public void abonarAFactura(CabFactura cabFactura) {
        BigDecimal valAbonoIngresado = tipoDataConverter.toBigDecimal(cabFactura.getValAbonoIngresado());

        if (valAbonoIngresado.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        CabFactura facturaActual =
                cabFacturaRepository.findById(cabFactura.getIdFactura())
                        .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada"));

        BigDecimal total = tipoDataConverter.toBigDecimal(facturaActual.getTotal());
        BigDecimal abonoActual = tipoDataConverter.toBigDecimal(facturaActual.getAbono());

        BigDecimal nuevoAbono = abonoActual.add(valAbonoIngresado);

        validarAbono(total, nuevoAbono);

        BigDecimal nuevoSaldo = total.subtract(nuevoAbono);

        facturaActual.setValAbonoAnterior(tipoDataConverter.toMoneyString(abonoActual));
        facturaActual.setValAbonoIngresado(tipoDataConverter.toMoneyString(valAbonoIngresado));
        facturaActual.setAbono(tipoDataConverter.toMoneyString(nuevoAbono));
        facturaActual.setSaldo(tipoDataConverter.toMoneyString(nuevoSaldo));
        facturaActual.setRucCliente(Objects.nonNull(cabFactura.getRucCliente()) ? cabFactura.getRucCliente() : facturaActual.getRucCliente());

        Abono logAbono = tipoDataConverter.traducirFacturaToAbono(facturaActual);

        CabFactura facturaGuardada = this.cabFacturaRepository.save(facturaActual);
        registrarAbono(logAbono);

        auditarService.registrarMovimiento(facturaGuardada, "Factura", "Abonar factura");
    }

    private void validarAbono(BigDecimal total, BigDecimal abono) {
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El total no puede ser negativo");
        }

        if (abono.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El abono no puede ser negativo");
        }

        if (abono.compareTo(total) > 0) {
            throw new IllegalArgumentException("El abono no puede ser mayor al total de la factura");
        }
    }

    public Abono registrarAbono(Abono abono) {
        Abono abonoCreado = this.abonoRepository.save(abono);
        auditarService.registrarMovimiento(abonoCreado, "Abonos", "Agregar abono");
        return abonoCreado;
    }

    public List<HistorialAbonosDTO> obtenerHistorialAbonos( ) {
        return this.abonoRepository.getAbonos();
    }
}
