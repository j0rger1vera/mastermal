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
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface AbonoRepository extends CrudRepository<Abono, Integer> {

    @Query(value = "SELECT a.id_abono, f.num_factura, a.valor_abono, a.fecha_abono, a.val_anterior, a.total_factura_original, c.nombre " +
        "FROM abonos a " +
        "INNER JOIN cab_factura f ON f.id_factura = a.id_factura " +
        "INNER JOIN cliente c ON c.id_cliente = f.ruc_cliente " +
        "ORDER BY f.num_factura DESC", nativeQuery = true)
    List<Object[]> getHistoricoAbonos();

    default List<HistorialAbonosDTO> getAbonos() {
        List<Object[]> results = getHistoricoAbonos();
        return results.stream().map(record -> {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            HistorialAbonosDTO dto = new HistorialAbonosDTO();
            dto.setIdAbono((Integer) record[0]);
            dto.setNumeroFactura((Integer) record[1]);
            dto.setAbono((BigDecimal) record[2]);
            dto.setFechaAbono(formatter.format(record[3]));
            dto.setAbonoAnterior((BigDecimal) record[4]);
            dto.setTotalFactura((BigDecimal) record[5]);
            dto.setNombreCliente(record[6].toString().toUpperCase());
            return dto;
        }).toList();
    }
}
