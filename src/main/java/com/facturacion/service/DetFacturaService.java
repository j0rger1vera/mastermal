package com.facturacion.service;

import com.facturacion.dto.DetFacturaDTO;
import com.facturacion.entity.CabFactura;
import com.facturacion.entity.DetFactura;
import com.facturacion.repository.DetFacturaRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DetFacturaService {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(DetFacturaService.class);
    private final DetFacturaRepository detFacturaRepository;
    private final AuditarService auditarService;

    public DetFacturaService(DetFacturaRepository detFacturaRepository, AuditarService auditarService) {
        this.detFacturaRepository = detFacturaRepository;
        this.auditarService = auditarService;
    }

    @Transactional
    public void insertarProducto(List<DetFacturaDTO> detFacturaDTOs) {
        for (DetFacturaDTO detFacturaDTO : detFacturaDTOs) {
            this.detFacturaRepository.insertarFactura(
                    Integer.parseInt(detFacturaDTO.getCodigoProducto()),
                    detFacturaDTO.getCantidad(),
                    detFacturaDTO.getPkCabFactura(),
                    detFacturaDTO.getValUnitarioProd(),
                    detFacturaDTO.getValTotalProd()
            );
            auditarService.registrarMovimiento(detFacturaDTO, "Planchas", "Agregar plancha");
        }
    }

    public void actualizarDetalles(DetFacturaDTO detFacturaDTOs) {
        try {

            List<DetFacturaDTO> detallesExistentes = obtenerDetallesExistentes(detFacturaDTOs.getPkCabFactura());

            List<DetFacturaDTO> nuevosDetalles = obtenerNuevosDetalles(List.of(detFacturaDTOs), detallesExistentes);

            if (!nuevosDetalles.isEmpty()) {
                insertarProducto(nuevosDetalles);
            }else {
                for (DetFacturaDTO detalleExistente : detallesExistentes) {
                    if (!detFacturaDTOs.getIdProducto().equals(detalleExistente.getIdProducto())) {
                        eliminarPlaancha(detalleExistente);
                    }
                }

                CabFactura factura = new CabFactura();
                factura.setIdFactura(detFacturaDTOs.getPkCabFactura());
                DetFactura detalle = DetFactura.builder()
                        .id(detFacturaDTOs.getIdProducto())
                        .codigoProducto(Integer.parseInt(detFacturaDTOs.getCodigoProducto()))
                        .cantidad(detFacturaDTOs.getCantidad())
                        .pkCabFactura(factura)
                        .valUnitarioProducto(detFacturaDTOs.getValUnitarioProd())
                        .valTotalProducto(detFacturaDTOs.getValTotalProd())
                        .build();
                this.detFacturaRepository.save(detalle);
                auditarService.registrarMovimiento(detFacturaDTOs, "Planchas", "Modificar plancha");
                System.out.println("Se actualizó plancha y se registró auditoría");
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void eliminarPlaancha(DetFacturaDTO detFacturaDTOs) {
        try {
            CabFactura factura = new CabFactura();
            factura.setIdFactura(detFacturaDTOs.getPkCabFactura());
            DetFactura detalle = DetFactura.builder()
                    .id(detFacturaDTOs.getIdProducto())
                    .codigoProducto(Integer.parseInt(detFacturaDTOs.getCodigoProducto()))
                    .cantidad(detFacturaDTOs.getCantidad())
                    .pkCabFactura(factura)
                    .valUnitarioProducto(detFacturaDTOs.getValUnitarioProd())
                    .valTotalProducto(detFacturaDTOs.getValTotalProd())
                    .build();
            this.detFacturaRepository.delete(detalle);
            auditarService.registrarMovimiento(detFacturaDTOs, "Planchas", "Eliminar plancha");
            System.out.println("Se eliminó plancha y se registró auditoría");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<DetFacturaDTO> obtenerProductosPorFactura(String idFactura) {
        return this.detFacturaRepository.getProductosPorIdFactura(idFactura);
    }

    private List<DetFacturaDTO> obtenerNuevosDetalles(List<DetFacturaDTO> detallesFacturaDto, List<DetFacturaDTO> listPlanchasPorFacturaExistentes) {
        // Filtrar los detalles que no están en la lista de detalles existentes
        return detallesFacturaDto.stream()
                .filter(nuevoDetalle -> listPlanchasPorFacturaExistentes.stream()
                        .noneMatch(detalleExistente -> detalleExistente.getIdProducto().equals(nuevoDetalle.getIdProducto())))
                .toList();
    }

    private List<DetFacturaDTO> obtenerDetallesExistentes(Integer pkCabFactura) {
        return this.obtenerProductosPorFactura(pkCabFactura.toString());
    }
}