package com.example.demo.services.vente;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.xhtmlrenderer.pdf.ITextRenderer;

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

    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("Vente");

    // Header row
    Row header = sheet.createRow(0);
    header.createCell(0).setCellValue("ID");
    header.createCell(1).setCellValue("Date vente");
    header.createCell(2).setCellValue("Quantité lait");
    header.createCell(3).setCellValue("Prix unitaire");
    header.createCell(4).setCellValue("Montant total");
    header.createCell(5).setCellValue("Créé le");
    header.createCell(6).setCellValue("Créé par");

    // Data row
    Row data = sheet.createRow(1);
    data.createCell(0).setCellValue(vente.getId());
    data.createCell(1).setCellValue(
            vente.getDateVente().toString()
    );
    data.createCell(2).setCellValue(
            vente.getQuantiteLait().doubleValue()
    );
    data.createCell(3).setCellValue(
            vente.getPrixUnitaire().doubleValue()
    );

    data.createCell(4).setCellValue(
            vente.getQuantiteLait()
                 .multiply(vente.getPrixUnitaire())
                 .doubleValue()
    );

    data.createCell(5).setCellValue(
            vente.getCreatedAt().toString()
    );

    if (vente.getCreatedBy() != null) {
        data.createCell(6).setCellValue(
                vente.getCreatedBy().getNom()
        );
    }

    // Resize columns automatically
    for (int i = 0; i <= 6; i++) {
        sheet.autoSizeColumn(i);
    }

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    workbook.write(out);

    workbook.close();

    return new ByteArrayInputStream(out.toByteArray());
}

}
