package com.facturacion.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "det_factura")
public class DetFactura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @NotNull(message = "El código del producto es obligatorio")
    @Column(name = "codigo_producto", nullable = false)
    private Integer codigoProducto;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a cero")
    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @NotNull(message = "El valor unitario es obligatorio")
    @Positive(message = "El valor unitario debe ser mayor a cero")
    @Column(name = "valor_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal valUnitarioProducto;

    @NotNull(message = "El valor total del producto es obligatorio")
    @Positive(message = "El valor total debe ser mayor a cero")
    @Column(name = "valor_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal valTotalProducto;

    @NotNull(message = "La factura asociada es obligatoria")
    @JsonBackReference
    @ManyToOne(optional = false)
    @JoinColumn(name = "pk_cab_factura", nullable = false)
    private CabFactura pkCabFactura;
}