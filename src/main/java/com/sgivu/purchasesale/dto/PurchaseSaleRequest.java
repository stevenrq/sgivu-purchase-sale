package com.sgivu.purchasesale.dto;

import com.sgivu.purchasesale.enums.ContractStatus;
import com.sgivu.purchasesale.enums.ContractType;
import com.sgivu.purchasesale.enums.PaymentMethod;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PurchaseSaleRequest {

  private Long clientId;

  private Long userId;

  private Long vehicleId;

  @NotNull
  @PositiveOrZero
  @Column(name = "purchase_price")
  private Double purchasePrice;

  @NotNull
  @PositiveOrZero
  @Column(name = "sale_price")
  private Double salePrice;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "contract_type", nullable = false)
  private ContractType contractType;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "contract_status", nullable = false)
  private ContractStatus contractStatus;

  @NotBlank
  @Column(name = "payment_limitations", nullable = false, length = 200)
  private String paymentLimitations;

  @NotBlank
  @Column(name = "payment_terms", nullable = false, length = 200)
  private String paymentTerms;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "payment_method", nullable = false)
  private PaymentMethod paymentMethod;

  @Column(name = "observations", length = 500)
  private String observations;
}
