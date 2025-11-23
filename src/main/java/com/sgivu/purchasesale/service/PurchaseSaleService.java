package com.sgivu.purchasesale.service;

import com.sgivu.purchasesale.dto.PurchaseSaleFilterCriteria;
import com.sgivu.purchasesale.dto.PurchaseSaleRequest;
import com.sgivu.purchasesale.entity.PurchaseSale;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PurchaseSaleService {

  /**
   * Registra un contrato asegurando consistencia con inventario, clientes y usuarios externos.
   *
   * @param purchaseSaleRequest payload con datos financieros, pagos y datos del vehículo (en
   *     compras)
   * @return contrato persistido con ids de relaciones resueltas
   */
  PurchaseSale create(PurchaseSaleRequest purchaseSaleRequest);

  /**
   * Busca un contrato por id sin enriquecer datos externos.
   *
   * @param id identificador del contrato
   * @return contrato encontrado o {@link Optional#empty()}
   */
  Optional<PurchaseSale> findById(Long id);

  /** Retorna todos los contratos sin paginar (uso acotado para lotes pequeños/reportes). */
  List<PurchaseSale> findAll();

  /**
   * Retorna contratos paginados, ideal para consumo desde UI o integraciones que manejan
   * paginación.
   *
   * @param pageable configuración de página/orden
   * @return página de contratos
   */
  Page<PurchaseSale> findAll(Pageable pageable);

  /**
   * Actualiza un contrato manteniendo su tipo original y revalidando reglas de negocio.
   *
   * @param id identificador del contrato
   * @param purchaseSaleRequest datos a modificar
   * @return contrato actualizado o vacío si no existe
   */
  Optional<PurchaseSale> update(Long id, PurchaseSaleRequest purchaseSaleRequest);

  /** Elimina un contrato por id. */
  void deleteById(Long id);

  /**
   * Lista las operaciones realizadas con un cliente específico (persona o empresa).
   *
   * @param clientId id de cliente validado en microservicio externo
   * @return contratos asociados
   */
  List<PurchaseSale> findByClientId(Long clientId);

  /**
   * Lista las operaciones gestionadas por un usuario interno.
   *
   * @param userId id de usuario validado en microservicio externo
   * @return contratos asociados al gestor
   */
  List<PurchaseSale> findByUserId(Long userId);

  /**
   * Obtiene las operaciones asociadas a un vehículo concreto dentro del inventario de usados.
   *
   * @param vehicleId id del vehículo validado en microservicio externo
   * @return historial de contratos
   */
  List<PurchaseSale> findByVehicleId(Long vehicleId);

  /**
   * Búsqueda avanzada aplicando filtros combinados de negocio.
   *
   * @param criteria filtros opcionales (estado, tipo, fechas, precios, término libre)
   * @param pageable paginación
   * @return página filtrada de contratos
   */
  Page<PurchaseSale> search(PurchaseSaleFilterCriteria criteria, Pageable pageable);
}
