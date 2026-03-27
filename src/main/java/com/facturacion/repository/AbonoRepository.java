package com.facturacion.repository;

import com.facturacion.dto.FacturacionGeneralDTO;
import com.facturacion.dto.HistorialAbonosDTO;
import com.facturacion.entity.Abono;
import com.facturacion.entity.Cliente;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AbonoRepository extends CrudRepository<Abono, Integer> {

    @Query(value = "" +
        "SELECT " +
            "a.id_abono AS idAbono, " +
            "f.num_factura AS numeroFactura, " +
            "a.valor_abono AS Abono, " +
            "a.fecha_abono AS fechaAbono, " +
            "a.val_anterior AS abonoAnterior, " +
            "a.total_factura_original AS totalFactura, " +
            "UPPER(c.nombre) AS nombreCliente FROM abonos a " +
        "INNER JOIN cab_factura f ON f.id_factura = a.id_factura " +
        "LEFT JOIN cliente c ON f.ruc_cliente = c.ruc_dni " +
        "ORDER BY f.num_factura DESC", nativeQuery = true)
    List<HistorialAbonosView> obtenerHistoricoAbonos();

    @Query(value = """
    SELECT 
        a.id_abono,
        f.num_factura,
        a.valor_abono,
        a.fecha_abono,
        a.val_anterior,
        a.total_factura_original,
        c.nombre
    FROM abonos a
    INNER JOIN cab_factura f ON f.id_factura = a.id_factura
    INNER JOIN cliente c ON f.ruc_cliente = c.ruc_dni
    WHERE f.num_factura = :numFactura AND f.id_factura = a.id_factura
    ORDER BY a.fecha_abono DESC
    """, nativeQuery = true)
    List<Object[]> obtenerHistorialPorFacturaRaw(Integer numFactura);

    default List<HistorialAbonosDTO> obtenerHistorialPorFactura(Integer numFactura) {
        return obtenerHistorialPorFacturaRaw(numFactura).stream().map(record -> {
            HistorialAbonosDTO dto = new HistorialAbonosDTO();
            dto.setIdAbono(((Number) record[0]).intValue());
            dto.setNumeroFactura(((Number) record[1]).intValue());
            dto.setAbono((BigDecimal) record[2]);
            Object fecha = record[3];
            if (fecha instanceof java.sql.Timestamp ts) {
                dto.setFechaAbono(ts.toLocalDateTime());
            } else {
                dto.setFechaAbono(LocalDateTime.parse(fecha.toString().replace(" ", "T")));
            }
            dto.setAbonoAnterior((BigDecimal) record[4]);
            dto.setTotalFactura((BigDecimal) record[5]);
            dto.setNombreCliente(record[6].toString());
            return dto;
        }).toList();
    }
}
