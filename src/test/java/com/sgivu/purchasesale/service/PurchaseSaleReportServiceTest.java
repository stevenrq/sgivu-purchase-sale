package com.sgivu.purchasesale.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

@ExtendWith(MockitoExtension.class)
class PurchaseSaleReportServiceTest {

  @Mock private PurchaseSaleRepository purchaseSaleRepository;

  private PurchaseSaleReportService purchaseSaleReportService;

  @BeforeEach
  void setUp() {
    purchaseSaleReportService = new PurchaseSaleReportService(purchaseSaleRepository);
  }

  @Test
  @DisplayName("generatePdf debe producir un arreglo de bytes no vacío")
  void generatePdf_ShouldReturnDocumentBytes() {
    when(purchaseSaleRepository.findAll(any(Sort.class))).thenReturn(List.of(sampleContract()));

    byte[] pdf = purchaseSaleReportService.generatePdf(null, null);

    assertThat(pdf).isNotEmpty();
    verify(purchaseSaleRepository).findAll(any(Sort.class));
  }

  @Test
  @DisplayName("generateExcel debe producir un arreglo de bytes no vacío")
  void generateExcel_ShouldReturnDocumentBytes() {
    when(purchaseSaleRepository.findAll(any(Sort.class))).thenReturn(List.of(sampleContract()));

    byte[] excel = purchaseSaleReportService.generateExcel(null, null);

    assertThat(excel).isNotEmpty();
    verify(purchaseSaleRepository).findAll(any(Sort.class));
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
}
