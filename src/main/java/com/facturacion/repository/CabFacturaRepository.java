package com.facturacion.repository;

import com.facturacion.dto.FacturacionGeneralDTO;
import com.facturacion.dto.DetFacturaDTO;
import com.facturacion.entity.CabFactura;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.List;
import java.util.Objects;

@Repository
public interface CabFacturaRepository extends CrudRepository<CabFactura, Integer> {

    @Query(value = "SELECT COALESCE(MAX(c.num_factura), 0) + 1 as num_factura FROM cab_factura c", nativeQuery = true)
    public Integer generaFactura();

    @Query(value = "SELECT c.id_factura, c.num_factura, cl.id_cliente, cl.nombre, c.nombre AS detalle, c.total, c.abono, c.saldo, c.fecha_creacion " +
            "FROM cab_factura c " +
            "INNER JOIN cliente cl ON CAST(c.ruc_cliente AS INTEGER) = cl.id_cliente " +
            "UNION ALL " +
            "SELECT a.pk_cab_factura, f.num_factura, c.id_cliente, c.nombre, null AS detalle, a.total_factura_original, a.valor_abono , a.total_factura_original -a.valor_abono as saldo, " +
            "a.fecha_abono AS fecha_abono " +
            "FROM abonos a " +
            "INNER JOIN cab_factura f ON f.id_factura = a.id_factura " +
            "INNER JOIN " +
            "cliente c ON c.id_cliente = CAST(f.ruc_cliente AS INTEGER) " +
            "ORDER BY num_factura DESC NULLS last, 2 DESC NULLS last;", nativeQuery = true)
    List<Object[]> getBalanceGeneralRaw();

    default List<FacturacionGeneralDTO> getBalanceGeneral() {
        List<Object[]> results = getBalanceGeneralRaw();
        return results.stream().map(record -> {
            FacturacionGeneralDTO dto = new FacturacionGeneralDTO();
            dto.setIdFactura(Objects.isNull(record[0]) ? "abono $$" : record[0].toString());
            dto.setNumeroFactura(Objects.isNull(record[1]) ? 0 : (Integer) record[1]);
            dto.setRucCliente(Objects.isNull(record[2]) ? "" : record[2].toString());
            dto.setNombreCliente(Objects.isNull(record[3]) ? "" : record[3].toString().toLowerCase());
            dto.setDetalle(Objects.isNull(record[4]) ? "" : record[4].toString().toLowerCase());
            dto.setTotal(Objects.isNull(record[5]) ? BigDecimal.ZERO : convertToBigDecimal(record[5]));
            dto.setAbono(Objects.isNull(record[6]) ? BigDecimal.ZERO : convertToBigDecimal(record[6]));
            dto.setSaldo(Objects.isNull(record[7]) ? BigDecimal.ZERO : convertToBigDecimal(record[7]));
            dto.setFecha(Objects.isNull(record[8]) ? "2025-09-27 08:07:01" : record[8].toString());
            return dto;
        }).toList();
    }


    @Query(value = "SELECT c.id_factura, c.ruc_cliente, cl.nombre, c.saldo, c.abono, c.nombre, c.total, c.num_factura, c.fecha_creacion, c.subtotal " +
        "FROM cab_factura c " +
        "INNER JOIN cliente cl ON CAST(c.ruc_cliente AS INTEGER) = cl.id_cliente " +
        "WHERE c.ruc_cliente = :nitCliente " +
        "ORDER BY c.num_factura ASC", nativeQuery = true)
    List<Object[]> getFacturaPorUnClienteQuery(String nitCliente);

    default List<FacturacionGeneralDTO> getFacturaPorUnCliente(String nitCliente) {
        List<Object[]> results = getFacturaPorUnClienteQuery(nitCliente.toString());
        return results.stream().map(record -> {
            FacturacionGeneralDTO dto = new FacturacionGeneralDTO();
            dto.setIdFactura(record[0].toString());
            dto.setRucCliente((String) record[1]);
            dto.setNombreCliente(record[2].toString().toLowerCase());
            dto.setSaldo((BigDecimal) record[3]);
            dto.setAbono((BigDecimal) record[4]);
            dto.setDetalle((String) record[5]);
            dto.setTotal((BigDecimal) record[6]);
            dto.setNumeroFactura((Integer) record[7]);
            dto.setFecha(record[8].toString());
            dto.setSubtotal((BigDecimal) record[9]);
            return dto;
        }).toList();
    }

    @Query(value = "SELECT c.id_factura, c.ruc_cliente, cl.nombre, c.saldo, c.abono, c.nombre, c.total, c.num_factura, c.fecha_creacion " +
        "FROM cab_factura c " +
        "INNER JOIN cliente cl ON CAST(c.ruc_cliente AS INTEGER) = cl.id_cliente " +
        "WHERE c.saldo > 0 " +
        "ORDER BY c.num_factura DESC", nativeQuery = true)
    List<Object[]> getSaldosClientes();

    default List<FacturacionGeneralDTO> getFacturasSaldosPorClientes() {
        List<Object[]> results = getSaldosClientes();
        return results.stream().map(record -> {
            FacturacionGeneralDTO dto = new FacturacionGeneralDTO();
            dto.setIdFactura(Objects.isNull(record[0]) ? "0" : record[0].toString());
            dto.setRucCliente((String) record[1]);
            dto.setNombreCliente((String) record[2].toString().toLowerCase());
            dto.setSaldo((BigDecimal) record[3]);
            dto.setAbono((BigDecimal) record[4]);
            dto.setDetalle((String) record[5]);
            dto.setTotal((BigDecimal) record[6]);
            dto.setNumeroFactura((Integer) record[7]);
            dto.setFecha(Objects.isNull(record[8]) ? "2025-09-27 00:00:00" : record[8].toString());
            return dto;
        }).toList();
    }

    @Query(value = "SELECT cl.nombre, SUM(c.saldo), SUM(c.abono), SUM(c.total) " +
        "FROM cliente cl " +
        "INNER JOIN cab_factura c ON CAST(c.ruc_cliente AS INTEGER) = cl.id_cliente " +
        "WHERE c.saldo > 0 " +
        "GROUP BY cl.nombre " +
        "ORDER BY cl.nombre asc", nativeQuery = true)
    List<Object[]> getSaldosPorCobrarQuery();

    default List<FacturacionGeneralDTO> getSaldosPorCobrar() {
        List<Object[]> results = getSaldosPorCobrarQuery();
        return results.stream().map(record -> {
            FacturacionGeneralDTO dto = new FacturacionGeneralDTO();
            dto.setNombreCliente((String) record[0]);
            dto.setSaldo((BigDecimal) record[1]);
            dto.setAbono((BigDecimal) record[2]);
            dto.setTotal((BigDecimal) record[3]);
            return dto;
        }).toList();
    }

    private BigDecimal convertToBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO; // O el valor que desees usar como predeterminado
        }
        try {
            // Si el valor es un String, convertirlo a BigDecimal
            if (value instanceof String) {
                return new BigDecimal((String) value);
            }
            // Si el valor es un número, convertirlo directamente
            return (BigDecimal) value;
        } catch (NumberFormatException e) {
            // Manejar el caso donde la conversión falla
            System.err.println("Error al convertir a BigDecimal: " + value);
            return BigDecimal.ZERO; // O el valor que desees usar como predeterminado
        }
    }


    @Query(value = "SELECT c.id_factura, c.ruc_cliente, cl.nombre, c.saldo, c.abono, c.nombre, c.total, c.num_factura, c.fecha_creacion " +
            "FROM cab_factura c " +
            "INNER JOIN cliente cl ON CAST(c.ruc_cliente AS INTEGER) = cl.id_cliente ORDER BY c.num_factura DESC", nativeQuery = true)
    List<Object[]> getSaldosPorClienteQuery();

    default List<FacturacionGeneralDTO> getSaldosPorCliente() {
        List<Object[]> results = getSaldosPorClienteQuery();
        return results.stream().map(record -> {
            FacturacionGeneralDTO dto = new FacturacionGeneralDTO();
            dto.setIdFactura(Objects.isNull(record[0]) ? " " : record[0].toString());
            dto.setRucCliente(record[1].toString());
            dto.setNombreCliente(record[2].toString().toLowerCase());
            dto.setSaldo(Objects.isNull(record[3]) ? BigDecimal.ZERO : convertToBigDecimal(record[3]));
            dto.setAbono(Objects.isNull(record[4]) ? BigDecimal.ZERO : convertToBigDecimal(record[4]));
            dto.setDetalle(Objects.isNull(record[5]) ? "" : record[5].toString());
            dto.setTotal(Objects.isNull(record[6]) ? BigDecimal.ZERO : convertToBigDecimal(record[6]));
            dto.setNumeroFactura(Objects.isNull(record[7]) ? 0 : (Integer) record[7]);
            dto.setFecha(Objects.isNull(record[8]) ? "2025-09-27 00:00:01" : record[8].toString());
            return dto;
        }).toList();
    }
}
