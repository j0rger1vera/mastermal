package com.facturacion.service;

import com.facturacion.dto.FacturacionGeneralDTO;
import com.facturacion.entity.Abono;
import com.facturacion.entity.CabFactura;
import com.facturacion.repository.CabFacturaRepository;
import com.facturacion.util.TipoDataConverter;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@AllArgsConstructor
public class CabFacturaService {

    private final CabFacturaRepository cabFacturaRepository;
    private final AuditarService auditarService;
    private final TipoDataConverter tipoDataConverter;
    private final AbonoService abonoService;

    public CabFactura guardarCabFactura(CabFactura cabFactura) {

        LocalDateTime ahora = LocalDateTime.now();

        cabFactura.setFecha(LocalDateTime.now().toString().replace('T', ' ').substring(0, 19));
        cabFactura.setFechaCreacion(ahora);

        BigDecimal total = tipoDataConverter.toBigDecimal(cabFactura.getTotal());
        BigDecimal abono = tipoDataConverter.toBigDecimal(cabFactura.getAbono());

        validarAbono(total, abono);

        BigDecimal saldo = total.subtract(abono);

        cabFactura.setTotal(tipoDataConverter.toMoneyString(total));
        cabFactura.setAbono(tipoDataConverter.toMoneyString(abono));
        cabFactura.setSaldo(tipoDataConverter.toMoneyString(saldo));
        cabFactura.setDetalle(StringUtils.isEmpty(cabFactura.getDetalle()) ? "" : cabFactura.getDetalle());
        cabFactura.setValAbonoAnterior(tipoDataConverter.toMoneyString(tipoDataConverter.toBigDecimal(cabFactura.getValAbonoAnterior())));
        cabFactura.setValAbonoIngresado(tipoDataConverter.toMoneyString(tipoDataConverter.toBigDecimal(cabFactura.getValAbonoIngresado())));

        CabFactura facturaGuardada = this.cabFacturaRepository.save(cabFactura);
        auditarService.registrarMovimiento(facturaGuardada, "Factura", "Crear factura");

        return facturaGuardada;
    }

    public void actualizarFacturaConAbonoOpcional(CabFactura cabFactura) {
        BigDecimal valAbonoIngresado = tipoDataConverter.toBigDecimal(cabFactura.getValAbonoIngresado());

        //1. Cargar factura actual desde BD
        CabFactura facturaActual =
                cabFacturaRepository.findById(cabFactura.getIdFactura())
                        .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada"));

        BigDecimal abonoActual = tipoDataConverter.toBigDecimal(facturaActual.getAbono());
        BigDecimal abonoManual = tipoDataConverter.toBigDecimal(cabFactura.getAbono());

        boolean tieneNuevoAbono = valAbonoIngresado.compareTo(BigDecimal.ZERO) > 0;
        boolean ajustaAbonoManual = abonoManual.compareTo(abonoActual) != 0;

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
        BigDecimal total = tipoDataConverter.toBigDecimal(facturaActual.getTotal());

        if (tieneNuevoAbono) {
            BigDecimal nuevoAbono = abonoActual.add(valAbonoIngresado);
            validarAbono(total, nuevoAbono);

            facturaActual.setValAbonoAnterior(tipoDataConverter.toMoneyString(abonoActual));
            facturaActual.setValAbonoIngresado(tipoDataConverter.toMoneyString(valAbonoIngresado));
            facturaActual.setAbono(tipoDataConverter.toMoneyString(nuevoAbono));
            facturaActual.setSaldo(tipoDataConverter.toMoneyString(total.subtract(nuevoAbono)));

            Abono logAbono = tipoDataConverter.traducirFacturaToAbono(facturaActual);

            CabFactura facturaGuardada = cabFacturaRepository.save(facturaActual);
            abonoService.registrarAbono(logAbono);
            auditarService.registrarMovimiento(facturaGuardada, "Factura", "Abonar factura");

            return;
        }

        if (ajustaAbonoManual) {
            validarAbono(total, abonoManual);

            facturaActual.setValAbonoAnterior(tipoDataConverter.toMoneyString(abonoActual));
            facturaActual.setValAbonoIngresado("0.00");
            facturaActual.setAbono(tipoDataConverter.toMoneyString(abonoManual));
            facturaActual.setSaldo(tipoDataConverter.toMoneyString(total.subtract(abonoManual)));

            CabFactura facturaGuardada = cabFacturaRepository.save(facturaActual);
            auditarService.registrarMovimiento(facturaGuardada, "Factura", "Ajustar abono acumulado");

            return;
        }

        validarAbono(total, abonoActual);

        facturaActual.setValAbonoIngresado("0.00");
        facturaActual.setSaldo(tipoDataConverter.toMoneyString(total.subtract(abonoActual)));

        CabFactura facturaGuardada = cabFacturaRepository.save(facturaActual);
        auditarService.registrarMovimiento(facturaGuardada, "Factura", "Modificar factura");
    }


    public void actualizarFactura(CabFactura cabFactura) {
        CabFactura facturaActual =
                cabFacturaRepository.findById(cabFactura.getIdFactura())
                        .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada"));

        BigDecimal total = tipoDataConverter.toBigDecimal(
                StringUtils.isEmpty(cabFactura.getTotal())
                        ? facturaActual.getTotal()
                        : cabFactura.getTotal()
        );

        BigDecimal abono = tipoDataConverter.toBigDecimal(
                StringUtils.isEmpty(cabFactura.getAbono())
                        ? facturaActual.getAbono()
                        : cabFactura.getAbono()
        );

        validarAbono(total, abono);

        BigDecimal saldo = total.subtract(abono);

        facturaActual.setTotal(tipoDataConverter.toMoneyString(total));
        facturaActual.setAbono(tipoDataConverter.toMoneyString(abono));
        facturaActual.setSaldo(tipoDataConverter.toMoneyString(saldo));

        facturaActual.setDetalle(
                StringUtils.isEmpty(cabFactura.getDetalle())
                        ? facturaActual.getDetalle()
                        : cabFactura.getDetalle()
        );

        facturaActual.setRucCliente(cabFactura.getRucCliente());

        CabFactura facturaGuardada = this.cabFacturaRepository.save(facturaActual);
        auditarService.registrarMovimiento(facturaGuardada, "Factura", "Modificar factura");
    }

    public List<CabFactura> obtenerTodas( ) {
        Iterable<CabFactura> cabFacturas = this.cabFacturaRepository.findAll();
        List<CabFactura> listaCabFacturas = new ArrayList<>();
        cabFacturas.forEach(listaCabFacturas::add);
        return listaCabFacturas;
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
        List<FacturacionGeneralDTO> listaFacturacion = this.cabFacturaRepository.getBalanceGeneral();

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
        List<FacturacionGeneralDTO> listaFacturacion = this.cabFacturaRepository.getSaldosPorCobrar();
        return listaFacturacion;
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

}