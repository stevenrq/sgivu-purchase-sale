package com.sgivu.purchasesale.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
/**
 * Datos mínimos del usuario interno que gestiona el contrato. Se obtiene del microservicio de
 * usuarios para mostrar responsable, contacto y trazabilidad en reportes.
 */
public class UserSummary {
  Long id;
  String fullName;
  String email;
  String username;
}
