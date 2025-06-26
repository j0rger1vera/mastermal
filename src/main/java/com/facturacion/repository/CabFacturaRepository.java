package com.facturacion.repository;

import com.facturacion.dto.DetFacturaDTO;
import com.facturacion.dto.FacturacionGeneralDTO;
import com.facturacion.entity.CabFactura;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CabFacturaRepository extends CrudRepository<CabFactura, Integer> {

    @Query(value = "SELECT COALESCE(MAX(num_factura), 0) + 1 as num_factura FROM facturacion.cab_factura", nativeQuery = true)
    public Integer generaFactura();

    @Query(value = "SELECT c.id_factura, c.ruc_cliente, cl.nombre, c.saldo, c.abono, c.nombre, c.total, c.num_factura, c.fecha " +
            "FROM cab_factura c " +
            "INNER JOIN cliente cl ON c.ruc_cliente = cl.ruc_dni ORDER BY c.num_factura DESC", nativeQuery = true)
    List<Object[]> getBalanceGeneralRaw();

    default List<FacturacionGeneralDTO> getBalanceGeneral() {
        List<Object[]> results = getBalanceGeneralRaw();
        return results.stream().map(record -> {
            FacturacionGeneralDTO dto = new FacturacionGeneralDTO();
            dto.setIdFactura((Integer) record[0]);
            dto.setNitCliente((String) record[1]);
                dto.setNombreCliente((String) record[2].toString().toLowerCase());
            dto.setSaldo((BigDecimal) record[3]);
            dto.setAbono((BigDecimal) record[4]);
            dto.setDetalle((String) record[5]);
            dto.setTotal((BigDecimal) record[6]);
            dto.setNumeroFactura((Integer) record[7]);
            dto.setFechaFacturada((String) record[8]);
            return dto;
        }).toList();
    }
}
