package com.sgivu.purchasesale.mapper;

import com.sgivu.purchasesale.dto.PurchaseSaleResponse;
import com.sgivu.purchasesale.entity.PurchaseSale;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PurchaseSaleMapper {

  @Mapping(source = "id", target = "id")
  @Mapping(source = "clientId", target = "clientId")
  @Mapping(source = "userId", target = "userId")
  @Mapping(source = "vehicleId", target = "vehicleId")
  @Mapping(source = "purchasePrice", target = "purchasePrice")
  @Mapping(source = "salePrice", target = "salePrice")
  @Mapping(source = "contractType", target = "contractType")
  @Mapping(source = "contractStatus", target = "contractStatus")
  @Mapping(source = "paymentLimitations", target = "paymentLimitations")
  @Mapping(source = "paymentTerms", target = "paymentTerms")
  @Mapping(source = "paymentMethod", target = "paymentMethod")
  @Mapping(source = "observations", target = "observations")
  PurchaseSaleResponse toPurchaseSaleResponse(PurchaseSale purchaseSale);
}
