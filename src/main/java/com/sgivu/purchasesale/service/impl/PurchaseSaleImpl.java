package com.sgivu.purchasesale.service.impl;

import com.sgivu.purchasesale.client.ClientServiceClient;
import com.sgivu.purchasesale.client.UserServiceClient;
import com.sgivu.purchasesale.client.VehicleServiceClient;
import com.sgivu.purchasesale.dto.PurchaseSaleRequest;
import com.sgivu.purchasesale.entity.PurchaseSale;
import com.sgivu.purchasesale.repository.PurchaseSaleRepository;
import com.sgivu.purchasesale.service.PurchaseSaleService;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

@Service
@Transactional(readOnly = true)
public class PurchaseSaleImpl implements PurchaseSaleService {

  private final PurchaseSaleRepository purchaseSaleRepository;

  private final ClientServiceClient clientServiceClient;
  private final VehicleServiceClient vehicleServiceClient;
  private final UserServiceClient userServiceClient;

  public PurchaseSaleImpl(
      PurchaseSaleRepository purchaseSaleRepository,
      ClientServiceClient clientServiceClient,
      VehicleServiceClient vehicleServiceClient,
      UserServiceClient userServiceClient) {
    this.purchaseSaleRepository = purchaseSaleRepository;
    this.clientServiceClient = clientServiceClient;
    this.vehicleServiceClient = vehicleServiceClient;
    this.userServiceClient = userServiceClient;
  }

  @Transactional
  @Override
  public PurchaseSale save(PurchaseSale purchaseSale) {
    purchaseSale.setClientId(resolveClientId(purchaseSale.getClientId()));
    purchaseSale.setUserId(userServiceClient.getUserById(purchaseSale.getUserId()).getId());
    purchaseSale.setVehicleId(resolveVehicleId(purchaseSale.getVehicleId()));

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

  @Transactional
  @Override
  public Optional<PurchaseSale> update(Long id, PurchaseSaleRequest purchaseSaleRequest) {
    return purchaseSaleRepository
        .findById(id)
        .map(
            existingPurchaseSale -> {
              existingPurchaseSale.setClientId(purchaseSaleRequest.getClientId());
              existingPurchaseSale.setUserId(purchaseSaleRequest.getUserId());
              existingPurchaseSale.setVehicleId(purchaseSaleRequest.getVehicleId());
              existingPurchaseSale.setPurchasePrice(purchaseSaleRequest.getPurchasePrice());
              existingPurchaseSale.setSalePrice(purchaseSaleRequest.getSalePrice());
              existingPurchaseSale.setContractType(purchaseSaleRequest.getContractType());
              existingPurchaseSale.setContractStatus(purchaseSaleRequest.getContractStatus());
              existingPurchaseSale.setPaymentLimitations(
                  purchaseSaleRequest.getPaymentLimitations());
              existingPurchaseSale.setPaymentTerms(purchaseSaleRequest.getPaymentTerms());
              existingPurchaseSale.setPaymentMethod(purchaseSaleRequest.getPaymentMethod());
              existingPurchaseSale.setObservations(purchaseSaleRequest.getObservations());
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
    return purchaseSaleRepository.findByClientId(resolveClientId(clientId));
  }

  @Override
  public List<PurchaseSale> findByUserId(Long userId) {
    return purchaseSaleRepository.findByUserId(userId);
  }

  @Override
  public List<PurchaseSale> findByVehicleId(Long vehicleId) {
    return purchaseSaleRepository.findByVehicleId(resolveVehicleId(vehicleId));
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
