package com.sgivu.purchasesale.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserSummary {
  Long id;
  String fullName;
  String email;
  String username;
}
