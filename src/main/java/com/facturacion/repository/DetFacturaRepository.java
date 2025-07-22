package com.facturacion.repository;

import com.facturacion.dto.DetFacturaDTO;
import com.facturacion.entity.CabFactura;
import com.facturacion.entity.DetFactura;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetFacturaRepository extends CrudRepository<DetFactura, Integer> {


    @Modifying
    @Transactional
    @Query(value = "INSERT INTO det_factura (codigo_producto, cantidad, pk_cab_factura, valor_unitario, valor_total) VALUES (?1, ?2, ?3, ?4, ?5)", nativeQuery = true)
    void insertarFactura(
        Integer codigoProducto, Integer cantidad, Integer pkCabFactura, String valUnitario, String valTotal);


}
