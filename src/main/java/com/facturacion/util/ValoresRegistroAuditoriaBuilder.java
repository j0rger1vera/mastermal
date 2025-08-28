package com.facturacion.util;

import com.facturacion.dto.ParametroRegistro;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

@Component
public class ValoresRegistroAuditoriaBuilder {

  public ParametroRegistro armarValores(Object datos) {
    StringBuilder nombresCampos = new StringBuilder();
    StringBuilder valoresCampos = new StringBuilder();

    if (datos == null) {
        return new ParametroRegistro("", "");
    }

    Field[] campos = datos.getClass().getDeclaredFields();

      for (Field campo : campos) {

              campo.setAccessible(true);
              try {
                  Object valor = campo.get(datos);

                  if (valor != null) {

                      if (nombresCampos.length() > 0) {
                          nombresCampos.append("  |  ");
                      }
                      nombresCampos.append(campo.getName());

                      if (valor != null) {
                          String valorString = valor.toString();
                          if (valoresCampos.length() > 0) {
                              valoresCampos.append("  |  ");
                          }
                          valoresCampos.append(valorString);
                      } else {

                          if (valoresCampos.length() > 0) {
                              valoresCampos.append("  |  ");
                          }
                          valoresCampos.append("null");
                      }
                  }
              } catch (IllegalAccessException e) {
                  e.printStackTrace();
              }
      }

        return new ParametroRegistro(nombresCampos.toString(), valoresCampos.toString());
    }
}
