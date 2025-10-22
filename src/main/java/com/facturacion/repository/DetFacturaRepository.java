package com.facturacion.repository;

import com.facturacion.dto.DetFacturaDTO;
import com.facturacion.entity.DetFactura;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface DetFacturaRepository extends CrudRepository<DetFactura, Integer> {

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO det_factura (codigo_producto, cantidad, pk_cab_factura, valor_unitario, valor_total) VALUES (?1, ?2, ?3, ?4, ?5)", nativeQuery = true)
    void insertarFactura(
            Integer codigoProducto, Integer cantidad, Integer pkCabFactura, BigDecimal valUnitario, BigDecimal valTotal);

    @Query(value = "SELECT d.id, d.cantidad, d.codigo_producto, d.pk_cab_factura, d.valor_unitario, d.valor_total, p.nombre " +
            "FROM det_factura d " +
            "INNER JOIN producto p " +
            "ON p.codigo = CAST(d.codigo_producto AS varchar) " +
            "WHERE d.pk_cab_factura = :idFactura " +
            "ORDER BY d.id ASC", nativeQuery = true)
    List<Object[]> getProdsxIdFacturaQuery(Integer idFactura);

    default List<DetFacturaDTO> getProductosPorIdFactura(String idFactura) {
        List<Object[]> results = getProdsxIdFacturaQuery(Integer.parseInt(idFactura));
        return results.stream().map(record -> {
            DetFacturaDTO dto = new DetFacturaDTO();
            dto.setIdProducto((Integer) record[0]);
            dto.setCantidad((Integer) record[1]);
            dto.setCodigoProducto(record[2].toString());
            dto.setPkCabFactura((Integer) record[3]);
            dto.setValUnitarioProd((BigDecimal) record[4]);
            dto.setValTotalProd((BigDecimal) record[5]);
            dto.setNombreProducto((String) record[6]);
                return dto;
        }).toList();
    }
}