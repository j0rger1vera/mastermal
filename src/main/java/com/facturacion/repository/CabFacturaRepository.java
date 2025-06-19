
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

    @Query(value = "SELECT c.ruc_cliente, cl.nombre, c.saldo, c.abono, c.total, c.num_factura, c.fecha " +
            "FROM cab_factura c " +
            "INNER JOIN cliente cl ON c.ruc_cliente = cl.ruc_dni", nativeQuery = true)
    List<Object[]> getBalanceGeneralRaw();

    default List<FacturacionGeneralDTO> getBalanceGeneral() {
        List<Object[]> results = getBalanceGeneralRaw();
        return results.stream().map(record -> {
            FacturacionGeneralDTO dto = new FacturacionGeneralDTO();
            dto.setNitCliente((String) record[0]);
            dto.setNombreCliente((String) record[1]);
            dto.setSaldo((BigDecimal) record[2]);
            dto.setAbono((BigDecimal) record[3]);
            dto.setTotal((BigDecimal) record[4]);
            dto.setNumeroFactura((Integer) record[5]);
            dto.setFechaFacturada((String) record[6]);
            return dto;
        }).toList();
    }
}
