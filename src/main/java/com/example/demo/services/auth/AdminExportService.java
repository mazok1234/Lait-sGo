package com.example.demo.services.auth;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;

@Service
public class AdminExportService {

    public ByteArrayInputStream exportStatistiquesExcel(Map<String, Object> data) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet kpiSheet = workbook.createSheet("KPIs");
            writeKpiRows(kpiSheet, data);

            Sheet monthlySheet = workbook.createSheet("Rentabilite");
            writeRentabiliteRows(monthlySheet, getList(data, "rentabiliteStats"));

            autoSize(kpiSheet, 2);
            autoSize(monthlySheet, 6);

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    public ByteArrayInputStream exportRapportsExcel(Map<String, Object> data) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet kpiSheet = workbook.createSheet("Synthese");
            writeKpiRows(kpiSheet, data);

            Sheet monthlySheet = workbook.createSheet("RentabiliteMensuelle");
            writeRentabiliteRows(monthlySheet, getList(data, "rentabiliteStats"));

            autoSize(kpiSheet, 2);
            autoSize(monthlySheet, 6);

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    public byte[] exportStatistiquesPdf(Map<String, Object> data) throws Exception {
        String html = buildHtml("Statistiques Admin", data);
        return renderPdf(html);
    }

    public byte[] exportRapportsPdf(Map<String, Object> data) throws Exception {
        String html = buildHtml("Rapports Admin", data);
        return renderPdf(html);
    }

    public byte[] exportStatistiquesImage(Map<String, Object> data) throws IOException {
        return buildSummaryImage("Statistiques Admin", data);
    }

    public byte[] exportRapportsImage(Map<String, Object> data) throws IOException {
        return buildSummaryImage("Rapports Admin", data);
    }

    public byte[] exportStatistiquesCsv(Map<String, Object> data) {
        return buildCsv(data).getBytes(StandardCharsets.UTF_8);
    }

    public byte[] exportRapportsCsv(Map<String, Object> data) {
        return buildCsv(data).getBytes(StandardCharsets.UTF_8);
    }

    private byte[] renderPdf(String html) throws Exception {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(outputStream);
            return outputStream.toByteArray();
        }
    }

    private String buildCsv(Map<String, Object> data) {
        StringBuilder sb = new StringBuilder();
        sb.append("section;indicateur;valeur\n");
        sb.append("kpi;total_revenus;").append(formatCsvValue(data.get("totalRevenus"))).append("\n");
        sb.append("kpi;total_depenses;").append(formatCsvValue(data.get("totalDepenses"))).append("\n");
        sb.append("kpi;total_benefice;").append(formatCsvValue(data.get("totalBenefice"))).append("\n");

        if (data.containsKey("margeBeneficePct")) {
            sb.append("kpi;marge_benefice_pct;").append(formatCsvValue(data.get("margeBeneficePct"))).append("\n");
        }

        sb.append("\n");
        sb.append("section;mois;annee;revenus;depenses;depenses_medicaments;benefice\n");

        for (Map<String, Object> row : getList(data, "rentabiliteStats")) {
            sb.append("rentabilite;")
                    .append(safeInt(row.get("month"))).append(";")
                    .append(safeInt(row.get("year"))).append(";")
                    .append(formatCsvValue(row.get("revenus"))).append(";")
                    .append(formatCsvValue(row.get("depenses"))).append(";")
                    .append(formatCsvValue(row.get("depensesMed"))).append(";")
                    .append(formatCsvValue(row.get("benefice"))).append("\n");
        }

        return sb.toString();
    }

    private String buildHtml(String title, Map<String, Object> data) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html xmlns='http://www.w3.org/1999/xhtml'><head>")
            .append("<meta http-equiv='Content-Type' content='text/html; charset=UTF-8' />")
            .append("<style type='text/css'>")
                .append("body{font-family:Arial,sans-serif;color:#1f2937;margin:24px;}")
                .append("h1{color:#0f766e;margin-bottom:8px;}")
                .append("table{width:100%;border-collapse:collapse;margin-top:16px;}")
                .append("th,td{border:1px solid #d1d5db;padding:8px;text-align:left;}")
                .append("th{background:#f3f4f6;}")
                .append(".muted{color:#6b7280;font-size:12px;}")
                .append("</style></head><body>");

        sb.append("<h1>").append(escape(title)).append("</h1>");
        sb.append("<p class='muted'>Export automatique du module admin.</p>");

        sb.append("<table><tbody>")
                .append(row("Total revenus", formatAmount(data.get("totalRevenus"))))
                .append(row("Total depenses", formatAmount(data.get("totalDepenses"))))
                .append(row("Total benefice", formatAmount(data.get("totalBenefice"))))
                .append(row("Marge benefice (%)", formatAmount(data.get("margeBeneficePct"))))
                .append("</tbody></table>");

        List<Map<String, Object>> rentabilite = getList(data, "rentabiliteStats");
        sb.append("<h2>Rentabilite mensuelle</h2>");
        sb.append("<table><thead><tr>")
                .append("<th>Mois</th><th>Annee</th><th>Revenus</th><th>Depenses</th><th>Depenses medicaments</th><th>Benefice</th>")
                .append("</tr></thead><tbody>");

        for (Map<String, Object> row : rentabilite) {
            sb.append("<tr>")
                    .append(td(asString(row.get("month"))))
                    .append(td(asString(row.get("year"))))
                    .append(td(formatAmount(row.get("revenus"))))
                    .append(td(formatAmount(row.get("depenses"))))
                    .append(td(formatAmount(row.get("depensesMed"))))
                    .append(td(formatAmount(row.get("benefice"))))
                    .append("</tr>");
        }

        if (rentabilite.isEmpty()) {
            sb.append("<tr><td colspan='6'>Aucune donnee mensuelle.</td></tr>");
        }

        sb.append("</tbody></table>");
        sb.append("</body></html>");
        return sb.toString();
    }

    private byte[] buildSummaryImage(String title, Map<String, Object> data) throws IOException {
        int width = 1200;
        int height = 700;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        g.setColor(new Color(248, 250, 252));
        g.fillRect(0, 0, width, height);

        g.setColor(new Color(15, 118, 110));
        g.setFont(new Font("SansSerif", Font.BOLD, 38));
        g.drawString(title, 40, 70);

        g.setColor(new Color(31, 41, 55));
        g.setFont(new Font("SansSerif", Font.PLAIN, 24));
        g.drawString("Total revenus : " + formatAmount(data.get("totalRevenus")), 40, 140);
        g.drawString("Total depenses : " + formatAmount(data.get("totalDepenses")), 40, 185);
        g.drawString("Total benefice : " + formatAmount(data.get("totalBenefice")), 40, 230);

        g.setFont(new Font("SansSerif", Font.BOLD, 24));
        g.drawString("Rentabilite mensuelle", 40, 290);

        g.setFont(new Font("Monospaced", Font.PLAIN, 20));
        int y = 335;
        for (Map<String, Object> row : getList(data, "rentabiliteStats")) {
            if (y > height - 30) {
                break;
            }
            String line = String.format(
                    "%02d/%s  Rev:%s  Dep:%s  Med:%s  Ben:%s",
                    safeInt(row.get("month")),
                    asString(row.get("year")),
                    formatAmount(row.get("revenus")),
                    formatAmount(row.get("depenses")),
                    formatAmount(row.get("depensesMed")),
                    formatAmount(row.get("benefice")));
            g.drawString(line, 40, y);
            y += 34;
        }

        g.dispose();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", out);
            return out.toByteArray();
        }
    }

    private void writeKpiRows(Sheet sheet, Map<String, Object> data) {
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Indicateur");
        header.createCell(1).setCellValue("Valeur");

        writeKpiRow(sheet, 1, "Total revenus", data.get("totalRevenus"));
        writeKpiRow(sheet, 2, "Total depenses", data.get("totalDepenses"));
        writeKpiRow(sheet, 3, "Total benefice", data.get("totalBenefice"));

        if (data.containsKey("margeBeneficePct")) {
            writeKpiRow(sheet, 4, "Marge benefice (%)", data.get("margeBeneficePct"));
        }
    }

    private void writeKpiRow(Sheet sheet, int rowNum, String label, Object value) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(toBigDecimal(value).doubleValue());
    }

    private void writeRentabiliteRows(Sheet sheet, List<Map<String, Object>> rows) {
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Mois");
        header.createCell(1).setCellValue("Annee");
        header.createCell(2).setCellValue("Revenus");
        header.createCell(3).setCellValue("Depenses");
        header.createCell(4).setCellValue("Depenses medicaments");
        header.createCell(5).setCellValue("Benefice");

        int idx = 1;
        for (Map<String, Object> rowData : rows) {
            Row row = sheet.createRow(idx++);
            row.createCell(0).setCellValue(safeInt(rowData.get("month")));
            row.createCell(1).setCellValue(safeInt(rowData.get("year")));
            row.createCell(2).setCellValue(toBigDecimal(rowData.get("revenus")).doubleValue());
            row.createCell(3).setCellValue(toBigDecimal(rowData.get("depenses")).doubleValue());
            row.createCell(4).setCellValue(toBigDecimal(rowData.get("depensesMed")).doubleValue());
            row.createCell(5).setCellValue(toBigDecimal(rowData.get("benefice")).doubleValue());
        }
    }

    private void autoSize(Sheet sheet, int columns) {
        for (int i = 0; i < columns; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getList(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value instanceof List<?> list) {
            return (List<Map<String, Object>>) list;
        }
        return List.of();
    }

    private String row(String label, String value) {
        return "<tr><td>" + escape(label) + "</td><td>" + escape(value) + "</td></tr>";
    }

    private String td(String value) {
        return "<td>" + escape(value) + "</td>";
    }

    private String formatAmount(Object value) {
        return toBigDecimal(value).stripTrailingZeros().toPlainString();
    }

    private String formatCsvValue(Object value) {
        return formatAmount(value);
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal bd) {
            return bd;
        }
        if (value instanceof Number n) {
            return BigDecimal.valueOf(n.doubleValue());
        }
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private int safeInt(Object value) {
        if (value instanceof Number n) {
            return n.intValue();
        }
        try {
            return Integer.parseInt(asString(value));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String asString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String escape(String raw) {
        byte[] bytes = raw == null ? new byte[0] : raw.getBytes(StandardCharsets.UTF_8);
        String value = new String(bytes, StandardCharsets.UTF_8);
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
