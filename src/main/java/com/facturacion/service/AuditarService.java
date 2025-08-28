package com.facturacion.service;

import com.facturacion.dto.ParametroRegistro;
import com.facturacion.entity.Auditoria;
import com.facturacion.entity.Cliente;
import com.facturacion.repository.AuditarRepository;
import com.facturacion.repository.ClienteRepository;
import com.facturacion.util.ValoresRegistroAuditoriaBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;


@SuppressWarnings({"checkstyle:MissingJavadocMethod", "checkstyle:MissingJavadocType"})
@Service
public class AuditarService {
  @Autowired
  private ValoresRegistroAuditoriaBuilder builder;

  @Autowired
  private AuditarRepository auditoriaRepository;

  @Autowired
  public AuditarService(AuditarRepository auditoriaRepository) {
    this.builder = new ValoresRegistroAuditoriaBuilder();
    this.auditoriaRepository = auditoriaRepository;
  }

  public List<Auditoria> listaRegistroMovimientos() {
        return this.auditoriaRepository.getMovimientos();
    }

  public Auditoria registrarMovimiento(Object movimiento, String funcion, String operacion) {
    return this.auditoriaRepository.save(armarValores(movimiento, funcion, operacion));
  }

  public Auditoria armarValores(Object datos, String funcion, String operacion) {
    ParametroRegistro parametros = builder.armarValores(datos);
    return Auditoria.builder()
        .campo(parametros.getNombresCampos())
        .valor(parametros.getValoresCampos())
        .fecha(LocalDateTime.now())
        .funcionalidad(funcion)
        .operacion(operacion)
        .build();
  }

}
