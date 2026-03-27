package com.facturacion.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
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

    @NotNull(message = "El valor del abono es obligatorio")
    @Positive(message = "El valor del abono debe ser mayor a cero")
    @Column(name = "valor_abono", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorAbono;

    @Column(name = "fecha_abono", nullable = false)
    private LocalDateTime fechaAbono;

    @NotNull(message = "El valor anterior es obligatorio")
    @PositiveOrZero(message = "El valor anterior no puede ser negativo")
    @Column(name = "val_anterior", nullable = false, precision = 10, scale = 2)
    private BigDecimal valAnterior;

    @NotNull(message = "El total de la factura original es obligatorio")
    @Positive(message = "El total de la factura original debe ser mayor a cero")
    @Column(name = "total_factura_original", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalFacturaOriginal;

    @NotNull(message = "La factura asociada es obligatoria")
    @Column(name = "id_factura", nullable = false)
    private Integer pkCabFactura;

    @PrePersist
    public void prePersist() {
        if (this.fechaAbono == null) {
            this.fechaAbono = LocalDateTime.now();
        }
    }
}