package com.example.demo.services.vente;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.example.demo.entity.auth.Utilisateur;
import com.example.demo.entity.vente.Vente;

@Service
public class VentePdfService {
    @Autowired
    private SpringTemplateEngine templateEngine;

    public byte[] generatePdf(Vente vente) throws Exception {
        Context context = new Context();
        context.setVariable("vente", vente);

        String html = templateEngine.process("vente/vente-pdf", context);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();

            String baseUrl = new java.io.File("src/main/resources/static/").toURI().toURL().toString();

            renderer.setDocumentFromString(html, baseUrl);
            renderer.layout();
            renderer.createPDF(outputStream);

            return outputStream.toByteArray();
        }
    }

    public ByteArrayInputStream export(Vente vente) throws IOException {

    try (Workbook workbook = new XSSFWorkbook()) {
        Sheet sheet = workbook.createSheet("Vente");

        
        Row header = sheet.createRow(0);
        // header.createCell(0).setCellValue("ID");
        header.createCell(0).setCellValue("Date vente");
        header.createCell(1).setCellValue("Quantité lait");
        header.createCell(2).setCellValue("Prix unitaire");
        header.createCell(3).setCellValue("Montant total");
        header.createCell(4).setCellValue("Créé le");
        header.createCell(5).setCellValue("Créé par");

        
        Row data = sheet.createRow(1);
        
        data.createCell(0).setCellValue(
                vente.getDateVente().toString()
        );
        data.createCell(1).setCellValue(
                vente.getQuantiteLait().doubleValue()
        );
        data.createCell(2).setCellValue(
                vente.getPrixUnitaire().doubleValue()
        );

        data.createCell(3).setCellValue(
                vente.getQuantiteLait()
                     .multiply(vente.getPrixUnitaire())
                     .doubleValue()
        );

        data.createCell(4).setCellValue(
                vente.getCreatedAt().toString()
        );

        if (vente.getCreatedBy() != null) {
            data.createCell(5).setCellValue(
                    vente.getCreatedBy().getNom()
            );
        }

        for (int i = 0; i <= 5; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);

        return new ByteArrayInputStream(out.toByteArray());
    }
}

public List<Vente> importExcel(InputStream inputStream) throws IOException {
        List<Vente> ventes = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
                Sheet sheet = workbook.getSheetAt(0);
                Iterator<Row> rows = sheet.iterator();

                if (rows.hasNext()) {
                        rows.next();
                }

                while (rows.hasNext()) {
                Row row = rows.next();

                if (isRowEmpty(row)) {
                        continue;
                }

                Vente vente = new Vente();

                vente.setDateVente(parseLocalDateCell(row.getCell(0)));
                vente.setQuantiteLait(parseBigDecimalCell(row.getCell(1)));
                vente.setPrixUnitaire(parseBigDecimalCell(row.getCell(2)));

                Cell createdAtCell = row.getCell(4);
                if (createdAtCell != null) {
                        String createdAtValue = getCellText(createdAtCell);
                        if (!createdAtValue.isBlank()) {
                                vente.setCreatedAt(parseLocalDateTimeValue(createdAtValue));
                        }
                }

                Cell createdByCell = row.getCell(5);
                if (createdByCell != null) {
                        String createdByValue = getCellText(createdByCell);
                        if (!createdByValue.isBlank()) {
                                Utilisateur utilisateur = new Utilisateur();
                                utilisateur.setNom(createdByValue);
                                vente.setCreatedBy(utilisateur);
                        }
                }

                ventes.add(vente);
                }
        }

        return ventes;
}

private boolean isRowEmpty(Row row) {
        for (int i = 0; i <= 5; i++) {
                Cell cell = row.getCell(i);
                if (cell != null && !getCellText(cell).isBlank()) {
                        return false;
                }
        }
        return true;
}

private String getCellText(Cell cell) {
        if (cell == null) {
                return "";
        }

        if (cell.getCellType() == CellType.STRING) {
                return cell.getStringCellValue().trim();
        }

        if (cell.getCellType() == CellType.NUMERIC) {
                double numericValue = cell.getNumericCellValue();
                if (numericValue == Math.rint(numericValue)) {
                        return Long.toString((long) numericValue);
                }
                return BigDecimal.valueOf(numericValue).stripTrailingZeros().toPlainString();
        }

        if (cell.getCellType() == CellType.BOOLEAN) {
                return Boolean.toString(cell.getBooleanCellValue());
        }

        return cell.toString().trim();
}

private BigDecimal parseBigDecimalCell(Cell cell) {
        String value = getCellText(cell);
        if (value.isBlank()) {
                return null;
        }
        return new BigDecimal(value);
}

private LocalDate parseLocalDateCell(Cell cell) {
        String value = getCellText(cell);
        if (value.isBlank()) {
                return null;
        }

        try {
                return LocalDate.parse(value);
        } catch (DateTimeParseException exception) {
                return cell.getLocalDateTimeCellValue().toLocalDate();
        }
}

private LocalDateTime parseLocalDateTimeValue(String value) {
        try {
                return LocalDateTime.parse(value);
        } catch (DateTimeParseException exception) {
                return LocalDate.parse(value).atStartOfDay();
        }
}

}
