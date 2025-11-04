package com.sgivu.purchasesale.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Company extends Client {
  private String taxId;
  private String companyName;
}
