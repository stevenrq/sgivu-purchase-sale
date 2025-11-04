package com.sgivu.purchasesale.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Vehicle {
  private Long id;
  private String brand;
  private String model;
  private Integer capacity;
  private String line;
  private String plate;
  private String motorNumber;
  private String serialNumber;
  private String chassisNumber;
  private String color;
  private String cityRegistered;
  private Integer year;
  private Integer mileage;
  private String transmission;
  private Double purchasePrice;
  private Double salePrice;
  private boolean isAvailable;
}
