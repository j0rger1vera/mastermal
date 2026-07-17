package com.facturacion.repository;

import com.facturacion.entity.Cliente;
import com.facturacion.entity.Producto;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends CrudRepository<Producto, Integer> {

    @Query(value = "SELECT codigo FROM producto where codigo = :codigo",
            nativeQuery = true
    )
    String verificarSiExisteElCodigoProducto(
            @Param("codigo") String codigo
    );

    @Modifying
    @Transactional
    @Query(value = "UPDATE producto SET stock = stock - :cantidad WHERE codigo = :codigo", nativeQuery = true)
    int disminuirStock(
            @Param("codigo") String codigo,
            @Param("cantidad") Integer cantidad
    );




}
