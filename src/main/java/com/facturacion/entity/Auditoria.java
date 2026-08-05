package com.facturacion.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "funcionalidad", length = 50)
    private String funcionalidad;

    @Column(name = "operacion", length = 50)
    private String operacion;

    @Column(name = "fecha")
    private LocalDateTime fecha;

    @Column(name = "campo", columnDefinition = "TEXT")
    private String campo;

    @Column(name = "valor", columnDefinition = "TEXT")
    private String valor;

}
