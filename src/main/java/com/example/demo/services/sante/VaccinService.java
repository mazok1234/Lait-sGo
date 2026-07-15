package com.example.demo.services.sante;

import com.example.demo.services.alerte.AlerteService;
import com.example.demo.entity.alerte.Alerte;
import com.example.demo.repository.alerte.AlerteRepository;
import com.example.demo.entity.sante.HistoriqueVaccin;
import com.example.demo.entity.cheptel.Vache;
import com.example.demo.repository.sante.HistoriqueVaccinRepository;
import com.example.demo.repository.cheptel.VacheRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import com.example.demo.dto.VaccinStatDTO;
import java.util.Optional;

@Service
public class VaccinService {

        private final VacheRepository vacheRepository;
        private final HistoriqueVaccinRepository historiqueRepository;
        private final AlerteService alerteService;
        private final AlerteRepository alerteRepo;

        public VaccinService(VacheRepository vacheRepository,
                        AlerteService alerteService,
                        HistoriqueVaccinRepository historiqueRepository,
                        AlerteRepository alerteRepo) {
                this.vacheRepository = vacheRepository;
                this.alerteService = alerteService;
                this.historiqueRepository = historiqueRepository;
                this.alerteRepo = alerteRepo;
        }

        public List<HistoriqueVaccin> getVaccinsEnRetard(Long vacheId) {
                Vache vache = vacheRepository.findById(vacheId)
                                .orElseThrow(() -> new RuntimeException("Vache introuvable"));
                List<HistoriqueVaccin> historiques = historiqueRepository.findByVache(vache);
                List<HistoriqueVaccin> enRetard = new ArrayList<>();
                LocalDate today = LocalDate.now();

                for (HistoriqueVaccin h : historiques) {
                        int rappel = h.getProtocoleVaccin().getDureeRappelJours();
                        LocalDate prochaineDate = h.getDateVaccination().plusDays(rappel);

                        if (prochaineDate.isBefore(today)) {
                                enRetard.add(h);
                                long joursRetard = ChronoUnit.DAYS.between(prochaineDate, today);

                                alerteService.envoyerAlerte(
                                                "vaccin_en_retard",
                                                "urgent",
                                                "Vaccin en retard — " + h.getVache().getNumeroBoucle(),
                                                "Vaccin " + h.getProtocoleVaccin().getNomVaccin()
                                                                + " en retard de " + joursRetard + " jour(s)"
                                                                + " (rappel prévu le " + prochaineDate + ").",
                                                h.getVache().getId());
                        }
                }
                return enRetard;
        }

        public List<HistoriqueVaccin> getVaccinsARevoirParMois(int mois, int annee) {
                List<HistoriqueVaccin> derniers = historiqueRepository.findLastByVaccin();
                List<HistoriqueVaccin> result = new ArrayList<>();

                for (HistoriqueVaccin h : derniers) {
                        LocalDate prochaineDate = h.getDateVaccination()
                                        .plusDays(h.getProtocoleVaccin().getDureeRappelJours());

                        if (prochaineDate.getMonthValue() == mois
                                        && prochaineDate.getYear() == annee) {
                                result.add(h);

                                alerteService.envoyerAlerte(
                                                "rappel_vaccin",
                                                "info",
                                                "Rappel vaccin — " + h.getVache().getNumeroBoucle(),
                                                "Vaccin " + h.getProtocoleVaccin().getNomVaccin()
                                                                + " à revoir le " + prochaineDate + ".",
                                                h.getVache().getId());
                        }
                }
                return result;
        }

        public List<HistoriqueVaccin> getVaccinsPrioritaires() {
                List<HistoriqueVaccin> derniers = historiqueRepository.findLastByVaccin();
                LocalDate today = LocalDate.now();

                List<HistoriqueVaccin> sorted = derniers.stream()
                                .sorted((a, b) -> {
                                        LocalDate dateA = a.getDateVaccination()
                                                        .plusDays(a.getProtocoleVaccin().getDureeRappelJours());
                                        LocalDate dateB = b.getDateVaccination()
                                                        .plusDays(b.getProtocoleVaccin().getDureeRappelJours());
                                        return dateA.compareTo(dateB);
                                })
                                .toList();

                sorted.forEach(h -> {
                        LocalDate prochaineDate = h.getDateVaccination()
                                        .plusDays(h.getProtocoleVaccin().getDureeRappelJours());
                        long joursAvant = ChronoUnit.DAYS.between(today, prochaineDate);

                        String niveauCode;
                        String typeCode;
                        String titre;
                        String description;

                        if (prochaineDate.isBefore(today)) {
                                long joursRetard = ChronoUnit.DAYS.between(prochaineDate, today);
                                niveauCode = "urgent";
                                typeCode = "vaccin_en_retard";
                                titre = "Vaccin en retard — " + h.getVache().getNumeroBoucle();
                                description = "Vaccin " + h.getProtocoleVaccin().getNomVaccin()
                                                + " en retard de " + joursRetard + " jour(s)"
                                                + " (rappel prévu le " + prochaineDate + ").";
                        } else if (joursAvant <= 7) {
                                niveauCode = "attention";
                                typeCode = "vaccin_prioritaire";
                                titre = "Vaccin prioritaire dans " + joursAvant + "j — "
                                                + h.getVache().getNumeroBoucle();
                                description = "Vaccin " + h.getProtocoleVaccin().getNomVaccin()
                                                + " — prochain rappel le " + prochaineDate + ".";
                        } else {
                                niveauCode = "info";
                                typeCode = "rappel_vaccin";
                                titre = "Rappel vaccin — " + h.getVache().getNumeroBoucle();
                                description = "Vaccin " + h.getProtocoleVaccin().getNomVaccin()
                                                + " — prochain rappel le " + prochaineDate
                                                + " (dans " + joursAvant + " jours).";
                        }

                        alerteService.envoyerAlerte(
                                        typeCode,
                                        niveauCode,
                                        titre,
                                        description,
                                        h.getVache().getId());
                        alerteRepo.findTopByVacheIdAndTypeAlerteAndAcquitteeFalseOrderByCreatedAtDesc(
                                        h.getVache().getId(), typeCode).ifPresentOrElse(
                                                        a -> {
                                                                h.setAlerteId(a.getId());
                                                                h.setAlerteAcquittee(false);
                                                        },
                                                        () -> {
                                                                alerteRepo.findTopByVacheIdAndTypeAlerteAndAcquitteeTrueOrderByCreatedAtDesc(
                                                                                h.getVache().getId(), typeCode)
                                                                                .ifPresent(a -> {
                                                                                        h.setAlerteId(null);
                                                                                        h.setAlerteAcquittee(true);
                                                                                });
                                                        });
                });

                return sorted;
        }

        public List<VaccinStatDTO> getStatistiquesVaccins() {
                return historiqueRepository.findAll()
                                .stream()
                                .collect(Collectors.groupingBy(h -> h.getProtocoleVaccin().getNomVaccin()))
                                .entrySet().stream()
                                .map(entry -> {
                                        List<HistoriqueVaccin> list = entry.getValue();
                                        long nombreBovins = list.stream()
                                                        .map(h -> h.getVache().getId()).distinct().count();
                                        HistoriqueVaccin last = list.stream()
                                                        .max(Comparator.comparing(HistoriqueVaccin::getDateVaccination))
                                                        .orElse(null);
                                        LocalDate lastDate = last != null ? last.getDateVaccination() : null;
                                        LocalDate nextDate = last != null
                                                        ? lastDate.plusDays(
                                                                        last.getProtocoleVaccin().getDureeRappelJours())
                                                        : null;
                                        return new VaccinStatDTO(entry.getKey(), nombreBovins, lastDate, nextDate);
                                })
                                .toList();
        }

        public List<String> statut(List<HistoriqueVaccin> vaccins) {
                List<String> statuts = new ArrayList<>();
                LocalDate today = LocalDate.now();
                for (HistoriqueVaccin h : vaccins) {
                        LocalDate prochaineDate = h.getDateVaccination()
                                        .plusDays(h.getProtocoleVaccin().getDureeRappelJours());
                        long joursRetard = ChronoUnit.DAYS.between(prochaineDate, today);
                        if (joursRetard > 0)
                                statuts.add("EN RETARD " + joursRetard + "j");
                        else if (joursRetard > -7)
                                statuts.add("A REVOIR " + joursRetard + "j");
                        else
                                statuts.add("------");
                }
                return statuts;
        }
}