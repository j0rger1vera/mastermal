package com.facturacion.service;

import com.facturacion.dto.HistorialAbonosDTO;
import com.facturacion.entity.Abono;
import com.facturacion.entity.CabFactura;
import com.facturacion.repository.AbonoRepository;
import com.facturacion.repository.CabFacturaRepository;
import com.facturacion.util.TipoDataConverter;
import jakarta.transaction.Transactional;
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

    @Transactional
    public void abonarAFactura(CabFactura cabFactura) {

        if (cabFactura == null
                || cabFactura.getIdFactura() == null) {
            throw new IllegalArgumentException(
                    "Debe indicar la factura a abonar");
        }

        BigDecimal valAbonoIngresado = cabFactura.getValAbonoIngresado();

        if (valAbonoIngresado == null
                || valAbonoIngresado.compareTo(
                BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "El valor del abono debe ser mayor que cero");
        }

        CabFactura facturaActual =
                cabFacturaRepository.findById(cabFactura.getIdFactura())
                        .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada"));

        BigDecimal total =
                facturaActual.getTotal() != null
                        ? facturaActual.getTotal()
                        : BigDecimal.ZERO;

        BigDecimal abonoActual =
                facturaActual.getAbono() != null
                        ? facturaActual.getAbono()
                        : BigDecimal.ZERO;

        BigDecimal nuevoAbono = abonoActual.add(valAbonoIngresado);

        validarAbono(total, nuevoAbono);

        BigDecimal nuevoSaldo = total.subtract(nuevoAbono);

        facturaActual.setValAbonoAnterior(abonoActual);
        facturaActual.setValAbonoIngresado(valAbonoIngresado);
        facturaActual.setAbono(nuevoAbono);
        facturaActual.setSaldo(total.subtract(nuevoAbono));
        if (cabFactura.getRucCliente() != null) {
            facturaActual.setRucCliente(
                    cabFactura.getRucCliente());
        }

        CabFactura facturaGuardada = this.cabFacturaRepository.save(facturaActual);

        Abono logAbono = tipoDataConverter.traducirFacturaToAbono(facturaActual);

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

        System.out.println("===== REGISTRAR ABONO =====");
        System.out.println("ID recibido: " + abono.getIdAbono());
        System.out.println("Factura: " + abono.getPkCabFactura());
        System.out.println("Valor: " + abono.getValorAbono());

        Abono abonoCreado = this.abonoRepository.save(abono);

        System.out.println("ID generado: " + abonoCreado.getIdAbono());
        auditarService.registrarMovimiento(abonoCreado, "Abonos", "Agregar abono");
        return abonoCreado;
    }

    public List<HistorialAbonosDTO> obtenerHistorialAbonos( ) {
        return this.abonoRepository.getAbonos();
    }
}
