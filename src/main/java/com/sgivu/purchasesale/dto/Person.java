package com.sgivu.purchasesale.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Person extends Client {
  private Long nationalId;
  private String firstName;
  private String lastName;
}
