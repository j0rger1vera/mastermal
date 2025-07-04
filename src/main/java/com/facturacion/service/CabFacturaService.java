package com.facturacion.service;

import com.facturacion.dto.FacturacionGeneralDTO;
import com.facturacion.entity.CabFactura;
import com.facturacion.repository.CabFacturaRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CabFacturaService {

    private final CabFacturaRepository cabFacturaRepository;

    public CabFacturaService(CabFacturaRepository cabFacturaRepository) {
        this.cabFacturaRepository = cabFacturaRepository;
    }

    public CabFactura guardarCabFactura(CabFactura cabFactura) {
        return this.cabFacturaRepository.save(cabFactura);
    }

    public void actualizarFactura(CabFactura cabFactura) {
        this.cabFacturaRepository.save(cabFactura);
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
                existente.setSaldo(existente.getSaldo().add(dto.getSaldo()));
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

}
