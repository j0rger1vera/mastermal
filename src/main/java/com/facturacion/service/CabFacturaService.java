package com.facturacion.service;

import com.facturacion.dto.FacturacionGeneralDTO;
import com.facturacion.dto.HistorialAbonosDTO;
import com.facturacion.entity.Abono;
import com.facturacion.entity.CabFactura;
import com.facturacion.repository.AbonoRepository;
import com.facturacion.repository.CabFacturaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@AllArgsConstructor
public class CabFacturaService {

    private static final String FACTURA = "Factura";
    private static final String MODIFICAR_FACTURA_OPERACION = "Modificar factura";
    private static final String FACTURA_NO_ENCONTRADA = "Factura no encontrada";
    private final CabFacturaRepository cabFacturaRepository;
    private final AuditarService auditarService;
    private final AbonoService abonoService;

    public CabFactura guardarCabFactura(CabFactura cabFactura) {
        cabFactura.setFecha(LocalDateTime.now());
        cabFactura.setTotal(cabFactura.getTotal() != null ? cabFactura.getTotal() : BigDecimal.ZERO);
        cabFactura.setSaldo(cabFactura.getSaldo() != null ? cabFactura.getSaldo() : BigDecimal.ZERO);
        cabFactura.setAbono(cabFactura.getAbono() != null ? cabFactura.getAbono() : BigDecimal.ZERO);
        cabFactura.setDetalle(cabFactura.getDetalle() != null ? cabFactura.getDetalle() : "");
        cabFactura.setValAbonoAnterior(cabFactura.getValAbonoAnterior() != null ? cabFactura.getValAbonoAnterior() : BigDecimal.ZERO);
        cabFactura.setValAbonoIngresado(cabFactura.getValAbonoIngresado() != null ? cabFactura.getValAbonoIngresado() : BigDecimal.ZERO);
        CabFactura facturaGuardada = this.cabFacturaRepository.save(cabFactura);
        auditarService.registrarMovimiento(facturaGuardada, "Factura", "Crear factura");
        if (cabFactura.getAbono().compareTo(BigDecimal.ZERO) > 0){
            abonoService.abonarAFactura(cabFactura);
        }
        return facturaGuardada;
    }

    public void actualizarFactura(CabFactura cabFactura) {
        // Obtener la factura original de la base de datos
        Optional<CabFactura> facturaEnBaseDatosOpt = obtenerPorId(cabFactura.getIdFactura());

        if (facturaEnBaseDatosOpt.isPresent()) {
            CabFactura facturaOriginal = facturaEnBaseDatosOpt.get();

            // Guardar la factura actualizada
            this.cabFacturaRepository.save(cabFactura);
            auditarService.registrarMovimiento(cabFactura, FACTURA, MODIFICAR_FACTURA_OPERACION);

            // Comparar el abono original con el nuevo abono
            if (!facturaOriginal.getAbono().equals(cabFactura.getAbono())) {
                // Solo abonar si el valor ha cambiado
                abonoService.abonarAFactura(cabFactura);
            }
        } else {
            // Manejar el caso en que la factura no se encuentra
            throw new RuntimeException(FACTURA_NO_ENCONTRADA);
        }
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

    public List<FacturacionGeneralDTO> obtenerSaldosPorCliente( ) {
        List<FacturacionGeneralDTO> listaFacturacion = this.cabFacturaRepository.getSaldosPorCliente();

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
}
