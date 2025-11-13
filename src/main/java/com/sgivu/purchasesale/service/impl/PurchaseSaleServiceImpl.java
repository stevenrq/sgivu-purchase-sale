package com.sgivu.purchasesale.service.impl;

import com.sgivu.purchasesale.client.ClientServiceClient;
import com.sgivu.purchasesale.client.UserServiceClient;
import com.sgivu.purchasesale.client.VehicleServiceClient;
import com.sgivu.purchasesale.dto.Car;
import com.sgivu.purchasesale.dto.Motorcycle;
import com.sgivu.purchasesale.dto.PurchaseSaleFilterCriteria;
import com.sgivu.purchasesale.dto.PurchaseSaleRequest;
import com.sgivu.purchasesale.dto.Vehicle;
import com.sgivu.purchasesale.dto.VehicleCreationRequest;
import com.sgivu.purchasesale.entity.PurchaseSale;
import com.sgivu.purchasesale.enums.ContractStatus;
import com.sgivu.purchasesale.enums.ContractType;
import com.sgivu.purchasesale.enums.VehicleType;
import com.sgivu.purchasesale.mapper.PurchaseSaleMapper;
import com.sgivu.purchasesale.repository.PurchaseSaleRepository;
import com.sgivu.purchasesale.service.PurchaseSaleService;
import com.sgivu.purchasesale.specification.PurchaseSaleSpecifications;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;

@Service
@Transactional(readOnly = true)
public class PurchaseSaleServiceImpl implements PurchaseSaleService {

  private final PurchaseSaleRepository purchaseSaleRepository;
  private final PurchaseSaleMapper purchaseSaleMapper;
  private final ClientServiceClient clientServiceClient;
  private final VehicleServiceClient vehicleServiceClient;
  private final UserServiceClient userServiceClient;

  public PurchaseSaleServiceImpl(
      PurchaseSaleRepository purchaseSaleRepository,
      PurchaseSaleMapper purchaseSaleMapper,
      ClientServiceClient clientServiceClient,
      VehicleServiceClient vehicleServiceClient,
      UserServiceClient userServiceClient) {
    this.purchaseSaleRepository = purchaseSaleRepository;
    this.purchaseSaleMapper = purchaseSaleMapper;
    this.clientServiceClient = clientServiceClient;
    this.vehicleServiceClient = vehicleServiceClient;
    this.userServiceClient = userServiceClient;
  }

  /**
   * Crea un nuevo contrato de compra o venta aplicando todas las reglas de negocio. Normaliza el
   * tipo de contrato, resuelve IDs en servicios externos, registra el vehículo si corresponde,
   * valida precios y garantiza que no existan contratos conflictivos sobre el mismo vehículo.
   *
   * @param purchaseSaleRequest datos del contrato a registrar
   * @return contrato creado y persistido
   */
  @Transactional
  @Override
  public PurchaseSale create(PurchaseSaleRequest purchaseSaleRequest) {
    ContractType contractType = normalizeContractType(purchaseSaleRequest);
    Long resolvedClientId = resolveClientId(purchaseSaleRequest.getClientId());
    Long resolvedUserId = resolveUserId(purchaseSaleRequest.getUserId());
    Long resolvedVehicleId = resolveVehicleReference(contractType, purchaseSaleRequest);
    List<PurchaseSale> contractsByVehicle =
        purchaseSaleRepository.findByVehicleId(resolvedVehicleId);
    applyBusinessRules(
        contractType, purchaseSaleRequest, contractsByVehicle, null, resolvedVehicleId);

    PurchaseSale purchaseSale = purchaseSaleMapper.toPurchaseSale(purchaseSaleRequest);
    applyContractAdjustments(purchaseSale, purchaseSaleRequest);
    purchaseSale.setClientId(resolvedClientId);
    purchaseSale.setUserId(resolvedUserId);
    purchaseSale.setVehicleId(resolvedVehicleId);
    validatePurchasePrice(purchaseSale.getPurchasePrice());

    return purchaseSaleRepository.save(purchaseSale);
  }

  @Override
  public Optional<PurchaseSale> findById(Long id) {
    return purchaseSaleRepository.findById(id);
  }

  @Override
  public List<PurchaseSale> findAll() {
    return purchaseSaleRepository.findAll();
  }

  @Override
  public Page<PurchaseSale> findAll(Pageable pageable) {
    return purchaseSaleRepository.findAll(pageable);
  }

  @Override
  public Page<PurchaseSale> search(PurchaseSaleFilterCriteria criteria, Pageable pageable) {
    return purchaseSaleRepository.findAll(
        PurchaseSaleSpecifications.withFilters(criteria), pageable);
  }

  /**
   * Actualiza un contrato existente manteniendo el tipo original (no modificable), aplicando
   * nuevamente reglas de negocio, validando precios y resolviendo IDs en servicios externos. Evita
   * inconsistencias respecto a otros contratos asociados al mismo vehículo.
   *
   * @param id ID del contrato a actualizar
   * @param purchaseSaleRequest datos actualizados del contrato
   * @return contrato actualizado si existe
   */
  @Transactional
  @Override
  public Optional<PurchaseSale> update(Long id, PurchaseSaleRequest purchaseSaleRequest) {
    ContractType contractType = normalizeContractType(purchaseSaleRequest);
    Long resolvedClientId = resolveClientId(purchaseSaleRequest.getClientId());
    Long resolvedUserId = resolveUserId(purchaseSaleRequest.getUserId());
    Long resolvedVehicleId = resolveVehicleId(purchaseSaleRequest.getVehicleId());
    List<PurchaseSale> contractsByVehicle =
        purchaseSaleRepository.findByVehicleId(resolvedVehicleId);

    return purchaseSaleRepository
        .findById(id)
        .map(
            existingPurchaseSale -> {
              if (existingPurchaseSale.getContractType() != contractType) {
                throw new IllegalArgumentException(
                    "No es posible cambiar el tipo de contrato una vez creado.");
              }
              applyBusinessRules(
                  contractType,
                  purchaseSaleRequest,
                  contractsByVehicle,
                  existingPurchaseSale.getId(),
                  resolvedVehicleId);
              purchaseSaleMapper.updatePurchaseSaleFromRequest(
                  purchaseSaleRequest, existingPurchaseSale);
              applyContractAdjustments(existingPurchaseSale, purchaseSaleRequest);
              existingPurchaseSale.setClientId(resolvedClientId);
              existingPurchaseSale.setUserId(resolvedUserId);
              existingPurchaseSale.setVehicleId(resolvedVehicleId);
              validatePurchasePrice(existingPurchaseSale.getPurchasePrice());
              return purchaseSaleRepository.save(existingPurchaseSale);
            });
  }

  @Transactional
  @Override
  public void deleteById(Long id) {
    purchaseSaleRepository.deleteById(id);
  }

  @Override
  public List<PurchaseSale> findByClientId(Long clientId) {
    Long resolvedClientId = resolveClientId(clientId);
    return purchaseSaleRepository.findByClientId(resolvedClientId);
  }

  @Override
  public List<PurchaseSale> findByUserId(Long userId) {
    resolveUserId(userId);
    return purchaseSaleRepository.findByUserId(userId);
  }

  @Override
  public List<PurchaseSale> findByVehicleId(Long vehicleId) {
    Long resolvedVehicleId = resolveVehicleId(vehicleId);
    return purchaseSaleRepository.findByVehicleId(resolvedVehicleId);
  }

  /**
   * Normaliza el tipo de contrato asignando un valor por defecto (PURCHASE) si no se especifica y
   * configura el estado inicial si está ausente.
   *
   * @param purchaseSaleRequest request del contrato
   * @return tipo de contrato resultante
   */
  private ContractType normalizeContractType(PurchaseSaleRequest purchaseSaleRequest) {
    ContractType contractType =
        Optional.ofNullable(purchaseSaleRequest.getContractType()).orElse(ContractType.PURCHASE);
    purchaseSaleRequest.setContractType(contractType);
    if (purchaseSaleRequest.getContractStatus() == null) {
      purchaseSaleRequest.setContractStatus(ContractStatus.PENDING);
    }
    return contractType;
  }

  /**
   * Determina cómo obtener el vehículo asociado al contrato dependiendo del tipo: - Para compras:
   * crea el vehículo si no se envía un ID. - Para ventas: exige un ID válido y prohíbe el envío de
   * datos detallados.
   *
   * @param contractType tipo de contrato
   * @param purchaseSaleRequest request con ID o datos del vehículo
   * @return ID del vehículo asociado
   */
  private Long resolveVehicleReference(
      ContractType contractType, PurchaseSaleRequest purchaseSaleRequest) {
    if (contractType == ContractType.PURCHASE) {
      if (purchaseSaleRequest.getVehicleId() != null) {
        return resolveVehicleId(purchaseSaleRequest.getVehicleId());
      }
      Long vehicleId = registerVehicleForPurchase(purchaseSaleRequest);
      purchaseSaleRequest.setVehicleId(vehicleId);
      return vehicleId;
    }

    if (purchaseSaleRequest.getVehicleId() == null) {
      throw new IllegalArgumentException(
          "Debes seleccionar el vehículo asociado al contrato de venta.");
    }

    if (purchaseSaleRequest.getVehicleData() != null) {
      throw new IllegalArgumentException(
          "Los datos detallados del vehículo solo deben enviarse para contratos de compra.");
    }

    return resolveVehicleId(purchaseSaleRequest.getVehicleId());
  }

  /**
   * Registra un nuevo vehículo en el servicio externo según el tipo especificado (carro o
   * motocicleta). Valida obligatoriamente todos los atributos necesarios.
   *
   * @param purchaseSaleRequest request que contiene los datos del vehículo
   * @return ID del vehículo registrado
   */
  private Long registerVehicleForPurchase(PurchaseSaleRequest purchaseSaleRequest) {
    VehicleCreationRequest vehicleData = purchaseSaleRequest.getVehicleData();
    if (vehicleData == null) {
      throw new IllegalArgumentException(
          "Debes proporcionar los datos del vehículo para registrar una compra.");
    }

    VehicleType vehicleType =
        Optional.ofNullable(vehicleData.getVehicleType())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Debes especificar si se trata de un automóvil o una motocicleta."));

    if (vehicleType == VehicleType.CAR) {
      Car car = applyCommonVehicleAttributes(new Car(), vehicleData, purchaseSaleRequest);
      car.setBodyType(
          requireText(vehicleData.getBodyType(), "La carrocería del automóvil es obligatoria."));
      car.setFuelType(
          requireText(
              vehicleData.getFuelType(), "El tipo de combustible del automóvil es obligatorio."));
      car.setNumberOfDoors(
          requirePositiveInteger(
              vehicleData.getNumberOfDoors(), "Debes indicar el número de puertas del automóvil."));
      return vehicleServiceClient.createCar(car).getId();
    }

    Motorcycle motorcycle =
        applyCommonVehicleAttributes(new Motorcycle(), vehicleData, purchaseSaleRequest);
    motorcycle.setMotorcycleType(
        requireText(vehicleData.getMotorcycleType(), "Debes indicar el tipo de motocicleta."));
    return vehicleServiceClient.createMotorcycle(motorcycle).getId();
  }

  /**
   * Aplica y valida atributos comunes para cualquier tipo de vehículo al momento de registrarlo
   * (marca, modelo, placa, motor, ciudad, chasis, año, transmisión, kilometraje, precios, etc.).
   *
   * @param target instancia del vehículo a completar
   * @param vehicleData datos enviados por el cliente
   * @param request request del contrato (para fallback de precios)
   * @return vehículo con atributos comunes inicializados
   */
  private <T extends Vehicle> T applyCommonVehicleAttributes(
      T target, VehicleCreationRequest vehicleData, PurchaseSaleRequest request) {
    target.setBrand(requireText(vehicleData.getBrand(), "La marca del vehículo es obligatoria."));
    target.setModel(requireText(vehicleData.getModel(), "El modelo del vehículo es obligatorio."));
    target.setCapacity(
        requirePositiveInteger(
            vehicleData.getCapacity(), "La capacidad de pasajeros del vehículo es obligatoria."));
    target.setLine(requireText(vehicleData.getLine(), "La línea del vehículo es obligatoria."));
    target.setPlate(
        requireText(vehicleData.getPlate(), "La placa del vehículo es obligatoria.").toUpperCase());
    target.setMotorNumber(
        requireText(
            vehicleData.getMotorNumber(), "El número de motor del vehículo es obligatorio."));
    target.setSerialNumber(
        requireText(
            vehicleData.getSerialNumber(), "El número serial del vehículo es obligatorio."));
    target.setChassisNumber(
        requireText(
            vehicleData.getChassisNumber(), "El número de chasis del vehículo es obligatorio."));
    target.setColor(requireText(vehicleData.getColor(), "El color del vehículo es obligatorio."));
    target.setCityRegistered(
        requireText(
            vehicleData.getCityRegistered(),
            "La ciudad de matrícula del vehículo es obligatoria."));
    target.setYear(requireValidYear(vehicleData.getYear()));
    target.setMileage(requireNonNegativeInteger(vehicleData.getMileage()));
    target.setTransmission(
        requireText(vehicleData.getTransmission(), "La transmisión del vehículo es obligatoria."));
    target.setPurchasePrice(resolveVehiclePurchasePrice(vehicleData, request.getPurchasePrice()));
    target.setSalePrice(resolveVehicleSalePrice(vehicleData.getSalePrice()));
    target.setPhotoUrl(normalizeNullable(vehicleData.getPhotoUrl()));
    target.setStatus("AVAILABLE");
    return target;
  }

  /**
   * Ejecuta todas las reglas de negocio relevantes según el tipo de contrato:
   *
   * <ul>
   *   <li>Compras: asegura que no exista una compra activa o pendiente para el vehículo.
   *   <li>Ventas: valida que exista una compra previa válida y que no haya otra venta activa.
   * </ul>
   *
   * @param contractType tipo del contrato
   * @param purchaseSaleRequest datos del contrato
   * @param contractsByVehicle contratos previos del mismo vehículo
   * @param excludedContractId ID a excluir (en caso de actualización)
   * @param vehicleId ID del vehículo asociado
   */
  private void applyBusinessRules(
      ContractType contractType,
      PurchaseSaleRequest purchaseSaleRequest,
      List<PurchaseSale> contractsByVehicle,
      Long excludedContractId,
      Long vehicleId) {
    if (contractType == ContractType.PURCHASE) {
      preparePurchaseRequest(purchaseSaleRequest);
      ensureNoActivePurchase(contractsByVehicle, excludedContractId, vehicleId);
    } else {
      prepareSaleRequest(purchaseSaleRequest, contractsByVehicle);
      ensureSalePrerequisites(
          contractsByVehicle,
          excludedContractId,
          vehicleId,
          purchaseSaleRequest.getContractStatus());
    }
  }

  /**
   * Prepara y valida los valores específicos para un contrato de compra, asignando un precio de
   * venta objetivo si no se proporcionó explícitamente.
   *
   * @param purchaseSaleRequest request del contrato de compra
   */
  private void preparePurchaseRequest(PurchaseSaleRequest purchaseSaleRequest) {
    Double targetSalePrice = null;
    if (purchaseSaleRequest.getVehicleData() != null) {
      targetSalePrice = purchaseSaleRequest.getVehicleData().getSalePrice();
    }
    if (targetSalePrice == null) {
      targetSalePrice = purchaseSaleRequest.getSalePrice();
    }
    purchaseSaleRequest.setSalePrice(targetSalePrice != null ? targetSalePrice : 0d);
  }

  /**
   * Prepara y valida los datos de un contrato de venta, asegurando que el precio de venta sea
   * válido y estableciendo el precio de compra desde compras previas.
   *
   * @param purchaseSaleRequest request del contrato de venta
   * @param contractsByVehicle historial de contratos del vehículo
   */
  private void prepareSaleRequest(
      PurchaseSaleRequest purchaseSaleRequest, List<PurchaseSale> contractsByVehicle) {
    Double salePrice = purchaseSaleRequest.getSalePrice();
    if (salePrice == null || salePrice <= 0) {
      throw new IllegalArgumentException("El precio de venta debe ser mayor a cero.");
    }
    purchaseSaleRequest.setPurchasePrice(
        findLatestPurchasePrice(contractsByVehicle, purchaseSaleRequest.getPurchasePrice()));
  }

  private String requireText(String value, String message) {
    if (!StringUtils.hasText(value)) {
      throw new IllegalArgumentException(message);
    }
    return value.trim();
  }

  private Integer requirePositiveInteger(Integer value, String message) {
    if (value == null || value <= 0) {
      throw new IllegalArgumentException(message);
    }
    return value;
  }

  private Integer requireNonNegativeInteger(Integer value) {
    if (value == null || value < 0) {
      throw new IllegalArgumentException("El kilometraje del vehículo es obligatorio.");
    }
    return value;
  }

  private Integer requireValidYear(Integer value) {
    Integer year = requirePositiveInteger(value, "El año del vehículo es obligatorio.");
    if (year < 1950 || year > 2050) {
      throw new IllegalArgumentException("El año del vehículo debe estar entre 1950 y 2050.");
    }
    return year;
  }

  private Double resolveVehiclePurchasePrice(
      VehicleCreationRequest vehicleData, Double fallbackPurchasePrice) {
    Double value =
        vehicleData.getPurchasePrice() != null
            ? vehicleData.getPurchasePrice()
            : fallbackPurchasePrice;
    if (value == null || value <= 0) {
      throw new IllegalArgumentException("El precio de compra del vehículo debe ser mayor a cero.");
    }
    return value;
  }

  private Double resolveVehicleSalePrice(Double salePrice) {
    if (salePrice == null) {
      return 0d;
    }
    if (salePrice < 0) {
      throw new IllegalArgumentException("El precio de venta del vehículo no puede ser negativo.");
    }
    return salePrice;
  }

  private String normalizeNullable(String value) {
    return StringUtils.hasText(value) ? value.trim() : null;
  }

  private void applyContractAdjustments(
      PurchaseSale purchaseSale, PurchaseSaleRequest purchaseSaleRequest) {
    purchaseSale.setContractType(purchaseSaleRequest.getContractType());
    purchaseSale.setContractStatus(purchaseSaleRequest.getContractStatus());
    if (purchaseSaleRequest.getContractType() == ContractType.PURCHASE) {
      purchaseSale.setSalePrice(Optional.ofNullable(purchaseSaleRequest.getSalePrice()).orElse(0d));
    } else {
      purchaseSale.setSalePrice(purchaseSaleRequest.getSalePrice());
    }
  }

  /**
   * Busca el precio de compra más reciente de un vehículo dentro de contratos válidos (activos o
   * completados). Si no existe, intenta usar el valor enviado como fallback.
   *
   * @param contractsByVehicle contratos asociados al vehículo
   * @param fallbackPurchasePrice precio alternativo si no hay compras previas
   * @return precio de compra válido
   */
  private Double findLatestPurchasePrice(
      List<PurchaseSale> contractsByVehicle, Double fallbackPurchasePrice) {
    return contractsByVehicle.stream()
        .filter(contract -> contract.getContractType() == ContractType.PURCHASE)
        .filter(
            contract ->
                EnumSet.of(ContractStatus.ACTIVE, ContractStatus.COMPLETED)
                    .contains(contract.getContractStatus()))
        .max(
            Comparator.comparing(
                PurchaseSale::getUpdatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
        .map(PurchaseSale::getPurchasePrice)
        .orElseGet(
            () -> {
              if (fallbackPurchasePrice != null && fallbackPurchasePrice > 0) {
                return fallbackPurchasePrice;
              }
              throw new IllegalArgumentException(
                  "No se encontró una compra válida asociada al vehículo.");
            });
  }

  /**
   * Verifica que el vehículo no tenga otra compra pendiente o activa asociada, evitando duplicidad
   * de compras sobre el mismo vehículo.
   *
   * @param contractsByVehicle contratos existentes del vehículo
   * @param excludedContractId contrato a ignorar (en actualizaciones)
   * @param vehicleId ID del vehículo
   */
  private void ensureNoActivePurchase(
      List<PurchaseSale> contractsByVehicle, Long excludedContractId, Long vehicleId) {
    boolean hasActiveOrPendingPurchase =
        contractsByVehicle.stream()
            .filter(
                existing ->
                    !Objects.equals(excludedContractId, existing.getId())
                        && existing.getContractType() == ContractType.PURCHASE)
            .anyMatch(
                existing ->
                    existing.getContractStatus() == ContractStatus.PENDING
                        || existing.getContractStatus() == ContractStatus.ACTIVE);

    if (hasActiveOrPendingPurchase) {
      throw new IllegalArgumentException(
          "El vehículo con id "
              + vehicleId
              + " ya tiene una compra registrada con estado pendiente o activa.");
    }
  }

  /**
   * Valida que el vehículo cumpla los requisitos para registrar una venta, incluyendo: - Existencia
   * de una compra activa/completada previa. - Ausencia de ventas en estado pendiente, activa o
   * completada.
   *
   * @param contractsByVehicle contratos existentes del vehículo
   * @param excludedContractId contrato a ignorar (en actualizaciones)
   * @param vehicleId ID del vehículo
   * @param targetStatus estado solicitado del contrato de venta
   */
  private void ensureSalePrerequisites(
      List<PurchaseSale> contractsByVehicle,
      Long excludedContractId,
      Long vehicleId,
      ContractStatus targetStatus) {
    boolean shouldValidateAvailability =
        EnumSet.of(ContractStatus.PENDING, ContractStatus.ACTIVE, ContractStatus.COMPLETED)
            .contains(targetStatus);

    if (shouldValidateAvailability) {
      boolean hasAvailableStock =
          contractsByVehicle.stream()
              .filter(contract -> contract.getContractType() == ContractType.PURCHASE)
              .anyMatch(
                  contract ->
                      EnumSet.of(ContractStatus.ACTIVE, ContractStatus.COMPLETED)
                          .contains(contract.getContractStatus()));

      if (!hasAvailableStock) {
        throw new IllegalArgumentException(
            "El vehículo con id "
                + vehicleId
                + " no cuenta con una compra activa o completada registrada.");
      }
    }

    boolean hasConflictingSale =
        contractsByVehicle.stream()
            .filter(
                contract ->
                    !Objects.equals(excludedContractId, contract.getId())
                        && contract.getContractType() == ContractType.SALE)
            .anyMatch(
                contract ->
                    EnumSet.of(
                            ContractStatus.PENDING, ContractStatus.ACTIVE, ContractStatus.COMPLETED)
                        .contains(contract.getContractStatus()));

    if (hasConflictingSale) {
      throw new IllegalArgumentException(
          "El vehículo con id "
              + vehicleId
              + " ya cuenta con una venta registrada en estado pendiente, activa o completada.");
    }
  }

  private void validatePurchasePrice(Double purchasePrice) {
    if (purchasePrice == null || purchasePrice <= 0) {
      throw new IllegalArgumentException("El precio de compra debe ser mayor a cero.");
    }
  }

  private Long resolveUserId(Long userId) {
    if (userId == null) {
      throw new IllegalArgumentException("El ID del usuario debe ser proporcionado.");
    }
    return userServiceClient.getUserById(userId).getId();
  }

  private Long resolveClientId(Long clientId) {
    if (clientId == null) {
      throw new IllegalArgumentException("El ID del cliente debe ser proporcionado.");
    }

    try {
      return clientServiceClient.getPersonById(clientId).getId();
    } catch (HttpClientErrorException exception) {
      if (exception.getStatusCode().value() != 404) {
        throw exception;
      }
    }

    try {
      return clientServiceClient.getCompanyById(clientId).getId();
    } catch (HttpClientErrorException exception) {
      if (exception.getStatusCode().value() == 404) {
        throw new IllegalArgumentException("Cliente no encontrado con id: " + clientId, exception);
      }
      throw exception;
    }
  }

  private Long resolveVehicleId(Long vehicleId) {
    if (vehicleId == null) {
      throw new IllegalArgumentException("El ID del vehículo debe ser proporcionado.");
    }

    try {
      return vehicleServiceClient.getCarById(vehicleId).getId();
    } catch (HttpClientErrorException exception) {
      if (exception.getStatusCode().value() != 404) {
        throw exception;
      }
    }

    try {
      return vehicleServiceClient.getMotorcycleById(vehicleId).getId();
    } catch (HttpClientErrorException exception) {
      if (exception.getStatusCode().value() == 404) {
        throw new IllegalArgumentException(
            "Vehículo no encontrado con id: " + vehicleId, exception);
      }
      throw exception;
    }
  }
}
