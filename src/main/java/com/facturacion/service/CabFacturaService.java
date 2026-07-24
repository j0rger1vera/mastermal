package com.facturacion.service;

import com.facturacion.dto.DashboardClienteSaldoDTO;
import com.facturacion.dto.DashboardResumenDTO;
import com.facturacion.dto.FacturacionGeneralDTO;
import com.facturacion.entity.Abono;
import com.facturacion.entity.CabFactura;
import com.facturacion.repository.AbonoRepository;
import com.facturacion.repository.CabFacturaRepository;
import com.facturacion.util.TipoDataConverter;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

@Slf4j
@Service
@AllArgsConstructor
public class CabFacturaService {

    private final CabFacturaRepository cabFacturaRepository;
    private final AuditarService auditarService;
    private final TipoDataConverter tipoDataConverter;
    private final AbonoService abonoService;
    private final AbonoRepository abonoRepository;


    public CabFactura guardarCabFactura(CabFactura cabFactura) {

        log.info("======================================");
        log.info("ZoneId.systemDefault(): {}", ZoneId.systemDefault());

        log.info("LocalDateTime.now(): {}", LocalDateTime.now());

        log.info("Instant.now(): {}", Instant.now());

        log.info("Bogota: {}",
                ZonedDateTime.now(ZoneId.of("America/Bogota")));

        log.info("Montevideo: {}",
                ZonedDateTime.now(ZoneId.of("America/Montevideo")));

        log.info("UTC: {}",
                ZonedDateTime.now(ZoneOffset.UTC));

        log.info("======================================");

        LocalDateTime ahora = LocalDateTime.now();

        cabFactura.setFecha(LocalDateTime.now().toString().replace('T', ' ').substring(0, 19));
        cabFactura.setFechaCreacion(ahora);

        BigDecimal total = cabFactura.getTotal();
        BigDecimal abono = cabFactura.getAbono();

        validarAbono(total, abono);

        BigDecimal saldo = total.subtract(abono);

        cabFactura.setTotal(total);
        cabFactura.setAbono(abono);
        cabFactura.setSaldo(saldo);
        cabFactura.setDetalle(ObjectUtils.isEmpty(cabFactura.getDetalle()) ? "" : cabFactura.getDetalle());
        cabFactura.setValAbonoAnterior(cabFactura.getValAbonoAnterior());
        cabFactura.setValAbonoIngresado(cabFactura.getValAbonoIngresado());

        CabFactura facturaGuardada = this.cabFacturaRepository.save(cabFactura);
        auditarService.registrarMovimiento(facturaGuardada, "Factura", "Crear factura");

        return facturaGuardada;
    }

    @Transactional
    public void actualizarFacturaConAbonoOpcional(CabFactura cabFactura) {
        BigDecimal valAbonoIngresado =
                cabFactura.getValAbonoIngresado() == null
                        ? BigDecimal.ZERO
                        : cabFactura.getValAbonoIngresado();

        //1. Cargar factura actual desde BD
        CabFactura facturaActual =
                cabFacturaRepository.findById(cabFactura.getIdFactura())
                        .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada"));

        System.out.println("\n========== ACTUALIZAR FACTURA ==========");
        System.out.println("Factura ID: " + cabFactura.getIdFactura());
        System.out.println("Abono BD: " + facturaActual.getAbono());
        System.out.println("Abono recibido: " + cabFactura.getAbono());
        System.out.println("ValAbonoIngresado: " + cabFactura.getValAbonoIngresado());

        BigDecimal abonoActual =
                facturaActual.getAbono() == null
                        ? BigDecimal.ZERO
                        : facturaActual.getAbono();

        BigDecimal abonoManual =
                cabFactura.getAbono() == null
                        ? abonoActual
                        : cabFactura.getAbono();

        boolean tieneNuevoAbono = valAbonoIngresado.compareTo(BigDecimal.ZERO) > 0;
        boolean ajustaAbonoManual = abonoManual.compareTo(abonoActual) != 0;

        System.out.println("tieneNuevoAbono = " + tieneNuevoAbono);
        System.out.println("ajustaAbonoManual = " + ajustaAbonoManual);

        if (tieneNuevoAbono && ajustaAbonoManual) {
            throw new IllegalArgumentException(
                    "No puede ajustar el abono acumulado y registrar un nuevo abono en la misma operación.");
        }

        // 2. Actualizar campos editables
        facturaActual.setFecha(cabFactura.getFecha());
        if (cabFactura.getRucCliente() != null && !cabFactura.getRucCliente().trim().isEmpty()) {
            facturaActual.setRucCliente(cabFactura.getRucCliente());
        }
        facturaActual.setDetalle(cabFactura.getDetalle());
        facturaActual.setSubtotal(cabFactura.getSubtotal());
        facturaActual.setIgv(cabFactura.getIgv());
        facturaActual.setTotal(cabFactura.getTotal());

        // 3. Recalcular saldo según total nuevo y abono actual
        BigDecimal total = facturaActual.getTotal();

        if (tieneNuevoAbono) {
            System.out.println(">>> ENTRO AL BLOQUE: NUEVO ABONO");
            BigDecimal nuevoAbono = abonoActual.add(valAbonoIngresado);
            validarAbono(total, nuevoAbono);

            facturaActual.setValAbonoAnterior(abonoActual);
            facturaActual.setValAbonoIngresado(valAbonoIngresado);
            facturaActual.setAbono(nuevoAbono);
            facturaActual.setSaldo(total.subtract(nuevoAbono));

            Abono logAbono = tipoDataConverter.traducirFacturaToAbono(facturaActual);

            CabFactura facturaGuardada = cabFacturaRepository.save(facturaActual);

            System.out.println("Abono a registrar:");
            System.out.println("ID Abono: " + logAbono.getIdAbono());
            System.out.println("Factura: " + logAbono.getPkCabFactura());
            System.out.println("Valor: " + logAbono.getValorAbono());
            System.out.println("Fecha: " + logAbono.getFechaAbono());

            abonoService.registrarAbono(logAbono);
            auditarService.registrarMovimiento(facturaGuardada, "Factura", "Abonar factura");

            return;
        }

        if (ajustaAbonoManual) {
            System.out.println(">>> ENTRO AL BLOQUE: AJUSTE MANUAL");
            validarAbono(total, abonoManual);

            facturaActual.setValAbonoAnterior(abonoActual);
            facturaActual.setValAbonoIngresado(new BigDecimal(0));
            facturaActual.setAbono(abonoManual);
            facturaActual.setSaldo(total.subtract(abonoManual));

            CabFactura facturaGuardada = cabFacturaRepository.save(facturaActual);
            auditarService.registrarMovimiento(facturaGuardada, "Factura", "Ajustar abono acumulado");

            return;
        }

        System.out.println(">>> ENTRO AL BLOQUE: MODIFICAR FACTURA");

        validarAbono(total, abonoActual);

        facturaActual.setValAbonoIngresado(new BigDecimal(0));
        facturaActual.setSaldo(total.subtract(abonoActual));

        CabFactura facturaGuardada = cabFacturaRepository.save(facturaActual);
        auditarService.registrarMovimiento(facturaGuardada, "Factura", "Modificar factura");
    }


    public void actualizarFactura(CabFactura cabFactura) {
        CabFactura facturaActual =
                cabFacturaRepository.findById(cabFactura.getIdFactura())
                        .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada"));

        BigDecimal total = 
                ObjectUtils.isEmpty(cabFactura.getTotal())
                        ? facturaActual.getTotal()
                        : cabFactura.getTotal();

        BigDecimal abono = 
                ObjectUtils.isEmpty(cabFactura.getAbono())
                        ? facturaActual.getAbono()
                        : cabFactura.getAbono();

        validarAbono(total, abono);

        BigDecimal saldo = total.subtract(abono);

        facturaActual.setTotal(total);
        facturaActual.setAbono(abono);
        facturaActual.setSaldo(saldo);

        facturaActual.setDetalle(
                ObjectUtils.isEmpty(cabFactura.getDetalle())
                        ? facturaActual.getDetalle()
                        : cabFactura.getDetalle()
        );

        facturaActual.setRucCliente(cabFactura.getRucCliente());

        CabFactura facturaGuardada = this.cabFacturaRepository.save(facturaActual);
        auditarService.registrarMovimiento(facturaGuardada, "Factura", "Modificar factura");
    }

    public Optional<CabFactura> obtenerPorId(Integer id) {
        return this.cabFacturaRepository.findById(id);
    }

    public void eliminarPorId(Integer id) {
        this.cabFacturaRepository.deleteById(id);
    }

    public Integer generaFactura() {
        return this.cabFacturaRepository.generaFactura();
    }

    public List<FacturacionGeneralDTO> obtenerBalanceGeneral( ) {
        List<FacturacionGeneralDTO> listaFacturacion = this.cabFacturaRepository.getFacturasConSaldos();

        Map<String, FacturacionGeneralDTO> agrupados = new HashMap<>();

        listaFacturacion.forEach(dto -> {
            // Verificar si ya existe un objeto con el mismo nitCliente
            if (agrupados.containsKey(dto.getRucCliente())) {
                // Si existe, sumar los valores
                FacturacionGeneralDTO existente = agrupados.get(dto.getRucCliente());
                existente.setSaldo(existente.getSaldo().add(dto.getSaldo()));//controlar nulos
                existente.setAbono(existente.getAbono().add(dto.getAbono()));
                existente.setTotal(existente.getTotal().add(dto.getTotal()));
                agrupados.put(dto.getRucCliente(), existente);
            } else {
                // Si no existe, agregar el objeto al mapa
                FacturacionGeneralDTO nuevaFacturaDto = new FacturacionGeneralDTO();
                nuevaFacturaDto.setRucCliente(dto.getRucCliente());
                nuevaFacturaDto.setNombreCliente(dto.getNombreCliente().toLowerCase());
                nuevaFacturaDto.setSaldo(dto.getSaldo());
                nuevaFacturaDto.setAbono(dto.getAbono());
                nuevaFacturaDto.setTotal(dto.getTotal());
                nuevaFacturaDto.setFecha(dto.getFecha());
                agrupados.put(dto.getRucCliente(), nuevaFacturaDto);
            }
        });

        // Convertir el mapa a una lista
        return new ArrayList<>(agrupados.values());
    }

    public List<FacturacionGeneralDTO> obtenerTodasFacturas( ) {
        return this.cabFacturaRepository.getBalanceGeneral();
    }

    public List<FacturacionGeneralDTO> obtenerPorNitCliente(String nitCliente) {
        return this.cabFacturaRepository.getFacturaPorUnCliente(nitCliente);
    }

    public List<FacturacionGeneralDTO> obtenerFacturasConSaldos( ) {
        return this.cabFacturaRepository.getFacturasSaldosPorClientes();
    }

    public List<FacturacionGeneralDTO> consultarSaldosPorCobrar( ) {
        return this.cabFacturaRepository.getSaldosPorCobrar();
    }

    /*logica temporal borrar cuando este estable la app*/

    public List<FacturacionGeneralDTO> obtenerFacturasSabado() {
        return cabFacturaRepository.getBalanceGeneralSabado();
    }

    public List<FacturacionGeneralDTO> consultarSaldosPorCobrarSabado() {
        return this.cabFacturaRepository.getSaldosPorCobrarSabado();
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

    public DashboardResumenDTO obtenerDashboardResumen() {
        Object[] row = cabFacturaRepository.getDashboardResumen().get(0);

        DashboardResumenDTO dto = new DashboardResumenDTO();
            dto.setTotalFacturado(toBigDecimal(row[0]));
        dto.setTotalAbonado(toBigDecimal(row[1]));
        dto.setTotalSaldo(toBigDecimal(row[2]));
        dto.setCantidadFacturas(((Number) row[3]).longValue());

        return dto;
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }

        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }

        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }

        return new BigDecimal(value.toString());
    }

    public List<DashboardClienteSaldoDTO> obtenerTopClientesSaldo2026() {
        List<DashboardClienteSaldoDTO> topClientes = cabFacturaRepository.getTopClientesSaldo2026()
                .stream()
                .map(row -> new DashboardClienteSaldoDTO(
                        row[0] != null ? row[0].toString().toUpperCase() : "SIN NOMBRE",
                        toBigDecimal(row[1]),
                        toBigDecimal(row[2])
                ))
                .toList();

        BigDecimal saldoTotal = cabFacturaRepository.getSaldoTotal2026();

        BigDecimal saldoTopClientes = topClientes.stream()
                .map(DashboardClienteSaldoDTO::getSaldo)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal saldoOtros = saldoTotal.subtract(saldoTopClientes);

        if (saldoOtros.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal porcentajeOtros = saldoOtros
                    .multiply(BigDecimal.valueOf(100))
                    .divide(saldoTotal, 2, RoundingMode.HALF_UP);

            List<DashboardClienteSaldoDTO> resultado = new ArrayList<>(topClientes);
            resultado.add(new DashboardClienteSaldoDTO(
                    "OTROS CLIENTES",
                    saldoOtros,
                    porcentajeOtros
            ));

            return resultado;
        }

        return topClientes;
    }


    @Transactional
    public void reversarAbono(Integer idAbono) {
        Abono abono = abonoRepository.findById(idAbono)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No se encontró el abono con id: " + idAbono
                        )
                );

        Integer idFactura = abono.getPkCabFactura();

        CabFactura factura = cabFacturaRepository.findById(idFactura)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No se encontró la factura asociada al abono: " + idAbono
                        )
                );

        BigDecimal valorAbonoReversar = valorSeguro(abono.getValorAbono());
        BigDecimal abonoActual = valorSeguro(factura.getAbono());
        BigDecimal totalFactura = valorSeguro(factura.getTotal());

        if (valorAbonoReversar.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException(
                    "El valor del abono a reversar debe ser mayor que cero"
            );
        }

        if (valorAbonoReversar.compareTo(abonoActual) > 0) {
            throw new IllegalStateException(
                    "El valor del abono a reversar supera el abono acumulado de la factura"
            );
        }

        BigDecimal nuevoAbono = abonoActual.subtract(valorAbonoReversar);
        BigDecimal nuevoSaldo = totalFactura.subtract(nuevoAbono);

        factura.setAbono(nuevoAbono);
        factura.setSaldo(nuevoSaldo);
        factura.setValAbonoIngresado(BigDecimal.ZERO);

        cabFacturaRepository.save(factura);
        abonoRepository.delete(abono);

        auditarService.registrarMovimiento(
                abono,
                "Reversar abono",
                "Factura: " + factura.getIdFactura()
                        + ", abono reversado: " + valorAbonoReversar
        );
    }

    private BigDecimal valorSeguro(BigDecimal valor) {
        return valor != null ? valor : BigDecimal.ZERO;
    }
}