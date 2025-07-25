package com.facturacion.repository;

import com.facturacion.dto.FacturacionGeneralDTO;
import com.facturacion.dto.DetFacturaDTO;
import com.facturacion.entity.CabFactura;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CabFacturaRepository extends CrudRepository<CabFactura, Integer> {

    @Query(value = "SELECT COALESCE(MAX(c.num_factura), 0) + 1 as num_factura FROM cab_factura c", nativeQuery = true)
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
            dto.setRucCliente((String) record[1]);
                dto.setNombreCliente((String) record[2].toString().toLowerCase());
            dto.setSaldo((BigDecimal) record[3]);
            dto.setAbono((BigDecimal) record[4]);
            dto.setDetalle((String) record[5]);
            dto.setTotal((BigDecimal) record[6]);
            dto.setNumeroFactura((Integer) record[7]);
            dto.setFecha((String) record[8]);
            return dto;
        }).toList();
    }


    @Query(value = "SELECT c.id_factura, c.ruc_cliente, cl.nombre, c.saldo, c.abono, c.nombre, c.total, c.num_factura, c.fecha, c.subtotal " +
        "FROM cab_factura c " +
        "INNER JOIN cliente cl ON c.ruc_cliente = cl.ruc_dni " +
        "WHERE c.ruc_cliente = :nitCliente " +
        "ORDER BY c.num_factura ASC", nativeQuery = true)
    List<Object[]> getFacturaPorUnClienteQuery(String nitCliente);

    default List<FacturacionGeneralDTO> getFacturaPorUnCliente(String nitCliente) {
        List<Object[]> results = getFacturaPorUnClienteQuery(nitCliente.toString());
        return results.stream().map(record -> {
            FacturacionGeneralDTO dto = new FacturacionGeneralDTO();
            dto.setIdFactura((Integer) record[0]);
            dto.setRucCliente((String) record[1]);
            dto.setNombreCliente((String) record[2].toString().toLowerCase());
            dto.setSaldo((BigDecimal) record[3]);
            dto.setAbono((BigDecimal) record[4]);
            dto.setDetalle((String) record[5]);
            dto.setTotal((BigDecimal) record[6]);
            dto.setNumeroFactura((Integer) record[7]);
            dto.setFecha((String) record[8]);
            dto.setSubtotal((BigDecimal) record[9]);
            return dto;
        }).toList();
    }

    @Query(value = "SELECT c.id_factura, c.ruc_cliente, cl.nombre, c.saldo, c.abono, c.nombre, c.total, c.num_factura, c.fecha " +
        "FROM cab_factura c " +
        "INNER JOIN cliente cl ON c.ruc_cliente = cl.ruc_dni " +
        "WHERE c.saldo = 0 " +
        "ORDER BY c.num_factura DESC", nativeQuery = true)
    List<Object[]> getSaldos();

    default List<FacturacionGeneralDTO> getFacturasConSaldos() {
        List<Object[]> results = getSaldos();
        return results.stream().map(record -> {
            FacturacionGeneralDTO dto = new FacturacionGeneralDTO();
            dto.setIdFactura((Integer) record[0]);
            dto.setRucCliente((String) record[1]);
            dto.setNombreCliente((String) record[2].toString().toLowerCase());
            dto.setSaldo((BigDecimal) record[3]);
            dto.setAbono((BigDecimal) record[4]);
            dto.setDetalle((String) record[5]);
            dto.setTotal((BigDecimal) record[6]);
            dto.setNumeroFactura((Integer) record[7]);
            dto.setFecha((String) record[8]);
            return dto;
        }).toList();
    }

    @Query(value = "SELECT c.id_factura, c.ruc_cliente, cl.nombre, c.saldo, c.abono, c.nombre, c.total, c.num_factura, c.fecha " +
        "FROM cab_factura c " +
        "INNER JOIN cliente cl ON c.ruc_cliente = cl.ruc_dni " +
        "WHERE c.saldo > 0 " +
        "ORDER BY c.num_factura DESC", nativeQuery = true)
    List<Object[]> getSaldosClientes();

    default List<FacturacionGeneralDTO> getFacturasSaldosPorClientes() {
        List<Object[]> results = getSaldosClientes();
        return results.stream().map(record -> {
            FacturacionGeneralDTO dto = new FacturacionGeneralDTO();
            dto.setIdFactura((Integer) record[0]);
            dto.setRucCliente((String) record[1]);
            dto.setNombreCliente((String) record[2].toString().toLowerCase());
            dto.setSaldo((BigDecimal) record[3]);
            dto.setAbono((BigDecimal) record[4]);
            dto.setDetalle((String) record[5]);
            dto.setTotal((BigDecimal) record[6]);
            dto.setNumeroFactura((Integer) record[7]);
            dto.setFecha((String) record[8]);
            return dto;
        }).toList();
    }

    @Query(value = "SELECT d.id, d.cantidad, d.codigo_producto, d.pk_cab_factura, d.valor_unitario, d.valor_total, p.nombre " +
        "FROM det_factura d " +
        "INNER JOIN producto p ON p.id_producto = d.codigo_producto " +
        "WHERE d.pk_cab_factura = :idFactura " +
        "ORDER BY d.id ASC", nativeQuery = true)
    List<Object[]> getProdsxIdFacturaQuery(Integer idFactura);

    default List<DetFacturaDTO> getProductosPorIdFactura(String idFactura) {
        List<Object[]> results = getProdsxIdFacturaQuery(Integer.parseInt(idFactura));
        return results.stream().map(record -> {
            DetFacturaDTO dto = new DetFacturaDTO();
            dto.setIdProducto((Integer) record[0]);
            dto.setCantidad((Integer) record[1]);
            dto.setCodigoProducto((Integer) record[2]);
            dto.setPkCabFactura((Integer) record[3]);
            dto.setValUnitarioProd((String) record[4]);
            dto.setValTotalProd((String) record[5]);
            dto.setNombreProducto((String) record[6]);
            return dto;
        }).toList();
    }

    @Query(value = "SELECT c.id_factura, c.ruc_cliente, cl.nombre, c.saldo, c.abono, c.nombre, c.total, c.num_factura, c.fecha " +
        "FROM cab_factura c " +
        "INNER JOIN cliente cl ON c.ruc_cliente = cl.ruc_dni " +
        "AND c.saldo > 0 " +
        "ORDER BY c.num_factura DESC", nativeQuery = true)
    List<Object[]> getSaldosPorCobrarQuery();

    default List<FacturacionGeneralDTO> getSaldosPorCobrar() {
        List<Object[]> results = getSaldosPorCobrarQuery();
        return results.stream().map(record -> {
            FacturacionGeneralDTO dto = new FacturacionGeneralDTO();
            dto.setIdFactura((Integer) record[0]);
            dto.setRucCliente((String) record[1]);
            dto.setNombreCliente((String) record[2].toString().toLowerCase());
            dto.setSaldo((BigDecimal) record[3]);
            dto.setAbono((BigDecimal) record[4]);
            dto.setDetalle((String) record[5]);
            dto.setTotal((BigDecimal) record[6]);
            dto.setNumeroFactura((Integer) record[7]);
            dto.setFecha((String) record[8]);
            return dto;
        }).toList();
    }
}
