package com.example.demo.services.sante;

import com.example.demo.entity.cheptel.Vache;
import com.example.demo.entity.sante.EvenementSante;
import com.example.demo.entity.sante.HistoriqueVaccin;
import com.example.demo.entity.sante.Maladie;
import com.example.demo.entity.sante.Medicament;
import com.example.demo.entity.sante.MedicamentFille;
import com.example.demo.entity.sante.ProtocoleVaccin;
import com.example.demo.entity.sante.TraitementSante;
import com.example.demo.repository.cheptel.VacheRepository;
import com.example.demo.repository.sante.EvenementSanteRepository;
import com.example.demo.repository.sante.HistoriqueVaccinRepository;
import com.example.demo.repository.sante.MaladieRepository;
import com.example.demo.repository.sante.MedicamentFilleRepository;
import com.example.demo.repository.sante.MedicamentRepository;
import com.example.demo.repository.sante.ProtocoleVaccinRepository;
import com.example.demo.repository.sante.TraitementSanteRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ImportService {
    private final VacheRepository vacheRepo;
    private final ProtocoleVaccinRepository protocoleRepo;
    private final HistoriqueVaccinRepository historiqueRepo;
    private final MaladieRepository maladieRepo;
    private final MedicamentRepository medicamentRepo;
    private final MedicamentFilleRepository medicamentFilleRepo;
    private final EvenementSanteRepository evenementRepo;
    private final TraitementSanteRepository traitementRepo;

    public ImportService(VacheRepository vacheRepo,
                              ProtocoleVaccinRepository protocoleRepo,
                              HistoriqueVaccinRepository historiqueRepo,
                              MaladieRepository maladieRepo,
                              MedicamentRepository medicamentRepo,
                              MedicamentFilleRepository medicamentFilleRepo,
                              EvenementSanteRepository evenementRepo,
                              TraitementSanteRepository traitementRepo) {
        this.vacheRepo = vacheRepo;
        this.protocoleRepo = protocoleRepo;
        this.historiqueRepo = historiqueRepo;
        this.maladieRepo = maladieRepo;
        this.medicamentRepo = medicamentRepo;
        this.medicamentFilleRepo = medicamentFilleRepo;
        this.evenementRepo = evenementRepo;
        this.traitementRepo = traitementRepo;
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

    @Transactional(rollbackFor = Exception.class)
    public void importTraitements(MultipartFile file) throws IOException {
        Map<String, EvenementSante> evenementsParCle = new HashMap<>();

        try (InputStream is = file.getInputStream();
                Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                final int lineNumber = i + 1;

                String numBoucle = getCellValueAsString(row.getCell(0));
                String nomMaladie = getCellValueAsString(row.getCell(1));
                LocalDate dateEvenement = getCellValueAsDate(row.getCell(2));
                String description = getCellValueAsString(row.getCell(3));
                String nomMedicament = getCellValueAsString(row.getCell(4));
                BigDecimal dose = getCellValueAsBigDecimal(row.getCell(5));
                String unite = getCellValueAsString(row.getCell(6));
                BigDecimal prixUnitaire = getCellValueAsBigDecimal(row.getCell(7));
                Integer delaiAttenteJ = getCellValueAsInteger(row.getCell(8));
                Integer nbrMedicament = getCellValueAsInteger(row.getCell(9));
                Integer dureeTraitement = getCellValueAsInteger(row.getCell(10));
                LocalDate dateDebut = getCellValueAsDate(row.getCell(11));

                if (numBoucle.isEmpty() || nomMaladie.isEmpty() || dateEvenement == null || nomMedicament.isEmpty()
                        || dose == null || unite.isEmpty() || prixUnitaire == null || dureeTraitement == null
                        || dateDebut == null) {
                    throw new RuntimeException("Ligne " + lineNumber
                            + " : Données obligatoires manquantes pour l'import des traitements.");
                }

                Vache vache = vacheRepo.findByNumeroBoucle(numBoucle)
                        .orElseThrow(() -> new RuntimeException(
                                "Ligne " + lineNumber + " : Vache introuvable avec la boucle " + numBoucle));

                Maladie maladie = maladieRepo.findByNomIgnoreCase(nomMaladie)
                        .orElseThrow(() -> new RuntimeException(
                                "Ligne " + lineNumber + " : Maladie introuvable avec le nom " + nomMaladie));

                Medicament medicament = medicamentRepo.findByNomIgnoreCase(nomMedicament)
                        .orElseThrow(() -> new RuntimeException(
                                "Ligne " + lineNumber + " : Medicament introuvable avec le nom " + nomMedicament));

                String descriptionFinale = (description == null || description.isBlank()) ? maladie.getNom() : description;
                String eventKey = numBoucle + "|" + nomMaladie.toLowerCase() + "|" + dateEvenement + "|"
                        + descriptionFinale.toLowerCase();

                EvenementSante evenement = evenementsParCle.get(eventKey);
                if (evenement == null) {
                    evenement = new EvenementSante();
                    evenement.setVache(vache);
                    evenement.setMaladie(maladie);
                    evenement.setDateEvenement(dateEvenement);
                    evenement.setDescription(descriptionFinale);
                    evenement = evenementRepo.save(evenement);
                    evenementsParCle.put(eventKey, evenement);
                }

                MedicamentFille medicamentFille = new MedicamentFille();
                medicamentFille.setMedicament(medicament);
                medicamentFille.setDose(dose);
                medicamentFille.setUnite(unite);
                medicamentFille.setPrixUnitaire(prixUnitaire);
                int delaiParDefaut = delaiAttenteJ != null ? delaiAttenteJ : 0;
                medicamentFille.setDelaiAttenteLaitDefaut(delaiParDefaut);
                medicamentFille.setDelaiAttenteViandeDefaut(delaiParDefaut);
                medicamentFille = medicamentFilleRepo.save(medicamentFille);

                TraitementSante traitement = new TraitementSante();
                traitement.setEvenementSante(evenement);
                traitement.setMedicamentFille(medicamentFille);
                traitement.setNbrMedicament(nbrMedicament != null && nbrMedicament > 0 ? nbrMedicament : 1);
                traitement.setDureeTraitement(dureeTraitement);
                traitement.setDelaiAttenteJ(delaiParDefaut);
                traitement.setDateDebut(dateDebut);
                traitement.setDateFin(calculerDateFinTraitement(dateDebut, dureeTraitement, delaiParDefaut));

                traitementRepo.save(traitement);
            }
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default -> "";
        };
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

    private BigDecimal getCellValueAsBigDecimal(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC) {
            return BigDecimal.valueOf(cell.getNumericCellValue());
        }
        if (cell.getCellType() == CellType.STRING) {
            String value = cell.getStringCellValue().trim();
            if (value.isEmpty()) {
                return null;
            }
            try {
                return new BigDecimal(value.replace(',', '.'));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private Integer getCellValueAsInteger(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC) {
            return (int) Math.round(cell.getNumericCellValue());
        }
        if (cell.getCellType() == CellType.STRING) {
            String value = cell.getStringCellValue().trim();
            if (value.isEmpty()) {
                return null;
            }
            try {
                return Integer.valueOf(value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private LocalDate calculerDateFinTraitement(LocalDate dateDebut, Integer dureeTraitement, Integer delaiAttenteJ) {
        if (dateDebut == null || dureeTraitement == null || dureeTraitement < 1) {
            return null;
        }
        int delai = delaiAttenteJ != null && delaiAttenteJ > 0 ? delaiAttenteJ : 0;
        return dateDebut.plusDays(dureeTraitement.longValue() - 1L + delai);
    }
}
