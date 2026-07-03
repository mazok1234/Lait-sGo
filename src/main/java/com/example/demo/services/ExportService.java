package com.example.demo.services;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

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
                                if (value instanceof Number) {
                                    cell.setCellValue(((Number) value).doubleValue());
                                } else if (value instanceof Boolean) {
                                    cell.setCellValue((Boolean) value);
                                } else {
                                    cell.setCellValue(value.toString());
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
}