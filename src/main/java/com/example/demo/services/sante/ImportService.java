package com.example.demo.services.sante;

import com.example.demo.entity.sante.HistoriqueVaccin;
import com.example.demo.entity.sante.ProtocoleVaccin;
import com.example.demo.entity.cheptel.Vache;
import com.example.demo.repository.sante.HistoriqueVaccinRepository;
import com.example.demo.repository.sante.ProtocoleVaccinRepository;
import com.example.demo.repository.cheptel.VacheRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ImportService {
    private final VacheRepository vacheRepo;
    private final ProtocoleVaccinRepository protocoleRepo;
    private final HistoriqueVaccinRepository historiqueRepo;

    public ImportService(VacheRepository vacheRepo,
                              ProtocoleVaccinRepository protocoleRepo,
                              HistoriqueVaccinRepository historiqueRepo) {
        this.vacheRepo = vacheRepo;
        this.protocoleRepo = protocoleRepo;
        this.historiqueRepo = historiqueRepo;
    }

    @Transactional(rollbackFor = Exception.class)
    public void importVaccins(MultipartFile file) throws IOException {
        List<HistoriqueVaccin> listeAEnregistrer = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                final int lineNumber = i + 1;

                String numBoucle = getCellValueAsString(row.getCell(0));
                String nomVaccin = getCellValueAsString(row.getCell(1));
                LocalDate dateVaccin = getCellValueAsDate(row.getCell(2));
                String typeInjection = getCellValueAsString(row.getCell(3));

                if (numBoucle.isEmpty() || nomVaccin.isEmpty() || dateVaccin == null) {
                    throw new RuntimeException("Ligne " + lineNumber + " : Données obligatoires manquantes (Boucle, Vaccin ou Date).");
                }

                Vache vache = vacheRepo.findByNumeroBoucle(numBoucle)
                        .orElseThrow(() -> new RuntimeException("Ligne " + lineNumber + " : Vache introuvable avec la boucle " + numBoucle));

                ProtocoleVaccin protocole = protocoleRepo.findByNomVaccin(nomVaccin)
                        .orElseThrow(() -> new RuntimeException("Ligne " + lineNumber + " : Vaccin '" + nomVaccin + "' non répertorié dans le système"));

                HistoriqueVaccin h = new HistoriqueVaccin();
                h.setVache(vache);
                h.setProtocoleVaccin(protocole);
                h.setDateVaccination(dateVaccin);
                h.setTypeInjection(typeInjection.isEmpty() ? "INTRAMUSCULAIRE" : typeInjection);

                listeAEnregistrer.add(h);
            }

            historiqueRepo.saveAll(listeAEnregistrer);
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue().trim();
            case NUMERIC: return String.valueOf((long) cell.getNumericCellValue());
            default: return "";
        }
    }

    private LocalDate getCellValueAsDate(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            Date date = cell.getDateCellValue();
            return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        } else if (cell.getCellType() == CellType.STRING) {
            try {
                return LocalDate.parse(cell.getStringCellValue().trim());
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}
