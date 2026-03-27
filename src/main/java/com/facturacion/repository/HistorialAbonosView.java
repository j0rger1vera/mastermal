package com.facturacion.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface HistorialAbonosView {

    Integer getIdAbono();
    Integer getNumeroFactura();
    BigDecimal getAbono();
    LocalDateTime getFechaAbono();
    BigDecimal getAbonoAnterior();
    BigDecimal getTotalFactura();
    String getNombreCliente();
}
