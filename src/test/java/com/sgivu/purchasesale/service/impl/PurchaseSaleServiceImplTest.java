package com.sgivu.purchasesale.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sgivu.purchasesale.client.ClientServiceClient;
import com.sgivu.purchasesale.client.UserServiceClient;
import com.sgivu.purchasesale.client.VehicleServiceClient;
import com.sgivu.purchasesale.dto.Car;
import com.sgivu.purchasesale.dto.Person;
import com.sgivu.purchasesale.dto.PurchaseSaleRequest;
import com.sgivu.purchasesale.dto.User;
import com.sgivu.purchasesale.entity.PurchaseSale;
import com.sgivu.purchasesale.enums.ContractStatus;
import com.sgivu.purchasesale.enums.ContractType;
import com.sgivu.purchasesale.enums.PaymentMethod;
import com.sgivu.purchasesale.mapper.PurchaseSaleMapper;
import com.sgivu.purchasesale.mapper.PurchaseSaleMapperImpl;
import com.sgivu.purchasesale.repository.PurchaseSaleRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PurchaseSaleServiceImplTest {

  private static final long CLIENT_ID = 100L;
  private static final long USER_ID = 200L;
  private static final long VEHICLE_ID = 300L;
  private static final double PURCHASE_PRICE = 15000000d;

  @Mock private PurchaseSaleRepository purchaseSaleRepository;
  @Mock private ClientServiceClient clientServiceClient;
  @Mock private VehicleServiceClient vehicleServiceClient;
  @Mock private UserServiceClient userServiceClient;

  private PurchaseSaleServiceImpl purchaseSaleService;
  private PurchaseSaleMapper purchaseSaleMapper;

  @BeforeEach
  void setUp() {
    purchaseSaleMapper = new PurchaseSaleMapperImpl();
    purchaseSaleService =
        new PurchaseSaleServiceImpl(
            purchaseSaleRepository,
            purchaseSaleMapper,
            clientServiceClient,
            vehicleServiceClient,
            userServiceClient);
  }

  @Test
  @DisplayName("create debe persistir una compra con defaults y validar entidades externas")
  void create_ShouldPersistPurchaseWithDefaults() {
    PurchaseSaleRequest request = buildBaseRequest();
    request.setContractStatus(ContractStatus.ACTIVE);

    configureSuccessfulExternalLookups();

    PurchaseSale existingPurchase = new PurchaseSale();
    existingPurchase.setId(900L);
    existingPurchase.setVehicleId(VEHICLE_ID);
    existingPurchase.setContractType(ContractType.PURCHASE);
    existingPurchase.setContractStatus(ContractStatus.COMPLETED);

    when(purchaseSaleRepository.findByVehicleId(VEHICLE_ID))
        .thenReturn(List.of(existingPurchase));
    when(purchaseSaleRepository.save(any(PurchaseSale.class)))
        .thenAnswer(
            invocation -> {
              PurchaseSale entity = invocation.getArgument(0);
              entity.setId(999L);
              return entity;
            });

    PurchaseSale result = purchaseSaleService.create(request);

    assertThat(result.getId()).isEqualTo(999L);
    assertThat(result.getContractType()).isEqualTo(ContractType.PURCHASE);
    assertThat(result.getContractStatus()).isEqualTo(ContractStatus.ACTIVE);
    assertThat(result.getSalePrice()).isZero();
    assertThat(result.getPurchasePrice()).isEqualTo(PURCHASE_PRICE);

    ArgumentCaptor<PurchaseSale> captor = ArgumentCaptor.forClass(PurchaseSale.class);
    verify(purchaseSaleRepository).save(captor.capture());
    assertThat(captor.getValue().getSalePrice()).isZero();

    verify(clientServiceClient).getPersonById(CLIENT_ID);
    verify(userServiceClient).getUserById(USER_ID);
    verify(vehicleServiceClient).getCarById(VEHICLE_ID);
  }

  @Test
  @DisplayName("create debe persistir una venta cuando existe una compra activa o completada")
  void create_ShouldPersistSaleWithValidPurchase() {
    PurchaseSaleRequest request = buildBaseRequest();
    request.setContractType(ContractType.SALE);
    request.setSalePrice(19000000d);
    request.setContractStatus(ContractStatus.PENDING);

    configureSuccessfulExternalLookups();

    PurchaseSale existingPurchase = new PurchaseSale();
    existingPurchase.setId(50L);
    existingPurchase.setVehicleId(VEHICLE_ID);
    existingPurchase.setContractType(ContractType.PURCHASE);
    existingPurchase.setContractStatus(ContractStatus.COMPLETED);

    when(purchaseSaleRepository.findByVehicleId(VEHICLE_ID))
        .thenReturn(List.of(existingPurchase));
    when(purchaseSaleRepository.save(any(PurchaseSale.class)))
        .thenAnswer(
            invocation -> {
              PurchaseSale entity = invocation.getArgument(0);
              entity.setId(1000L);
              return entity;
            });

    PurchaseSale result = purchaseSaleService.create(request);

    assertThat(result.getId()).isEqualTo(1000L);
    assertThat(result.getContractType()).isEqualTo(ContractType.SALE);
    assertThat(result.getSalePrice()).isEqualTo(19000000d);
    assertThat(result.getContractStatus()).isEqualTo(ContractStatus.PENDING);
  }

  @Test
  @DisplayName("create debe rechazar ventas sin una compra previa válida")
  void create_WhenSaleHasNoInventory_ShouldThrowException() {
    PurchaseSaleRequest request = buildBaseRequest();
    request.setContractType(ContractType.SALE);
    request.setSalePrice(19000000d);

    configureSuccessfulExternalLookups();
    when(purchaseSaleRepository.findByVehicleId(VEHICLE_ID)).thenReturn(List.of());

    assertThatThrownBy(() -> purchaseSaleService.create(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("no cuenta con una compra activa o completada");
  }

  @Test
  @DisplayName("create debe rechazar ventas con precio de venta inválido")
  void create_WhenSalePriceIsInvalid_ShouldThrowException() {
    PurchaseSaleRequest request = buildBaseRequest();
    request.setContractType(ContractType.SALE);
    request.setSalePrice(0d);

    configureSuccessfulExternalLookups();

    PurchaseSale existingPurchase = new PurchaseSale();
    existingPurchase.setId(50L);
    existingPurchase.setVehicleId(VEHICLE_ID);
    existingPurchase.setContractType(ContractType.PURCHASE);
    existingPurchase.setContractStatus(ContractStatus.COMPLETED);

    when(purchaseSaleRepository.findByVehicleId(VEHICLE_ID))
        .thenReturn(List.of(existingPurchase));

    assertThatThrownBy(() -> purchaseSaleService.create(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("El precio de venta debe ser mayor a cero.");
  }

  @Test
  @DisplayName("create debe rechazar ventas cuando ya existe una venta pendiente, activa o completada")
  void create_WhenSaleAlreadyExists_ShouldThrowException() {
    PurchaseSaleRequest request = buildBaseRequest();
    request.setContractType(ContractType.SALE);
    request.setSalePrice(19000000d);
    request.setContractStatus(ContractStatus.ACTIVE);

    configureSuccessfulExternalLookups();

    PurchaseSale existingPurchase = new PurchaseSale();
    existingPurchase.setId(50L);
    existingPurchase.setVehicleId(VEHICLE_ID);
    existingPurchase.setContractType(ContractType.PURCHASE);
    existingPurchase.setContractStatus(ContractStatus.COMPLETED);

    PurchaseSale existingSale = new PurchaseSale();
    existingSale.setId(75L);
    existingSale.setVehicleId(VEHICLE_ID);
    existingSale.setContractType(ContractType.SALE);
    existingSale.setContractStatus(ContractStatus.ACTIVE);

    when(purchaseSaleRepository.findByVehicleId(VEHICLE_ID))
        .thenReturn(List.of(existingPurchase, existingSale));

    assertThatThrownBy(() -> purchaseSaleService.create(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("ya cuenta con una venta registrada");
  }

  @Test
  @DisplayName("create debe impedir compras duplicadas cuando exista una compra pendiente/activa")
  void create_WhenVehicleHasActivePurchase_ShouldThrowException() {
    PurchaseSaleRequest request = buildBaseRequest();

    configureSuccessfulExternalLookups();
    PurchaseSale existing = new PurchaseSale();
    existing.setId(10L);
    existing.setVehicleId(VEHICLE_ID);
    existing.setContractType(ContractType.PURCHASE);
    existing.setContractStatus(ContractStatus.ACTIVE);

    when(purchaseSaleRepository.findByVehicleId(VEHICLE_ID)).thenReturn(List.of(existing));

    assertThatThrownBy(() -> purchaseSaleService.create(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("ya tiene una compra registrada");
  }

  @Test
  @DisplayName("update debe mantener la lógica de compra y forzar salePrice en cero")
  void update_ShouldApplyPurchaseDefaults() {
    PurchaseSaleRequest request = buildBaseRequest();
    request.setContractStatus(ContractStatus.COMPLETED);

    configureSuccessfulExternalLookups();
    when(purchaseSaleRepository.findByVehicleId(VEHICLE_ID)).thenReturn(List.of());

    PurchaseSale stored = new PurchaseSale();
    stored.setId(77L);
    stored.setClientId(CLIENT_ID);
    stored.setUserId(USER_ID);
    stored.setVehicleId(VEHICLE_ID);
    stored.setPurchasePrice(10000000d);
    stored.setSalePrice(5000000d);
    stored.setContractType(ContractType.PURCHASE);
    stored.setContractStatus(ContractStatus.PENDING);

    when(purchaseSaleRepository.findById(77L)).thenReturn(Optional.of(stored));
    when(purchaseSaleRepository.save(any(PurchaseSale.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Optional<PurchaseSale> result = purchaseSaleService.update(77L, request);

    assertThat(result).isPresent();
    PurchaseSale updated = result.orElseThrow();

    assertThat(updated.getContractType()).isEqualTo(ContractType.PURCHASE);
    assertThat(updated.getSalePrice()).isZero();
    assertThat(updated.getContractStatus()).isEqualTo(ContractStatus.COMPLETED);
    assertThat(updated.getPurchasePrice()).isEqualTo(PURCHASE_PRICE);

    verify(purchaseSaleRepository, times(1)).findByVehicleId(VEHICLE_ID);
    verify(purchaseSaleRepository).save(eq(stored));
  }

  @Test
  @DisplayName("update no debe permitir cambiar el tipo de contrato")
  void update_ShouldRejectContractTypeChange() {
    PurchaseSaleRequest request = buildBaseRequest();
    request.setContractType(ContractType.SALE);
    request.setSalePrice(20000000d);

    configureSuccessfulExternalLookups();
    when(purchaseSaleRepository.findByVehicleId(VEHICLE_ID)).thenReturn(List.of());

    PurchaseSale stored = new PurchaseSale();
    stored.setId(88L);
    stored.setClientId(CLIENT_ID);
    stored.setUserId(USER_ID);
    stored.setVehicleId(VEHICLE_ID);
    stored.setPurchasePrice(PURCHASE_PRICE);
    stored.setSalePrice(0d);
    stored.setContractType(ContractType.PURCHASE);
    stored.setContractStatus(ContractStatus.ACTIVE);

    when(purchaseSaleRepository.findById(88L)).thenReturn(Optional.of(stored));

    assertThatThrownBy(() -> purchaseSaleService.update(88L, request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("No es posible cambiar el tipo de contrato");
  }

  @Test
  @DisplayName("update debe permitir cancelar una venta incluso sin compras activas")
  void updateSale_ShouldAllowCancellationWithoutActivePurchase() {
    PurchaseSaleRequest request = buildBaseRequest();
    request.setContractType(ContractType.SALE);
    request.setSalePrice(20000000d);
    request.setContractStatus(ContractStatus.CANCELED);

    configureSuccessfulExternalLookups();

    PurchaseSale storedSale = new PurchaseSale();
    storedSale.setId(91L);
    storedSale.setClientId(CLIENT_ID);
    storedSale.setUserId(USER_ID);
    storedSale.setVehicleId(VEHICLE_ID);
    storedSale.setPurchasePrice(PURCHASE_PRICE);
    storedSale.setSalePrice(18000000d);
    storedSale.setContractType(ContractType.SALE);
    storedSale.setContractStatus(ContractStatus.ACTIVE);

    PurchaseSale canceledPurchase = new PurchaseSale();
    canceledPurchase.setId(13L);
    canceledPurchase.setVehicleId(VEHICLE_ID);
    canceledPurchase.setContractType(ContractType.PURCHASE);
    canceledPurchase.setContractStatus(ContractStatus.CANCELED);

    when(purchaseSaleRepository.findByVehicleId(VEHICLE_ID))
        .thenReturn(List.of(canceledPurchase, storedSale));
    when(purchaseSaleRepository.findById(91L)).thenReturn(Optional.of(storedSale));
    when(purchaseSaleRepository.save(any(PurchaseSale.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Optional<PurchaseSale> result = purchaseSaleService.update(91L, request);

    assertThat(result).isPresent();
    assertThat(result.orElseThrow().getContractStatus()).isEqualTo(ContractStatus.CANCELED);
  }

  private PurchaseSaleRequest buildBaseRequest() {
    PurchaseSaleRequest request = new PurchaseSaleRequest();
    request.setClientId(CLIENT_ID);
    request.setUserId(USER_ID);
    request.setVehicleId(VEHICLE_ID);
    request.setPurchasePrice(PURCHASE_PRICE);
    request.setPaymentLimitations("Máximo 5M en efectivo");
    request.setPaymentTerms("Pago inmediato");
    request.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
    request.setObservations("Compra de prueba");
    return request;
  }

  private void configureSuccessfulExternalLookups() {
    Person person = new Person();
    person.setId(CLIENT_ID);
    when(clientServiceClient.getPersonById(CLIENT_ID)).thenReturn(person);

    User user = new User();
    user.setId(USER_ID);
    when(userServiceClient.getUserById(USER_ID)).thenReturn(user);

    Car car = new Car();
    car.setId(VEHICLE_ID);
    when(vehicleServiceClient.getCarById(VEHICLE_ID)).thenReturn(car);
  }
}
