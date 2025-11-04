package com.sgivu.purchasesale.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode
public class Client {
  private Long id;
  private Address address;
  private Long phoneNumber;
  private String email;
  private boolean isEnabled;
}
