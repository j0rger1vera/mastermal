package com.facturacion.repository;

import com.facturacion.entity.Cliente;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClienteRepository extends CrudRepository<Cliente, Integer> {

    @Query(value = "SELECT count(ruc_dni) as ruc_dni FROM facturacion.cliente where ruc_dni = :ruc_dni", nativeQuery = true)
    public String verificarSiExiteCliente(@Param("ruc_dni") String ruc_dni);

    @Query(value = "SELECT COALESCE(MAX(id_cliente), 0) + 1 as id_cliente FROM facturacion.cliente", nativeQuery = true)
    public Integer generaNit();

    @Query(value = "SELECT * FROM cliente c ORDER BY c.nombre ASC", nativeQuery = true)
    List<Cliente> getClientes();
}
