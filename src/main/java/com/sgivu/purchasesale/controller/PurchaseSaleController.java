package com.sgivu.purchasesale.controller;

import com.sgivu.purchasesale.dto.PurchaseSaleRequest;
import com.sgivu.purchasesale.dto.PurchaseSaleResponse;
import com.sgivu.purchasesale.entity.PurchaseSale;
import com.sgivu.purchasesale.mapper.PurchaseSaleMapper;
import com.sgivu.purchasesale.service.PurchaseSaleService;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/purchase-sales")
public class PurchaseSaleController {

  private final PurchaseSaleService purchaseSaleService;
  private final PurchaseSaleMapper purchaseSaleMapper;

  public PurchaseSaleController(
      PurchaseSaleService purchaseSaleService, PurchaseSaleMapper purchaseSaleMapper) {
    this.purchaseSaleService = purchaseSaleService;
    this.purchaseSaleMapper = purchaseSaleMapper;
  }

  @PostMapping
  @PreAuthorize("hasAuthority('purchase_sale:create')")
  public ResponseEntity<PurchaseSale> create(
      @RequestBody PurchaseSale purchaseSale, BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      return ResponseEntity.badRequest().body(null);
    }
    return ResponseEntity.status(HttpStatus.CREATED).body(purchaseSaleService.save(purchaseSale));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAuthority('purchase_sale:read')")
  public ResponseEntity<PurchaseSaleResponse> getById(@PathVariable Long id) {
    return purchaseSaleService
        .findById(id)
        .map(
            purchaseSale ->
                ResponseEntity.ok(purchaseSaleMapper.toPurchaseSaleResponse(purchaseSale)))
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping
  @PreAuthorize("hasAuthority('purchase_sale:read')")
  public ResponseEntity<List<PurchaseSaleResponse>> getAll() {
    return ResponseEntity.ok(
        purchaseSaleService.findAll().stream()
            .map(purchaseSaleMapper::toPurchaseSaleResponse)
            .toList());
  }

  @GetMapping("/page/{page}")
  public ResponseEntity<List<PurchaseSaleResponse>> getByPage(@PathVariable Integer page) {
    return ResponseEntity.ok(
        purchaseSaleService
            .findAll(PageRequest.of(page, 10))
            .map(purchaseSaleMapper::toPurchaseSaleResponse)
            .toList());
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAuthority('purchase_sale:update')")
  public ResponseEntity<PurchaseSaleResponse> update(
      @PathVariable Long id,
      @RequestBody PurchaseSaleRequest purchaseSaleRequest,
      BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      return ResponseEntity.badRequest().body(null);
    }
    return purchaseSaleService
        .update(id, purchaseSaleRequest)
        .map(
            purchaseSale ->
                ResponseEntity.ok(purchaseSaleMapper.toPurchaseSaleResponse(purchaseSale)))
        .orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority('purchase_sale:delete')")
  public ResponseEntity<Void> deleteById(@PathVariable Long id) {
    Optional<PurchaseSale> purchaseSaleOptional = purchaseSaleService.findById(id);
    if (purchaseSaleOptional.isPresent()) {
      purchaseSaleService.deleteById(id);

      return ResponseEntity.noContent().build();
    }

    return ResponseEntity.notFound().build();
  }

  @GetMapping("/client/{clientId}")
  @PreAuthorize("hasAuthority('purchase_sale:read')")
  public ResponseEntity<List<PurchaseSaleResponse>> getByClientId(@PathVariable Long clientId) {
    return ResponseEntity.ok(
        purchaseSaleService.findByClientId(clientId).stream()
            .map(purchaseSaleMapper::toPurchaseSaleResponse)
            .toList());
  }

  @GetMapping("/user/{userId}")
  @PreAuthorize("hasAuthority('purchase_sale:read')")
  public ResponseEntity<List<PurchaseSaleResponse>> getByUserId(@PathVariable Long userId) {
    return ResponseEntity.ok(
        purchaseSaleService.findByUserId(userId).stream()
            .map(purchaseSaleMapper::toPurchaseSaleResponse)
            .toList());
  }

  @GetMapping("/vehicle/{vehicleId}")
  @PreAuthorize("hasAuthority('purchase_sale:read')")
  public ResponseEntity<List<PurchaseSaleResponse>> getByVehicleId(@PathVariable Long vehicleId) {
    return ResponseEntity.ok(
        purchaseSaleService.findByVehicleId(vehicleId).stream()
            .map(purchaseSaleMapper::toPurchaseSaleResponse)
            .toList());
  }
}
