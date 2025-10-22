package com.facturacion.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "abonos")
public class Abono {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_abono")
    private Integer idAbono;

    @Column(name = "valor_abono", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorAbono;

    @Column(name = "fecha_abono", nullable = false)
    private LocalDateTime fechaAbono;

    @Column(name = "val_anterior", nullable = false, precision = 10, scale = 2)
    private BigDecimal valAnterior;

    @Column(name = "total_factura_original", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalFacturaOriginal;

    @Column(name = "id_factura", nullable = false)
    private Integer pkCabFactura;
}