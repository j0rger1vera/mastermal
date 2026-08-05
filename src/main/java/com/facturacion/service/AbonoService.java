package com.facturacion.service;

import com.facturacion.dto.DispersarAbonoRequest;
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

    @Transactional
    public void dispersarAbono(DispersarAbonoRequest request) {

        validarSolicitudDispersion(request);

        List<CabFactura> facturasPendientes =
                cabFacturaRepository.buscarFacturasPendientesParaAbono(
                        request.getClienteId());

        if (facturasPendientes.isEmpty()) {
            throw new IllegalArgumentException(
                    "El cliente no tiene facturas con saldo pendiente");
        }

        BigDecimal saldoTotalCliente = facturasPendientes.stream()
                .map(CabFactura::getSaldo)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (request.getValorAbono().compareTo(saldoTotalCliente) > 0) {
            throw new IllegalArgumentException(
                    "El valor del abono supera el saldo pendiente del cliente");
        }

        BigDecimal abonoRestante = request.getValorAbono();

        for (CabFactura factura : facturasPendientes) {

            if (abonoRestante.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal saldoFactura = valorSeguro(factura.getSaldo());

            if (saldoFactura.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            BigDecimal valorAplicar =
                    abonoRestante.min(saldoFactura);

            aplicarAbonoAFactura(
                    factura,
                    valorAplicar);

            abonoRestante =
                    abonoRestante.subtract(valorAplicar);
        }

        if (abonoRestante.compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalStateException(
                    "No fue posible distribuir completamente el abono. " +
                            "Valor pendiente de aplicar: " + abonoRestante);
        }
    }

    private void validarSolicitudDispersion(
            DispersarAbonoRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "La solicitud de abono es obligatoria");
        }

        if (request.getClienteId() == null
                || request.getClienteId().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Debe indicar el cliente");
        }

        if (request.getValorAbono() == null) {
            throw new IllegalArgumentException(
                    "Debe indicar el valor del abono");
        }

        if (request.getValorAbono()
                .compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "El valor del abono debe ser mayor que cero");
        }
    }

    private void aplicarAbonoAFactura(
            CabFactura factura,
            BigDecimal valorAbono) {

        BigDecimal totalFactura =
                valorSeguro(factura.getTotal());

        BigDecimal abonoActual =
                valorSeguro(factura.getAbono());

        BigDecimal nuevoAbono =
                abonoActual.add(valorAbono);

        validarAbono(totalFactura, nuevoAbono);

        BigDecimal nuevoSaldo =
                totalFactura.subtract(nuevoAbono);

        factura.setValAbonoAnterior(abonoActual);
        factura.setValAbonoIngresado(valorAbono);
        factura.setAbono(nuevoAbono);
        factura.setSaldo(nuevoSaldo);

        CabFactura facturaGuardada =
                cabFacturaRepository.save(factura);

        Abono movimientoAbono =
                tipoDataConverter.traducirFacturaToAbono(
                        facturaGuardada);

        registrarAbono(movimientoAbono);

        auditarService.registrarMovimiento(
                facturaGuardada,
                "Factura",
                "Abonar factura");
    }

    private BigDecimal valorSeguro(BigDecimal valor) {
        return valor != null
                ? valor
                : BigDecimal.ZERO;
    }
}
