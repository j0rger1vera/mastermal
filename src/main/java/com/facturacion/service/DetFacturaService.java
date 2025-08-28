package com.facturacion.service;

import com.facturacion.dto.DetFacturaDTO;
import com.facturacion.entity.CabFactura;
import com.facturacion.entity.DetFactura;
import com.facturacion.repository.CabFacturaRepository;
import com.facturacion.repository.DetFacturaRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("checkstyle:Indentation")
@Service
public class DetFacturaService {

    private static  final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(DetFacturaService.class);
    private final DetFacturaRepository detFacturaRepository;
    private final AuditarService auditarService;

    public DetFacturaService(DetFacturaRepository detFacturaRepository, AuditarService auditarService) {
        this.detFacturaRepository = detFacturaRepository;
        this.auditarService = auditarService;
    }

    @Transactional
    public void insertarProducto(List<DetFacturaDTO> detFacturaDTOs) {
        for (DetFacturaDTO detFacturaDTO : detFacturaDTOs) {
            this.detFacturaRepository.insertarFactura(  detFacturaDTO.getCodigoProducto(),
                                                        detFacturaDTO.getCantidad(),
                                                        detFacturaDTO.getPkCabFactura(),
                                                        detFacturaDTO.getValUnitarioProd(),
                                                        detFacturaDTO.getValTotalProd()
                                                      );
            auditarService.registrarMovimiento(detFacturaDTO, "Planchas", "Agregar plancha");
        }
    }

  @SuppressWarnings({"checkstyle:MissingJavadocMethod", "checkstyle:AbbreviationAsWordInName"})
  public void actualizarDetalles(DetFacturaDTO detFacturaDTOs) {
    try {
        CabFactura factura = new CabFactura();
        factura.setIdFactura(detFacturaDTOs.getPkCabFactura());
        DetFactura detalle = DetFactura.builder()
            .id(detFacturaDTOs.getIdProducto())
            .codigoProducto(detFacturaDTOs.getCodigoProducto())
            .cantidad(detFacturaDTOs.getCantidad())
            .pkCabFactura(factura)
            .valUnitarioProducto(detFacturaDTOs.getValUnitarioProd())
            .valTotalProducto(detFacturaDTOs.getValTotalProd())
            .build();
        this.detFacturaRepository.save(detalle);
        auditarService.registrarMovimiento(detFacturaDTOs, "Planchas", "Modificar plancha");
        System.out.println("Se actualizón plancha y se registró auditoria");
    } catch (Exception e) {
        throw new RuntimeException(e);
    }

  }

  public List<DetFacturaDTO> obtenerProductosPorFactura(String idFactura) {
    return this.detFacturaRepository.getProductosPorIdFactura(idFactura);
  }

}
