package com.sgivu.purchasesale.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sgivu.purchasesale.dto.ClientSummary;
import com.sgivu.purchasesale.dto.PurchaseSaleDetailResponse;
import com.sgivu.purchasesale.dto.UserSummary;
import com.sgivu.purchasesale.dto.VehicleSummary;
import com.sgivu.purchasesale.entity.PurchaseSale;
import com.sgivu.purchasesale.enums.ContractStatus;
import com.sgivu.purchasesale.enums.ContractType;
import com.sgivu.purchasesale.enums.PaymentMethod;
import com.sgivu.purchasesale.repository.PurchaseSaleRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class PurchaseSaleReportServiceTest {

  private static final Sort DEFAULT_SORT = Sort.unsorted();

  @Mock private PurchaseSaleRepository purchaseSaleRepository;
  @Mock private PurchaseSaleDetailService purchaseSaleDetailService;

  private PurchaseSaleReportService purchaseSaleReportService;

  @BeforeEach
  void setUp() {
    purchaseSaleReportService =
        new PurchaseSaleReportService(purchaseSaleRepository, purchaseSaleDetailService);
  }

  @Test
  @DisplayName("generatePdf debe producir un arreglo de bytes no vacío")
  void generatePdf_ShouldReturnDocumentBytes() {
    when(purchaseSaleRepository.findAll(DEFAULT_SORT)).thenReturn(List.of(sampleContract()));
    when(purchaseSaleDetailService.toDetails(anyList())).thenReturn(List.of(sampleDetail()));

    byte[] pdf = purchaseSaleReportService.generatePdf(null, null);

    assertThat(pdf).isNotEmpty();
    verify(purchaseSaleRepository).findAll(DEFAULT_SORT);
  }

  @Test
  @DisplayName("generateExcel debe producir un arreglo de bytes no vacío")
  void generateExcel_ShouldReturnDocumentBytes() {
    when(purchaseSaleRepository.findAll(DEFAULT_SORT)).thenReturn(List.of(sampleContract()));
    when(purchaseSaleDetailService.toDetails(anyList())).thenReturn(List.of(sampleDetail()));

    byte[] excel = purchaseSaleReportService.generateExcel(null, null);

    assertThat(excel).isNotEmpty();
    verify(purchaseSaleRepository).findAll(DEFAULT_SORT);
  }

  @Test
  @DisplayName("generateCsv debe producir un arreglo de bytes no vacío")
  void generateCsv_ShouldReturnDocumentBytes() {
    when(purchaseSaleRepository.findAll(DEFAULT_SORT)).thenReturn(List.of(sampleContract()));
    when(purchaseSaleDetailService.toDetails(anyList())).thenReturn(List.of(sampleDetail()));

    byte[] csv = purchaseSaleReportService.generateCsv(null, null);

    assertThat(csv).isNotEmpty();
    verify(purchaseSaleRepository).findAll(DEFAULT_SORT);
  }

  private PurchaseSale sampleContract() {
    PurchaseSale purchaseSale = new PurchaseSale();
    purchaseSale.setId(1L);
    purchaseSale.setClientId(10L);
    purchaseSale.setUserId(5L);
    purchaseSale.setVehicleId(7L);
    purchaseSale.setPurchasePrice(15000000d);
    purchaseSale.setSalePrice(18000000d);
    purchaseSale.setContractType(ContractType.SALE);
    purchaseSale.setContractStatus(ContractStatus.ACTIVE);
    purchaseSale.setPaymentLimitations("Máx 5M en efectivo");
    purchaseSale.setPaymentTerms("Pago contra entrega");
    purchaseSale.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
    purchaseSale.setObservations("Contrato de prueba");
    purchaseSale.setCreatedAt(LocalDateTime.now().minusDays(1));
    purchaseSale.setUpdatedAt(LocalDateTime.now());
    return purchaseSale;
  }

  private PurchaseSaleDetailResponse sampleDetail() {
    PurchaseSaleDetailResponse detail = new PurchaseSaleDetailResponse();
    detail.setId(1L);
    detail.setClientId(10L);
    detail.setUserId(5L);
    detail.setVehicleId(7L);
    detail.setPurchasePrice(15000000d);
    detail.setSalePrice(18000000d);
    detail.setContractType(ContractType.SALE);
    detail.setContractStatus(ContractStatus.ACTIVE);
    detail.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
    detail.setPaymentLimitations("Máx 5M en efectivo");
    detail.setPaymentTerms("Pago contra entrega");
    detail.setObservations("Contrato de prueba");
    detail.setCreatedAt(LocalDateTime.now().minusDays(1));
    detail.setUpdatedAt(LocalDateTime.now());
    detail.setClientSummary(
        ClientSummary.builder()
            .id(10L)
            .type("PERSON")
            .name("Juan Pérez")
            .identifier("CC 123456")
            .email("juan@example.com")
            .phoneNumber(3219876543L)
            .build());
    detail.setUserSummary(
        UserSummary.builder()
            .id(5L)
            .fullName("Ana Gestora")
            .username("agestora")
            .email("ana@example.com")
            .build());
    detail.setVehicleSummary(
        VehicleSummary.builder()
            .id(7L)
            .type("CAR")
            .brand("Kia")
            .line("Rio")
            .model("Rio")
            .plate("ABC123")
            .status("Activo")
            .build());
    return detail;
  }
}
