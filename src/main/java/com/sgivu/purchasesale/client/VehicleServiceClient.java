package com.sgivu.purchasesale.client;

import com.sgivu.purchasesale.dto.Car;
import com.sgivu.purchasesale.dto.Motorcycle;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/**
 * Cliente declarativo del microservicio de vehículos. Se reutiliza tanto para consultar
 * vehículos existentes (durante ventas) como para registrar nuevos (durante compras).
 */
@HttpExchange("/v1")
public interface VehicleServiceClient {

  /**
   * Recupera un automóvil previamente registrado, utilizado para validar disponibilidad en
   * contratos de venta.
   */
  @GetExchange("/cars/{id}")
  Car getCarById(@PathVariable Long id);

  /** Recupera una motocicleta previamente registrada. */
  @GetExchange("/motorcycles/{id}")
  Motorcycle getMotorcycleById(@PathVariable Long id);

  /**
   * Registra un automóvil adquirido; la creación se dispara solo en contratos de compra para no
   * duplicar inventario.
   */
  @PostExchange("/cars")
  Car createCar(@RequestBody Car car);

  /** Registra una motocicleta adquirida. */
  @PostExchange("/motorcycles")
  Motorcycle createMotorcycle(@RequestBody Motorcycle motorcycle);
}
