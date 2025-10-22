package com.facturacion.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "cab_factura")
public class CabFactura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_factura")
    private Integer idFactura;

    @Column(name = "num_factura", unique = true)
    private Integer numeroFactura;

    @Column(name = "fecha_creacion")
    private LocalDateTime fecha;

    @Column(name = "ruc_cliente")
    private String rucCliente;

    @Column(name = "nombre")
    private String detalle;

    @Column(name = "subtotal", precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "igv", precision = 10, scale = 2)
    private BigDecimal igv;

    @Column(name = "total", precision = 10, scale = 2)
    private BigDecimal total;

    @Column(name = "abono", precision = 10, scale = 2)
    private BigDecimal abono;

    @Column(name = "saldo", precision = 10, scale = 2)
    private BigDecimal saldo;

    @JsonManagedReference
    @OneToMany(mappedBy = "pkCabFactura", cascade = CascadeType.ALL)
    private List<DetFactura> detFactura;

    private BigDecimal valAbonoIngresado;
    private BigDecimal valAbonoAnterior;

}