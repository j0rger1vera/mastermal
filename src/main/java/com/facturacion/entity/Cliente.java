package com.facturacion.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(name = "cliente")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Integer idCliente;

    @NotBlank(message = "El RUC o DNI es obligatorio")
    @Size(min = 8, max = 15, message = "El RUC o DNI debe tener entre 8 y 15 caracteres")
    @Column(name = "ruc_dni", nullable = false, length = 15, unique = true)
    private String rucDni;

    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Column(name = "nombre", nullable = false)
    private String nombre;

    private String direccion;

    @Email(message = "El correo no tiene un formato válido")
    private String correo;

    @Size(min = 7, max = 15, message = "El celular debe tener entre 7 y 15 dígitos")
    private String celular;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDate fechaCreacion;

    @PrePersist
    public void prePersist() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDate.now();
        }

        if (this.activo == null) {
            this.activo = true;
        }
    }
}
