package com.example.demo.services.vente;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.ByteArrayOutputStream;

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
}
