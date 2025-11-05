package com.sgivu.purchasesale.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ClientSummary {
  Long id;
  String type;
  String name;
  String identifier;
  String email;
  Long phoneNumber;
}
