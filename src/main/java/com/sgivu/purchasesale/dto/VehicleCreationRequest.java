package com.sgivu.purchasesale.dto;

import com.sgivu.purchasesale.enums.VehicleType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class VehicleCreationRequest {

  @NotNull(message = "El tipo de vehículo es obligatorio para registrar la compra.")
  private VehicleType vehicleType;

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
  private String photoUrl;

  // Car specific
  private String bodyType;
  private String fuelType;
  private Integer numberOfDoors;

  // Motorcycle specific
  private String motorcycleType;
}
