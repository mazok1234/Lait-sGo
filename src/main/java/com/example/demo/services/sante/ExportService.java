package com.example.demo.services.sante;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.example.demo.entity.sante.EvenementSante;
import com.example.demo.entity.sante.TraitementSante;

@Service
public class ExportService {
    public <T> ByteArrayInputStream exportToExcel(List<T> data, Class<T> clazz, String sheetName) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(sheetName);

            Field[] fields = clazz.getDeclaredFields();

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            headerCellStyle.setFillForegroundColor(IndexedColors.CORNFLOWER_BLUE.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row headerRow = sheet.createRow(0);
            int colIdx = 0;
            for (Field field : fields) {
                if (!List.class.isAssignableFrom(field.getType())) {
                    Cell cell = headerRow.createCell(colIdx++);
                    cell.setCellValue(field.getName().toUpperCase());
                    cell.setCellStyle(headerCellStyle);
                }
            }

            int rowIdx = 1;
            for (T item : data) {
                Row row = sheet.createRow(rowIdx++);
                colIdx = 0;

                for (Field field : fields) {
                    if (!List.class.isAssignableFrom(field.getType())) {
                        field.setAccessible(true);
                        Cell cell = row.createCell(colIdx++);

                        try {
                            Object value = field.get(item);
                            if (value != null) {
                                switch (value) {
                                    case Number number -> cell.setCellValue(number.doubleValue());
                                    case Boolean bool -> cell.setCellValue(bool);
                                    default -> cell.setCellValue(value.toString());
                                }
                            } else {
                                cell.setCellValue("");
                            }
                        } catch (IllegalAccessException e) {
                            cell.setCellValue("Erreur d'accès");
                        }
                    }
                }
            }

            for (int i = 0; i < colIdx; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    public ByteArrayInputStream exportTraitementsToExcel(List<EvenementSante> evenements) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Traitements");

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            headerCellStyle.setFillForegroundColor(IndexedColors.CORNFLOWER_BLUE.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            String[] headers = {
                    "VACHE", "MALADIE", "DATE_EVENEMENT", "DESCRIPTION", "MEDICAMENT", "DOSE", "UNITE",
                    "PRIX_UNITAIRE", "NBR_MEDICAMENT", "DUREE_TRAITEMENT", "DELAI_ATTENTE_J", "DATE_DEBUT",
                    "DATE_FIN"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerCellStyle);
            }

            int rowIdx = 1;
            for (EvenementSante evenement : evenements) {
                if (evenement.getTraitements() == null || evenement.getTraitements().isEmpty()) {
                    Row row = sheet.createRow(rowIdx++);
                    writeEvenementRow(row, evenement, null);
                    continue;
                }

                for (TraitementSante traitement : evenement.getTraitements()) {
                    Row row = sheet.createRow(rowIdx++);
                    writeEvenementRow(row, evenement, traitement);
                }
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    private void writeEvenementRow(Row row, EvenementSante evenement, TraitementSante traitement) {
        int col = 0;
        setCellValue(row.createCell(col++), evenement != null && evenement.getVache() != null
                ? evenement.getVache().getNumeroBoucle() : "");
        setCellValue(row.createCell(col++), evenement != null && evenement.getMaladie() != null
                ? evenement.getMaladie().getNom() : "");
        setCellValue(row.createCell(col++), evenement != null ? evenement.getDateEvenement() : null);
        setCellValue(row.createCell(col++), evenement != null ? evenement.getDescription() : "");
        setCellValue(row.createCell(col++), traitement != null && traitement.getMedicament() != null
                ? traitement.getMedicament().getNom() : "");
        setCellValue(row.createCell(col++), traitement != null ? traitement.getDose() : null);
        setCellValue(row.createCell(col++), traitement != null ? traitement.getUnite() : "");
        setCellValue(row.createCell(col++), traitement != null ? traitement.getPrixUnitaireMedicament() : null);
        setCellValue(row.createCell(col++), traitement != null ? traitement.getNbrMedicament() : null);
        setCellValue(row.createCell(col++), traitement != null ? traitement.getDureeTraitement() : null);
        setCellValue(row.createCell(col++), traitement != null ? traitement.getDelaiAttenteJ() : null);
        setCellValue(row.createCell(col++), traitement != null ? traitement.getDateDebut() : null);
        setCellValue(row.createCell(col++), traitement != null ? traitement.getDateFin() : null);
    }

    private void setCellValue(Cell cell, String value) {
        cell.setCellValue(value != null ? value : "");
    }

    private void setCellValue(Cell cell, Integer value) {
        if (value != null) {
            cell.setCellValue(value.doubleValue());
        } else {
            cell.setCellValue("");
        }
    }

    private void setCellValue(Cell cell, BigDecimal value) {
        if (value != null) {
            cell.setCellValue(value.doubleValue());
        } else {
            cell.setCellValue("");
        }
    }

    private void setCellValue(Cell cell, LocalDate value) {
        cell.setCellValue(value != null ? value.toString() : "");
    }
}
