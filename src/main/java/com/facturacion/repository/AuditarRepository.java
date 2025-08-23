package com.facturacion.repository;

import com.facturacion.entity.Auditoria;
import com.facturacion.entity.Cliente;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditarRepository extends CrudRepository<Auditoria, Integer> {


    @Query(value = "SELECT * FROM auditoria a ORDER BY a.fecha ASC", nativeQuery = true)
    List<Auditoria> getMovimientos();
}
