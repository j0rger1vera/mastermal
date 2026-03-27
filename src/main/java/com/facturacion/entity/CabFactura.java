package com.facturacion.entity;

import com.facturacion.enums.EstadoFactura;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
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

    @Column(name = "num_factura", unique = true, nullable = false)
    private Long numeroFactura;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fecha;

    /* ========= Datos del cliente ========= */

    @NotBlank(message = "El RUC del cliente es obligatorio")
    @Column(name = "ruc_cliente", nullable = false, length = 15)
    private String rucCliente;

    @Column(name = "nombre")
    private String detalle;

    /* ========= Montos calculados ========= */

    @Column(precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(precision = 10, scale = 2)
    private BigDecimal igv;

    @Column(precision = 10, scale = 2)
    private BigDecimal total;

    @Column(precision = 10, scale = 2)
    private BigDecimal abono;

    @Column(precision = 10, scale = 2)
    private BigDecimal saldo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoFactura estado;

    /* ========= Detalle ========= */

    @NotNull(message = "El detalle de la factura no puede ser nulo")
    @Size(min = 1, message = "La factura debe tener al menos un detalle")
    @JsonManagedReference
    @OneToMany(
            mappedBy = "pkCabFactura",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<DetFactura> detFactura;

    /* ========= Control de abonos ========= */

    @PositiveOrZero(message = "El valor del abono ingresado no puede ser negativo")
    @Column(name = "val_abono_ingresado", precision = 10, scale = 2)
    private BigDecimal valAbonoIngresado;

    @PositiveOrZero(message = "El valor del abono anterior no puede ser negativo")
    @Column(name = "val_abono_anterior", precision = 10, scale = 2)
    private BigDecimal valAbonoAnterior;

    /* ========= Ciclo de vida ========= */

    @PrePersist
    public void prePersist() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }

        if (subtotal == null) subtotal = BigDecimal.ZERO;
        if (igv == null) igv = BigDecimal.ZERO;
        if (total == null) total = BigDecimal.ZERO;
        if (abono == null) abono = BigDecimal.ZERO;
        if (saldo == null) saldo = BigDecimal.ZERO;

        if (valAbonoIngresado == null) valAbonoIngresado = BigDecimal.ZERO;
        if (valAbonoAnterior == null) valAbonoAnterior = BigDecimal.ZERO;
    }
}