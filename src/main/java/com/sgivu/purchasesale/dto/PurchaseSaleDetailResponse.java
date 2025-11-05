package com.sgivu.purchasesale.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PurchaseSaleDetailResponse extends PurchaseSaleResponse {

  private ClientSummary clientSummary;
  private UserSummary userSummary;
  private VehicleSummary vehicleSummary;
}
