package com.sgivu.purchasesale.controller;

import com.sgivu.purchasesale.dto.PurchaseSaleDetailResponse;
import com.sgivu.purchasesale.dto.PurchaseSaleFilterCriteria;
import com.sgivu.purchasesale.dto.PurchaseSaleRequest;
import com.sgivu.purchasesale.dto.PurchaseSaleResponse;
import com.sgivu.purchasesale.entity.PurchaseSale;
import com.sgivu.purchasesale.mapper.PurchaseSaleMapper;
import com.sgivu.purchasesale.service.PurchaseSaleDetailService;
import com.sgivu.purchasesale.service.PurchaseSaleReportService;
import com.sgivu.purchasesale.service.PurchaseSaleService;
import com.sgivu.purchasesale.enums.ContractStatus;
import com.sgivu.purchasesale.enums.ContractType;
import com.sgivu.purchasesale.enums.PaymentMethod;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/purchase-sales")
public class PurchaseSaleController {

  private final PurchaseSaleService purchaseSaleService;
  private final PurchaseSaleMapper purchaseSaleMapper;
  private final PurchaseSaleReportService purchaseSaleReportService;
  private final PurchaseSaleDetailService purchaseSaleDetailService;

  public PurchaseSaleController(
      PurchaseSaleService purchaseSaleService,
      PurchaseSaleMapper purchaseSaleMapper,
      PurchaseSaleReportService purchaseSaleReportService,
      PurchaseSaleDetailService purchaseSaleDetailService) {
    this.purchaseSaleService = purchaseSaleService;
    this.purchaseSaleMapper = purchaseSaleMapper;
    this.purchaseSaleReportService = purchaseSaleReportService;
    this.purchaseSaleDetailService = purchaseSaleDetailService;
  }

  @PostMapping
  @PreAuthorize("hasAuthority('purchase_sale:create')")
  public ResponseEntity<PurchaseSaleResponse> create(
      @Valid @RequestBody PurchaseSaleRequest purchaseSaleRequest) {
    PurchaseSaleResponse response =
        purchaseSaleMapper.toPurchaseSaleResponse(purchaseSaleService.create(purchaseSaleRequest));
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
    List<PurchaseSaleResponse> responses =
        purchaseSaleService.findAll().stream()
            .map(purchaseSaleMapper::toPurchaseSaleResponse)
            .toList();
    return ResponseEntity.ok(responses);
  }

  @GetMapping("/detailed")
  @PreAuthorize("hasAuthority('purchase_sale:read')")
  public ResponseEntity<List<PurchaseSaleDetailResponse>> getAllDetailed() {
    return ResponseEntity.ok(
        purchaseSaleDetailService.toDetails(purchaseSaleService.findAll()));
  }

  @GetMapping("/page/{page}")
  @PreAuthorize("hasAuthority('purchase_sale:read')")
  public ResponseEntity<Page<PurchaseSaleResponse>> getByPage(@PathVariable Integer page) {
    Page<PurchaseSaleResponse> pagedResponse =
        purchaseSaleService
            .findAll(PageRequest.of(page, 10))
            .map(purchaseSaleMapper::toPurchaseSaleResponse);
    return ResponseEntity.ok(pagedResponse);
  }

  @GetMapping("/page/{page}/detailed")
  @PreAuthorize("hasAuthority('purchase_sale:read')")
  public ResponseEntity<Page<PurchaseSaleDetailResponse>> getDetailedPage(
      @PathVariable Integer page) {
    var pageable = PageRequest.of(page, 10);
    var pagedContracts = purchaseSaleService.findAll(pageable);
    return ResponseEntity.ok(toDetailPage(pagedContracts));
  }

  @GetMapping("/search")
  @PreAuthorize("hasAuthority('purchase_sale:read')")
  public ResponseEntity<Page<PurchaseSaleDetailResponse>> searchContracts(
      @RequestParam(defaultValue = "0") Integer page,
      @RequestParam(defaultValue = "10") Integer size,
      @RequestParam(required = false) ContractType contractType,
      @RequestParam(required = false) ContractStatus contractStatus,
      @RequestParam(required = false) Long clientId,
      @RequestParam(required = false) Long userId,
      @RequestParam(required = false) Long vehicleId,
      @RequestParam(required = false) PaymentMethod paymentMethod,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate endDate,
      @RequestParam(required = false) Double minPurchasePrice,
      @RequestParam(required = false) Double maxPurchasePrice,
      @RequestParam(required = false) Double minSalePrice,
      @RequestParam(required = false) Double maxSalePrice,
      @RequestParam(required = false) String term) {

    var pageable = PageRequest.of(page, size);
    var criteria =
        PurchaseSaleFilterCriteria.builder()
            .contractType(contractType)
            .contractStatus(contractStatus)
            .clientId(clientId)
            .userId(userId)
            .vehicleId(vehicleId)
            .paymentMethod(paymentMethod)
            .startDate(startDate)
            .endDate(endDate)
            .minPurchasePrice(minPurchasePrice)
            .maxPurchasePrice(maxPurchasePrice)
            .minSalePrice(minSalePrice)
            .maxSalePrice(maxSalePrice)
            .term(trimToNull(term))
            .build();

    var filteredContracts = purchaseSaleService.search(criteria, pageable);
    return ResponseEntity.ok(toDetailPage(filteredContracts));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAuthority('purchase_sale:update')")
  public ResponseEntity<PurchaseSaleResponse> update(
      @PathVariable Long id, @Valid @RequestBody PurchaseSaleRequest purchaseSaleRequest) {
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
    return purchaseSaleService
        .findById(id)
        .map(
            purchaseSale -> {
              purchaseSaleService.deleteById(id);
              return ResponseEntity.noContent().<Void>build();
            })
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/client/{clientId}")
  @PreAuthorize("hasAuthority('purchase_sale:read')")
  public ResponseEntity<List<PurchaseSaleResponse>> getByClientId(@PathVariable Long clientId) {
    List<PurchaseSaleResponse> responses =
        purchaseSaleService.findByClientId(clientId).stream()
            .map(purchaseSaleMapper::toPurchaseSaleResponse)
            .toList();
    return ResponseEntity.ok(responses);
  }

  @GetMapping("/user/{userId}")
  @PreAuthorize("hasAuthority('purchase_sale:read')")
  public ResponseEntity<List<PurchaseSaleResponse>> getByUserId(@PathVariable Long userId) {
    List<PurchaseSaleResponse> responses =
        purchaseSaleService.findByUserId(userId).stream()
            .map(purchaseSaleMapper::toPurchaseSaleResponse)
            .toList();
    return ResponseEntity.ok(responses);
  }

  @GetMapping("/vehicle/{vehicleId}")
  @PreAuthorize("hasAuthority('purchase_sale:read')")
  public ResponseEntity<List<PurchaseSaleResponse>> getByVehicleId(@PathVariable Long vehicleId) {
    List<PurchaseSaleResponse> responses =
        purchaseSaleService.findByVehicleId(vehicleId).stream()
            .map(purchaseSaleMapper::toPurchaseSaleResponse)
            .toList();
    return ResponseEntity.ok(responses);
  }

  @GetMapping(value = "/report/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
  @PreAuthorize("hasAuthority('purchase_sale:read')")
  public ResponseEntity<byte[]> exportPdfReport(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate endDate) {
    byte[] report = purchaseSaleReportService.generatePdf(startDate, endDate);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, buildContentDisposition("pdf"))
        .contentType(MediaType.APPLICATION_PDF)
        .body(report);
  }

  @GetMapping(
      value = "/report/excel",
      produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
  @PreAuthorize("hasAuthority('purchase_sale:read')")
  public ResponseEntity<byte[]> exportExcelReport(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate endDate) {
    byte[] report = purchaseSaleReportService.generateExcel(startDate, endDate);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, buildContentDisposition("xlsx"))
        .contentType(
            MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        .body(report);
  }

  @GetMapping(value = "/report/csv", produces = "text/csv")
  @PreAuthorize("hasAuthority('purchase_sale:read')")
  public ResponseEntity<byte[]> exportCsvReport(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate endDate) {
    byte[] report = purchaseSaleReportService.generateCsv(startDate, endDate);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, buildContentDisposition("csv"))
        .contentType(MediaType.parseMediaType("text/csv"))
        .body(report);
  }

  private String buildContentDisposition(String extension) {
    String timestamp = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
    return "attachment; filename=\"reporte-compras-ventas-" + timestamp + "." + extension + "\"";
  }

  private Page<PurchaseSaleDetailResponse> toDetailPage(Page<PurchaseSale> contracts) {
    var detailed = purchaseSaleDetailService.toDetails(contracts.getContent());
    return new PageImpl<>(detailed, contracts.getPageable(), contracts.getTotalElements());
  }

  private String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    String normalized = value.trim();
    return normalized.isEmpty() ? null : normalized;
  }
}
