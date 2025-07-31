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

    public DetFacturaService(DetFacturaRepository detFacturaRepository) {
        this.detFacturaRepository = detFacturaRepository;
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
        }
    }

  @SuppressWarnings({"checkstyle:MissingJavadocMethod", "checkstyle:AbbreviationAsWordInName"})
  public void actualizarDetalles(List<DetFacturaDTO> detFacturaDTOs) {
    for (DetFacturaDTO detFacturaDto : detFacturaDTOs) {
        DetFactura detalle = DetFactura.builder()
            .codigoProducto(detFacturaDto.getCodigoProducto())
            .cantidad(detFacturaDto.getCantidad())
            .valUnitarioProducto(detFacturaDto.getValUnitarioProd())
            .valTotalProducto(detFacturaDto.getValTotalProd())
            .build();
        this.detFacturaRepository.save(detalle);
    }
  }

  public List<DetFacturaDTO> obtenerProductosPorFactura(String idFactura) {
    return this.detFacturaRepository.getProductosPorIdFactura(idFactura);
  }

}
