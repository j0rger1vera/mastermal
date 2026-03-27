package com.facturacion.service;

import com.facturacion.dto.ActualizarFacturaRequestDTO;
import com.facturacion.dto.CrearFacturaRequestDTO;
import com.facturacion.dto.CabFacturaResponseDTO;
import com.facturacion.dto.DetFacturaRequestDTO;
import com.facturacion.dto.DetFacturaResponseDTO;
import com.facturacion.dto.FacturaDetalleResponseDTO;
import com.facturacion.dto.FacturacionGeneralDTO;
import com.facturacion.dto.HistorialAbonosDTO;
import com.facturacion.entity.CabFactura;
import com.facturacion.entity.DetFactura;
import com.facturacion.enums.EstadoFactura;
import com.facturacion.repository.CabFacturaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CabFacturaService {

    private static final String FACTURA = "Factura";
    private static final String CREAR_FACTURA = "Crear factura";
    private static final String MODIFICAR_FACTURA = "Modificar factura";
    private static final String FACTURA_NO_ENCONTRADA = "Factura no encontrada";

    private final CabFacturaRepository cabFacturaRepository;
    private final AuditarService auditarService;
    private final AbonoService abonoService;

    @Value("${facturacion.igv}")
    private BigDecimal igvPorcentaje;

    // =====================
    // CREACIÓN / ACTUALIZACIÓN
    // =====================
    @Transactional
    public CabFacturaResponseDTO crearFactura(CrearFacturaRequestDTO request) {
        CabFactura factura = mapToEntity(request);
        calcularTotales(factura);
        CabFactura guardada = crearFacturaInterno(factura);
        return mapToResponseDTO(guardada);
    }

    private CabFactura crearFacturaInterno(CabFactura factura) {

        BigDecimal total = factura.getTotal();
        BigDecimal abonoInicial = valorSeguro(factura.getAbono());

        validarAbonoInicial(total, abonoInicial);

        factura.setNumeroFactura(generarNumeroFactura());

        factura.setFecha(LocalDateTime.now());
        factura.setAbono(abonoInicial);

        BigDecimal saldo = total.subtract(abonoInicial);
        factura.setSaldo(saldo);
        factura.setEstado(determinarEstado(saldo, total));

        CabFactura facturaGuardada = cabFacturaRepository.save(factura);

        auditarService.registrarMovimiento(
                facturaGuardada,
                FACTURA,
                CREAR_FACTURA
        );

        if (abonoInicial.compareTo(BigDecimal.ZERO) > 0) {
            abonoService.registrarAbonoInicial(facturaGuardada);
        }

        return facturaGuardada;
    }

    @Transactional
    public CabFacturaResponseDTO actualizarFactura(ActualizarFacturaRequestDTO request) {

        CabFactura facturaBD = cabFacturaRepository.findById(request.getIdFactura())
                .orElseThrow(() -> new IllegalArgumentException(FACTURA_NO_ENCONTRADA));

        // 1️⃣ Actualizar descripción
        if (request.getDetalle() != null) {
            facturaBD.setDetalle(request.getDetalle());
        }

        // 2️⃣ Reemplazar detalles
        if (facturaBD.getDetFactura() == null) {
            facturaBD.setDetFactura(new ArrayList<>());
        } else {
            facturaBD.getDetFactura().clear();
        }

        List<DetFactura> nuevosDetalles = request.getDetalles().stream()
                .map(det -> {

                    BigDecimal totalProducto = det.getValUnitarioProducto()
                            .multiply(BigDecimal.valueOf(det.getCantidad()));

                    DetFactura detalle = new DetFactura();
                    detalle.setCodigoProducto(det.getCodigoProducto());
                    detalle.setCantidad(det.getCantidad());
                    detalle.setValUnitarioProducto(det.getValUnitarioProducto());
                    detalle.setValTotalProducto(totalProducto);
                    detalle.setPkCabFactura(facturaBD);

                    return detalle;
                })
                .toList();

        facturaBD.getDetFactura().addAll(nuevosDetalles);

        // 3️⃣ Recalcular totales
        recalcularTotalesYEstado(facturaBD);

        CabFactura actualizada = cabFacturaRepository.save(facturaBD);

        auditarService.registrarMovimiento(
                actualizada,
                FACTURA,
                MODIFICAR_FACTURA
        );

        return mapToResponseDTO(actualizada);
    }

    @Transactional
    private Long generarNumeroFactura() {
        Long ultimoNumero = cabFacturaRepository.obtenerUltimoNumeroFactura();
        return ultimoNumero + 1;
    }

    public CabFacturaResponseDTO mapToResponseDTO(CabFactura factura) {

        return CabFacturaResponseDTO.builder()
                .idFactura(factura.getIdFactura())
                .fecha(factura.getFecha())
                .detalle(factura.getDetalle())
                .total(factura.getTotal())
                .abono(factura.getAbono())
                .saldo(factura.getSaldo())
                .estado(factura.getEstado())
                .build();
    }

    private CabFactura mapToEntity(CrearFacturaRequestDTO request) {

        CabFactura factura = new CabFactura();

        factura.setRucCliente(request.getRucCliente());
        factura.setDetalle(request.getDetalle());

        List<DetFactura> detalles = request.getDetalles().stream()
                .map(det -> {
                    DetFactura detalle = new DetFactura();
                    detalle.setCodigoProducto(det.getCodigoProducto());
                    detalle.setCantidad(det.getCantidad());
                    detalle.setValUnitarioProducto(det.getValUnitarioProducto());

                    BigDecimal totalProducto = det.getValUnitarioProducto()
                            .multiply(BigDecimal.valueOf(det.getCantidad()));

                    detalle.setValTotalProducto(totalProducto);
                    detalle.setPkCabFactura(factura);

                    return detalle;
                })
                .toList();

        factura.setDetFactura(detalles);

        factura.setAbono(valorSeguro(request.getAbonoInicial()));

        return factura;
    }


    // =====================
    // CONSULTAS (USADAS POR EL FRONT)
    // =====================

    public Optional<CabFactura> obtenerPorId(Integer id) {
        return cabFacturaRepository.findById(id);
    }

    public List<FacturacionGeneralDTO> listarFacturas() {
        return cabFacturaRepository.getBalanceGeneral();
    }

    public List<FacturacionGeneralDTO> listarPorCliente(String nitCliente) {
        return cabFacturaRepository.getFacturaPorUnCliente(nitCliente);
    }

    public List<FacturacionGeneralDTO> obtenerFacturasConSaldos() {
        return cabFacturaRepository.getFacturasSaldosPorClientes();
    }

    public List<FacturacionGeneralDTO> consultarSaldosPorCobrar() {
        return cabFacturaRepository.getSaldosPorCobrar();
    }

    public Integer generaFactura() {
        return cabFacturaRepository.generaFactura();
    }

    public FacturaDetalleResponseDTO obtenerDetalleFactura(Integer idFactura) {

        CabFactura factura = cabFacturaRepository.findById(idFactura)
                .orElseThrow(() -> new IllegalArgumentException(FACTURA_NO_ENCONTRADA));

        List<DetFacturaResponseDTO> detalles = factura.getDetFactura().stream()
                .map(det -> DetFacturaResponseDTO.builder()
                        .id(det.getId())
                        .codigoProducto(det.getCodigoProducto())
                        .cantidad(det.getCantidad())
                        .valUnitarioProducto(det.getValUnitarioProducto())
                        .valTotalProducto(det.getValTotalProducto())
                        .build())
                .toList();

        List<HistorialAbonosDTO> abonos = abonoService.obtenerHistorialPorFactura(factura.getNumeroFactura().intValue());

        return FacturaDetalleResponseDTO.builder()
                .idFactura(factura.getIdFactura())
                .numeroFactura(factura.getNumeroFactura())
                .fecha(factura.getFecha())
                .rucCliente(factura.getRucCliente())
                .detalle(factura.getDetalle())
                .subtotal(factura.getSubtotal())
                .igv(factura.getIgv())
                .total(factura.getTotal())
                .abono(factura.getAbono())
                .saldo(factura.getSaldo())
                .estado(factura.getEstado())
                .detalles(detalles)
                .abonos(abonos)
                .build();
    }

    // =====================
    // ELIMINACIÓN
    // =====================

    @Transactional
    public void eliminarPorId(Integer id) {
        CabFactura factura = cabFacturaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(FACTURA_NO_ENCONTRADA));
        cabFacturaRepository.delete(factura);
    }

    // =====================
    // REGLAS DE NEGOCIO
    // =====================

    private void validarAbonoInicial(BigDecimal total, BigDecimal abono) {

        if (abono.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El abono no puede ser negativo");
        }

        if (abono.compareTo(total) > 0) {
            throw new IllegalArgumentException("El abono no puede ser mayor al total");
        }
    }

    private EstadoFactura determinarEstado(BigDecimal saldo, BigDecimal total) {

        if (saldo.compareTo(total) == 0) {
            return EstadoFactura.PENDIENTE;
        }

        if (saldo.compareTo(BigDecimal.ZERO) == 0) {
            return EstadoFactura.PAGADA;
        }

        return EstadoFactura.PARCIAL;
    }

    // =====================
    // UTILIDAD
    // =====================

    private BigDecimal valorSeguro(BigDecimal valor) {
        return valor != null ? valor : BigDecimal.ZERO;
    }

    private void calcularTotales(CabFactura factura) {

        BigDecimal subtotal = factura.getDetFactura().stream()
                .map(DetFactura::getValTotalProducto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal igv = subtotal.multiply(igvPorcentaje);
        BigDecimal total = subtotal.add(igv);

        factura.setSubtotal(subtotal);
        factura.setIgv(igv);
        factura.setTotal(total);
    }

    private void actualizarDetalles(CabFactura factura,
                                    List<DetFacturaRequestDTO> nuevosDetalles) {

        factura.getDetFactura().clear();

        List<DetFactura> detalles = nuevosDetalles.stream()
                .map(det -> {

                    BigDecimal totalProducto = det.getValUnitarioProducto()
                            .multiply(BigDecimal.valueOf(det.getCantidad()));

                    DetFactura detalle = new DetFactura();
                    detalle.setCodigoProducto(det.getCodigoProducto());
                    detalle.setCantidad(det.getCantidad());
                    detalle.setValUnitarioProducto(det.getValUnitarioProducto());
                    detalle.setValTotalProducto(totalProducto);
                    detalle.setPkCabFactura(factura);

                    return detalle;
                })
                .toList();

        factura.getDetFactura().addAll(detalles);
    }

    private void recalcularTotalesYEstado(CabFactura factura) {

        BigDecimal subtotal = factura.getDetFactura().stream()
                .map(DetFactura::getValTotalProducto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal igv = subtotal.multiply(igvPorcentaje);
        BigDecimal total = subtotal.add(igv);

        factura.setSubtotal(subtotal);
        factura.setIgv(igv);
        factura.setTotal(total);

        BigDecimal abonoActual = valorSeguro(factura.getAbono());

        if (abonoActual.compareTo(total) > 0) {
            throw new IllegalStateException(
                    "El nuevo total no puede ser menor al abono ya registrado"
            );
        }

        BigDecimal saldo = total.subtract(abonoActual);

        factura.setSaldo(saldo);
        factura.setEstado(determinarEstado(saldo, total));
    }
}