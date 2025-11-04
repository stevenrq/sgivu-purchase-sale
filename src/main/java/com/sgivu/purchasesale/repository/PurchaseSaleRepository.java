package com.sgivu.purchasesale.repository;

import com.sgivu.purchasesale.entity.PurchaseSale;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseSaleRepository extends JpaRepository<PurchaseSale, Long> {

  List<PurchaseSale> findByClientId(Long clientId);

  List<PurchaseSale> findByUserId(Long userId);

  List<PurchaseSale> findByVehicleId(Long vehicleId);
}
