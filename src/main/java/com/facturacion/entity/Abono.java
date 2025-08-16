package com.facturacion.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Abono {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_abono")
    private Integer idAbono;

    @Column(name = "valor_abono", columnDefinition = "DECIMAL(10,2)")
    private String valorAbono;

    @Column(name = "fecha_abono")
    private LocalDate fechaAbono;

    @Column(name = "val_anterior", columnDefinition = "DECIMAL(10,2)")
    private String valAnterior;

    @Column(name = "total_factura_original", columnDefinition = "DECIMAL(10,2)")
    private String totalFacturaOriginal;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "id_factura")
    private CabFactura pkCabFactura;
}
