package com.sgivu.purchasesale.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
/**
 * Resumen compacto de un vehículo usado. Se construye a partir de respuestas del microservicio de
 * inventario y se utiliza para enriquecer contratos sin exponer la estructura completa del recurso
 * remoto.
 */
public class VehicleSummary {
  Long id;
  String type;
  String brand;
  String model;
  String plate;
  String status;
}
