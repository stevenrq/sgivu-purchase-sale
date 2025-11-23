package com.sgivu.purchasesale.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * Dirección asociada al cliente. Se replica desde el microservicio de clientes para mostrar la
 * localización en reportes y validar restricciones geográficas de contratos.
 */
public class Address {
  private Long id;
  private String street;
  private String number;
  private String city;
}
