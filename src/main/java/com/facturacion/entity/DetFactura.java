package com.facturacion.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DetFactura {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "codigo_producto")
    private Integer codigoProducto;

    @Column(name = "cantidad")
    private Integer cantidad;

    @Column(name = "valor_unitario", columnDefinition = "DECIMAL(10,2)")
    private BigDecimal valUnitarioProducto;

    @Column(name = "valor_total", columnDefinition = "DECIMAL(10,2)")
    private BigDecimal valTotalProducto;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "pk_cab_factura")
    private CabFactura pkCabFactura;
}
