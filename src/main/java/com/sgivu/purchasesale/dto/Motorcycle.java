package com.sgivu.purchasesale.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
/**
 * DTO para motocicletas que viaja hacia/desde el microservicio de inventario. Complementa los
 * datos básicos de {@link Vehicle} con el tipo de motocicleta para cálculos de valorización.
 */
public class Motorcycle extends Vehicle {
  private String motorcycleType;
}
