package com.sgivu.purchasesale.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.sgivu.purchasesale.entity.PurchaseSale;
import com.sgivu.purchasesale.enums.ContractStatus;
import com.sgivu.purchasesale.enums.ContractType;
import com.sgivu.purchasesale.enums.PaymentMethod;
import com.sgivu.purchasesale.repository.PurchaseSaleRepository;
import com.sgivu.purchasesale.dto.ClientSummary;
import com.sgivu.purchasesale.dto.PurchaseSaleDetailResponse;
import com.sgivu.purchasesale.dto.UserSummary;
import com.sgivu.purchasesale.dto.VehicleSummary;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class PurchaseSaleReportService {

  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

  private final PurchaseSaleRepository purchaseSaleRepository;
  private final PurchaseSaleDetailService purchaseSaleDetailService;
  private final Map<ContractStatus, String> statusLabels = new EnumMap<>(ContractStatus.class);
  private final Map<ContractType, String> typeLabels = new EnumMap<>(ContractType.class);
  private final Map<PaymentMethod, String> paymentMethodLabels =
      new EnumMap<>(PaymentMethod.class);

  public PurchaseSaleReportService(
      PurchaseSaleRepository purchaseSaleRepository,
      PurchaseSaleDetailService purchaseSaleDetailService) {
    this.purchaseSaleRepository = purchaseSaleRepository;
    this.purchaseSaleDetailService = purchaseSaleDetailService;
    initialiseLabels();
  }

  public byte[] generatePdf(LocalDate startDate, LocalDate endDate) {
    List<PurchaseSale> contracts = findContracts(startDate, endDate);
    List<PurchaseSaleDetailResponse> details = purchaseSaleDetailService.toDetails(contracts);

    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      Document document = new Document();
      PdfWriter.getInstance(document, outputStream);

      document.open();
      document.addTitle("Reporte de compras y ventas");
      document.addAuthor("SGIVU");

      Paragraph title =
          new Paragraph(
              "Reporte de compras y ventas de vehículos", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16));
      title.setAlignment(Element.ALIGN_CENTER);
      title.setSpacingAfter(10f);
      document.add(title);

      Paragraph period = new Paragraph(buildPeriodText(startDate, endDate));
      period.setAlignment(Element.ALIGN_CENTER);
      period.setSpacingAfter(20f);
      document.add(period);

      PdfPTable table = buildPdfTable(details);
      document.add(table);

      document.close();
      return outputStream.toByteArray();
    } catch (Exception ex) {
      throw new IllegalStateException("Error al generar el reporte en PDF", ex);
    }
  }

  public byte[] generateExcel(LocalDate startDate, LocalDate endDate) {
    List<PurchaseSale> contracts = findContracts(startDate, endDate);
    List<PurchaseSaleDetailResponse> details = purchaseSaleDetailService.toDetails(contracts);

    try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream =
        new ByteArrayOutputStream()) {
      Sheet sheet = workbook.createSheet("Compras y ventas");

      Font headerFont = workbook.createFont();
      headerFont.setBold(true);
      CellStyle headerStyle = workbook.createCellStyle();
      headerStyle.setFont(headerFont);
      headerStyle.setAlignment(HorizontalAlignment.CENTER);

      Font periodFont = workbook.createFont();
      periodFont.setBold(true);
      CellStyle periodStyle = workbook.createCellStyle();
      periodStyle.setFont(periodFont);

      Row periodRow = sheet.createRow(0);
      Cell periodCell = periodRow.createCell(0);
      periodCell.setCellValue(buildPeriodText(startDate, endDate));
      periodCell.setCellStyle(periodStyle);

      String[] headers = {
        "ID",
        "Tipo",
        "Estado",
        "Cliente",
        "Usuario",
        "Vehículo",
        "Precio de compra",
        "Precio de venta",
        "Método de pago",
        "Creado",
        "Actualizado"
      };

      Row headerRow = sheet.createRow(2);
      for (int i = 0; i < headers.length; i++) {
        Cell cell = headerRow.createCell(i);
        cell.setCellValue(headers[i]);
        cell.setCellStyle(headerStyle);
      }

      int rowIdx = 3;
      for (PurchaseSaleDetailResponse contract : details) {
        Row row = sheet.createRow(rowIdx++);
        row.createCell(0).setCellValue(contract.getId());
        row.createCell(1).setCellValue(getContractTypeLabel(contract.getContractType()));
        row.createCell(2).setCellValue(getStatusLabel(contract.getContractStatus()));
        row.createCell(3).setCellValue(formatClient(contract.getClientSummary()));
        row.createCell(4).setCellValue(formatUser(contract.getUserSummary()));
        row.createCell(5).setCellValue(formatVehicle(contract.getVehicleSummary()));
        row.createCell(6).setCellValue(contract.getPurchasePrice());
        row.createCell(7).setCellValue(contract.getSalePrice());
        row.createCell(8).setCellValue(getPaymentMethodLabel(contract.getPaymentMethod()));
        row.createCell(9).setCellValue(formatDate(contract.getCreatedAt()));
        row.createCell(10).setCellValue(formatDate(contract.getUpdatedAt()));
      }

      autoSizeColumns(sheet, headers.length);

      workbook.write(outputStream);
      return outputStream.toByteArray();
    } catch (Exception ex) {
      throw new IllegalStateException("Error al generar el reporte en Excel", ex);
    }
  }

  private List<PurchaseSale> findContracts(LocalDate startDate, LocalDate endDate) {
    return purchaseSaleRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
        .filter(contract -> filterByDateRange(contract.getCreatedAt(), startDate, endDate))
        .collect(Collectors.toList());
  }

  private boolean filterByDateRange(
      LocalDateTime value, LocalDate startDate, LocalDate endDate) {
    if (value == null) {
      return false;
    }
    LocalDate date = value.toLocalDate();
    boolean afterStart = startDate == null || !date.isBefore(startDate);
    boolean beforeEnd = endDate == null || !date.isAfter(endDate);
    return afterStart && beforeEnd;
  }

  private PdfPTable buildPdfTable(List<PurchaseSaleDetailResponse> contracts) {
    float[] columnWidths = {1.2f, 1.5f, 1.5f, 1.5f, 1.5f, 1.5f, 1.6f, 1.6f, 1.8f, 1.6f, 1.6f};
    PdfPTable table = new PdfPTable(columnWidths);
    table.setWidthPercentage(100);

    String[] headers = {
      "ID",
      "Tipo",
      "Estado",
      "Cliente",
      "Usuario",
      "Vehículo",
      "Precio compra",
      "Precio venta",
      "Método pago",
      "Creado",
      "Actualizado"
    };

    for (String header : headers) {
      PdfPCell headerCell =
          new PdfPCell(new Phrase(header, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
      headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
      headerCell.setPadding(6f);
      headerCell.setBackgroundColor(new java.awt.Color(242, 242, 242));
      headerCell.setBorder(Rectangle.BOX);
      table.addCell(headerCell);
    }

    for (PurchaseSaleDetailResponse contract : contracts) {
      addCell(table, contract.getId());
      addCell(table, getContractTypeLabel(contract.getContractType()));
      addCell(table, getStatusLabel(contract.getContractStatus()));
      addCell(table, formatClient(contract.getClientSummary()));
      addCell(table, formatUser(contract.getUserSummary()));
      addCell(table, formatVehicle(contract.getVehicleSummary()));
      addCell(table, contract.getPurchasePrice());
      addCell(table, contract.getSalePrice());
      addCell(table, getPaymentMethodLabel(contract.getPaymentMethod()));
      addCell(table, formatDate(contract.getCreatedAt()));
      addCell(table, formatDate(contract.getUpdatedAt()));
    }

    if (contracts.isEmpty()) {
      PdfPCell emptyCell =
          new PdfPCell(new Phrase("No existen registros para el periodo seleccionado."));
      emptyCell.setColspan(headers.length);
      emptyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
      emptyCell.setPadding(12f);
      table.addCell(emptyCell);
    }

    return table;
  }

  private void addCell(PdfPTable table, Object value) {
    PdfPCell cell = new PdfPCell(new Phrase(value != null ? value.toString() : ""));
    cell.setPadding(5f);
    table.addCell(cell);
  }

  private String formatDate(LocalDateTime dateTime) {
    return dateTime == null ? "" : dateTime.format(DATE_TIME_FORMATTER);
  }

  private String buildPeriodText(LocalDate startDate, LocalDate endDate) {
    if (startDate == null && endDate == null) {
      return "Periodo: todos los registros disponibles";
    }

    String start = startDate != null ? startDate.format(DateTimeFormatter.ISO_DATE) : "...";
    String end = endDate != null ? endDate.format(DateTimeFormatter.ISO_DATE) : "...";
    return "Periodo: " + start + " - " + end;
  }

  private String formatClient(ClientSummary clientSummary) {
    if (clientSummary == null) {
      return "N/D";
    }

    StringBuilder builder = new StringBuilder();
    builder.append(clientSummary.getName() != null ? clientSummary.getName() : "Cliente");
    builder.append(" (ID ").append(clientSummary.getId()).append(")");
    if (clientSummary.getIdentifier() != null) {
      builder.append(" - ").append(clientSummary.getIdentifier());
    }
    return builder.toString();
  }

  private String formatUser(UserSummary userSummary) {
    if (userSummary == null) {
      return "N/D";
    }
    String name = userSummary.getFullName() != null ? userSummary.getFullName() : "Usuario";
    String username = userSummary.getUsername() != null ? userSummary.getUsername() : "N/D";
    return String.format("%s (@%s)", name, username);
  }

  private String formatVehicle(VehicleSummary vehicleSummary) {
    if (vehicleSummary == null) {
      return "N/D";
    }

    String plate = vehicleSummary.getPlate() != null ? vehicleSummary.getPlate() : "N/D";
    return String.format(
        "%s %s - %s",
        vehicleSummary.getBrand() != null ? vehicleSummary.getBrand() : "Vehículo",
        vehicleSummary.getModel() != null ? vehicleSummary.getModel() : "N/D",
        plate);
  }

  private void initialiseLabels() {
    statusLabels.put(ContractStatus.PENDING, "Pendiente");
    statusLabels.put(ContractStatus.ACTIVE, "Activa");
    statusLabels.put(ContractStatus.COMPLETED, "Completada");
    statusLabels.put(ContractStatus.CANCELED, "Cancelada");

    typeLabels.put(ContractType.PURCHASE, "Compra");
    typeLabels.put(ContractType.SALE, "Venta");

    paymentMethodLabels.put(PaymentMethod.CASH, "Efectivo");
    paymentMethodLabels.put(PaymentMethod.BANK_TRANSFER, "Transferencia bancaria");
    paymentMethodLabels.put(PaymentMethod.BANK_DEPOSIT, "Consignación bancaria");
    paymentMethodLabels.put(PaymentMethod.CASHIERS_CHECK, "Cheque de gerencia");
    paymentMethodLabels.put(PaymentMethod.MIXED, "Pago combinado");
    paymentMethodLabels.put(PaymentMethod.FINANCING, "Financiación");
    paymentMethodLabels.put(PaymentMethod.DIGITAL_WALLET, "Billetera digital");
    paymentMethodLabels.put(PaymentMethod.TRADE_IN, "Permuta");
    paymentMethodLabels.put(PaymentMethod.INSTALLMENT_PAYMENT, "Pago a plazos");
  }

  private String getStatusLabel(ContractStatus status) {
    return status == null ? "" : statusLabels.getOrDefault(status, status.name());
  }

  private String getContractTypeLabel(ContractType contractType) {
    return contractType == null ? "" : typeLabels.getOrDefault(contractType, contractType.name());
  }

  private String getPaymentMethodLabel(PaymentMethod paymentMethod) {
    return paymentMethod == null
        ? ""
        : paymentMethodLabels.getOrDefault(paymentMethod, paymentMethod.name());
  }

  private void autoSizeColumns(Sheet sheet, int columnCount) {
    for (int i = 0; i < columnCount; i++) {
      try {
        sheet.autoSizeColumn(i);
      } catch (Exception ex) {
        sheet.setColumnWidth(i, 20 * 256);
      }
    }
  }
}
