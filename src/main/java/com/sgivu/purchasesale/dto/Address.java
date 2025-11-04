package com.sgivu.purchasesale.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {
  private Long id;
  private String street;
  private String number;
  private String city;
}
