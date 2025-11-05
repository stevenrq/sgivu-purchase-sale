package com.sgivu.purchasesale.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class VehicleSummary {
  Long id;
  String type;
  String brand;
  String model;
  String plate;
  String status;
}
