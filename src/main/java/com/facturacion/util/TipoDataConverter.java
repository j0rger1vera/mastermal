package com.facturacion.util;

import com.facturacion.entity.Abono;
import com.facturacion.entity.CabFactura;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class TipoDataConverter {

    public Abono traducirFacturaToAbono(CabFactura cabFactura){
        LocalDateTime ahora = LocalDateTime.now();

        // 2. Formatear la fecha y hora
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String fechaFormateada = ahora.format(formato);

        Abono abono = Abono.builder()
                .valorAbono(cabFactura.getValAbonoIngresado())
                .valAnterior(cabFactura.getValAbonoAnterior())
                .totalFacturaOriginal(cabFactura.getTotal())
                .pkCabFactura(cabFactura.getIdFactura())
                .fechaAbono(fechaFormateada)
                .build();

        return abono;
    }

    public BigDecimal toBigDecimal(String value) {
        if (value == null || value.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }

        return new BigDecimal(value.trim());
    }

    public String toMoneyString(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

}
