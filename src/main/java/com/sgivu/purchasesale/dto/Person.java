package com.sgivu.purchasesale.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
/**
 * Cliente de tipo persona natural retornado por el microservicio de clientes. Se utiliza para
 * validar identidad y mostrar datos legibles en contratos y reportes.
 */
public class Person extends Client {
  private Long nationalId;
  private String firstName;
  private String lastName;
}
