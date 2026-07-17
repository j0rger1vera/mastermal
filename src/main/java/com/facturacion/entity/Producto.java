package com.facturacion.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Integer idProducto;
    @Column(length = 15, unique = true)
    private String codigo;
    private String nombre;
    @Column(precision = 10, scale = 2)
    private BigDecimal precio;
    private Integer stock;
    @Column(columnDefinition = "SMALLINT")
    private Byte activo;
    private LocalDate fechaCreacion;


}
