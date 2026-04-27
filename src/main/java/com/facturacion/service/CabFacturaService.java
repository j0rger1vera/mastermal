package com.facturacion.service;

import com.facturacion.dto.FacturacionGeneralDTO;
import com.facturacion.dto.DetFacturaDTO;
import com.facturacion.dto.HistorialAbonosDTO;
import com.facturacion.entity.Abono;
import com.facturacion.entity.CabFactura;
import com.facturacion.entity.Cliente;
import com.facturacion.entity.DetFactura;
import com.facturacion.repository.AbonoRepository;
import com.facturacion.repository.CabFacturaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@AllArgsConstructor
public class CabFacturaService {

    private final CabFacturaRepository cabFacturaRepository;
    private final AbonoRepository abonoRepository;
    private final AuditarService auditarService;

    public CabFactura guardarCabFactura(CabFactura cabFactura) {
        cabFactura.setFecha(LocalDateTime.now().toString().replace('T', ' ').substring(0, 19));

        BigDecimal total = toBigDecimal(cabFactura.getTotal());
        BigDecimal abono = toBigDecimal(cabFactura.getAbono());

        validarAbono(total, abono);

        BigDecimal saldo = total.subtract(abono);

        cabFactura.setTotal(toMoneyString(total));
        cabFactura.setAbono(toMoneyString(abono));
        cabFactura.setSaldo(toMoneyString(saldo));
        cabFactura.setDetalle(StringUtils.isEmpty(cabFactura.getDetalle()) ? "" : cabFactura.getDetalle());
        cabFactura.setValAbonoAnterior(toMoneyString(toBigDecimal(cabFactura.getValAbonoAnterior())));
        cabFactura.setValAbonoIngresado(toMoneyString(toBigDecimal(cabFactura.getValAbonoIngresado())));

        CabFactura facturaGuardada = this.cabFacturaRepository.save(cabFactura);
        auditarService.registrarMovimiento(facturaGuardada, "Factura", "Crear factura");

        return facturaGuardada;
    }

    public void actualizarFactura(CabFactura cabFactura) {
        CabFactura facturaActual =
                cabFacturaRepository.findById(cabFactura.getIdFactura())
                        .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada"));

        BigDecimal total = toBigDecimal(
                StringUtils.isEmpty(cabFactura.getTotal())
                        ? facturaActual.getTotal()
                        : cabFactura.getTotal()
        );

        BigDecimal abono = toBigDecimal(
                StringUtils.isEmpty(cabFactura.getAbono())
                        ? facturaActual.getAbono()
                        : cabFactura.getAbono()
        );

        validarAbono(total, abono);

        BigDecimal saldo = total.subtract(abono);

        facturaActual.setTotal(toMoneyString(total));
        facturaActual.setAbono(toMoneyString(abono));
        facturaActual.setSaldo(toMoneyString(saldo));

        facturaActual.setDetalle(
                StringUtils.isEmpty(cabFactura.getDetalle())
                        ? facturaActual.getDetalle()
                        : cabFactura.getDetalle()
        );

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

    public void abonarAFactura(CabFactura cabFactura) {
        BigDecimal valAbonoIngresado = toBigDecimal(cabFactura.getValAbonoIngresado());

        if (valAbonoIngresado.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        CabFactura facturaActual =
                cabFacturaRepository.findById(cabFactura.getIdFactura())
                        .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada"));

        BigDecimal total = toBigDecimal(facturaActual.getTotal());
        BigDecimal abonoActual = toBigDecimal(facturaActual.getAbono());

        BigDecimal nuevoAbono = abonoActual.add(valAbonoIngresado);

        validarAbono(total, nuevoAbono);

        BigDecimal nuevoSaldo = total.subtract(nuevoAbono);

        facturaActual.setValAbonoAnterior(toMoneyString(abonoActual));
        facturaActual.setValAbonoIngresado(toMoneyString(valAbonoIngresado));
        facturaActual.setAbono(toMoneyString(nuevoAbono));
        facturaActual.setSaldo(toMoneyString(nuevoSaldo));

        Abono logAbono = traducirFacturaToAbono(facturaActual);

        CabFactura facturaGuardada = this.cabFacturaRepository.save(facturaActual);
        registrarAbono(logAbono);

        auditarService.registrarMovimiento(facturaGuardada, "Factura", "Abonar factura");
    }

    public Abono registrarAbono(Abono abono) {
        Abono abonoCreado = this.abonoRepository.save(abono);
        auditarService.registrarMovimiento(abonoCreado, "Abonos", "Agregar abono");
        return abonoCreado;
    }

    public List<HistorialAbonosDTO> obtenerHistorialAbonos( ) {
        return this.abonoRepository.getAbonos();
    }

    private Abono traducirFacturaToAbono(CabFactura cabFactura){
        LocalDateTime ahora = LocalDateTime.now();

        // 2. Formatear la fecha y hora
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String fechaFormateada = ahora.format(formato);

        Abono abono = Abono.builder()
            .valorAbono(cabFactura.getValAbonoIngresado())
            .valAnterior(cabFactura.getValAbonoAnterior())
            .totalFacturaOriginal(cabFactura.getTotal())
            .pkCabFactura(cabFactura.getIdFactura())
            .fechaAbono(fechaFormateada)
            .build();

        return abono;
    }

    private BigDecimal toBigDecimal(String value) {
        if (value == null || value.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }

        return new BigDecimal(value.trim());
    }

    private String toMoneyString(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
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

    /*logica temporal borrar cuando este estable la app*/

    public List<FacturacionGeneralDTO> obtenerFacturasSabado() {
        return cabFacturaRepository.getBalanceGeneralSabado();
    }

}
