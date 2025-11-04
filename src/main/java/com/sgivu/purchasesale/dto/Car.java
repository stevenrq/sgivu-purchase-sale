package com.sgivu.purchasesale.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Car extends Vehicle {
  private String bodyType;
  private String fuelType;
  private Integer numberOfDoors;
}
