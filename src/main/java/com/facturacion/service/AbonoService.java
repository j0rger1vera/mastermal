package com.facturacion.service;

import com.facturacion.dto.AbonoDTO;
import com.facturacion.dto.HistorialAbonosDTO;
import com.facturacion.entity.Abono;
import com.facturacion.entity.CabFactura;
import com.facturacion.enums.EstadoFactura;
import com.facturacion.exception.AbonoInvalidoException;
import com.facturacion.repository.AbonoRepository;
import com.facturacion.repository.CabFacturaRepository;
import com.facturacion.repository.HistorialAbonosView;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@AllArgsConstructor
public class AbonoService {

    private static final String FUNCION_ABONOS = "Abonos";
    private static final String OPERACION_AGREGAR_ABONO = "Agregar abono";

    private final AbonoRepository abonoRepository;
    private final CabFacturaRepository cabFacturaRepository;
    private final AuditarService auditarService;

    // =====================
    // ABONO NORMAL
    // =====================

    @Transactional
    public Abono registrarAbono(AbonoDTO request) {

        CabFactura factura = cabFacturaRepository.findById(request.getIdFactura())
                .orElseThrow(() -> new AbonoInvalidoException("Factura no encontrada"));

        validarFacturaParaAbono(factura, request.getValorAbono());

        return registrarAbonoDesdeFactura(
                factura,
                request.getValorAbono(),
                OPERACION_AGREGAR_ABONO
        );
    }

    // =====================
    // ABONO INICIAL
    // =====================

    @Transactional
    public void registrarAbonoInicial(CabFactura factura) {

        if (factura.getAbono().compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        validarFacturaParaAbono(factura, factura.getAbono());

        Abono abono = Abono.builder()
                .valorAbono(factura.getAbono())
                .valAnterior(BigDecimal.ZERO)
                .totalFacturaOriginal(factura.getTotal())
                .pkCabFactura(factura.getIdFactura())
                .build();

        abonoRepository.save(abono);

        auditarService.registrarMovimiento(
                abono,
                FUNCION_ABONOS,
                "Agregar abono inicial"
        );
    }

    // =====================
    // LÓGICA CENTRAL
    // =====================

    private Abono registrarAbonoDesdeFactura(
            CabFactura factura,
            BigDecimal valorAbono,
            String operacion
    ) {

        BigDecimal abonoActual = valorSeguro(factura.getAbono());
        BigDecimal nuevoAbono = abonoActual.add(valorAbono);
        BigDecimal nuevoSaldo = factura.getSaldo().subtract(valorAbono);

        Abono abono = Abono.builder()
                .valorAbono(valorAbono)
                .valAnterior(abonoActual)
                .totalFacturaOriginal(factura.getTotal())
                .pkCabFactura(factura.getIdFactura())
                .build();

        abonoRepository.save(abono);

        factura.setAbono(nuevoAbono);
        factura.setSaldo(nuevoSaldo);
        factura.setEstado(determinarEstado(nuevoSaldo, factura.getTotal()));

        cabFacturaRepository.save(factura);

        auditarService.registrarMovimiento(
                abono,
                FUNCION_ABONOS,
                operacion
        );

        return abono;
    }

    // =====================
    // REVERSO
    // =====================

    @Transactional
    public void eliminarAbonoPorId(Integer abonoId) {

        Abono abono = abonoRepository.findById(abonoId)
                .orElseThrow(() -> new AbonoInvalidoException("Abono no encontrado"));

        CabFactura factura = cabFacturaRepository.findById(abono.getPkCabFactura())
                .orElseThrow(() -> new AbonoInvalidoException("Factura no encontrada"));

        factura.setAbono(factura.getAbono().subtract(abono.getValorAbono()));
        factura.setSaldo(factura.getSaldo().add(abono.getValorAbono()));
        factura.setEstado(determinarEstado(factura.getSaldo(), factura.getTotal()));

        cabFacturaRepository.save(factura);
        abonoRepository.delete(abono);

        auditarService.registrarMovimiento(
                abono,
                FUNCION_ABONOS,
                "Eliminar abono"
        );
    }

    // =====================
    // CONSULTA
    // =====================

    public List<HistorialAbonosView> obtenerHistorialAbonos() {
        return abonoRepository.obtenerHistoricoAbonos();
    }

    public List<HistorialAbonosDTO> obtenerHistorialPorFactura(Integer numFactura) {
        return abonoRepository.obtenerHistorialPorFactura(numFactura);
    }

    // =====================
    // VALIDACIONES
    // =====================

    private void validarFacturaParaAbono(CabFactura factura, BigDecimal valorAbono) {

        if (factura.getEstado() == EstadoFactura.PAGADA) {
            throw new AbonoInvalidoException(
                    "No se puede registrar abonos en una factura pagada"
            );
        }

        if (valorAbono.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AbonoInvalidoException(
                    "El valor del abono debe ser mayor a cero"
            );
        }

        if (valorAbono.compareTo(factura.getSaldo()) > 0) {
            throw new AbonoInvalidoException(
                    "El abono no puede ser mayor al saldo pendiente"
            );
        }
    }

    private EstadoFactura determinarEstado(BigDecimal saldo, BigDecimal total) {

        if (saldo.compareTo(total) == 0) {
            return EstadoFactura.PENDIENTE;
        }

        if (saldo.compareTo(BigDecimal.ZERO) == 0) {
            return EstadoFactura.PAGADA;
        }

        if (saldo.compareTo(BigDecimal.ZERO) > 0
                && saldo.compareTo(total) < 0) {
            return EstadoFactura.PARCIAL;
        }

        throw new IllegalStateException("Estado financiero inconsistente");
    }

    private BigDecimal valorSeguro(BigDecimal valor) {
        return valor != null ? valor : BigDecimal.ZERO;
    }

}