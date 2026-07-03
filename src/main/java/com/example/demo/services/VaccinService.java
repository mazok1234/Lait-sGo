package com.example.demo.services;

import com.example.demo.entity.HistoriqueVaccin;
import com.example.demo.entity.Vache;
import com.example.demo.repository.HistoriqueVaccinRepository;
import com.example.demo.repository.VacheRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import com.example.demo.dto.VaccinStatDTO;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class VaccinService {

        private final VacheRepository vacheRepository;
        private final HistoriqueVaccinRepository historiqueRepository;
        private final AlerteService alerteService;

        public VaccinService(VacheRepository vacheRepository, AlerteService alerteService,
                        HistoriqueVaccinRepository historiqueRepository) {
                this.vacheRepository = vacheRepository;
                this.historiqueRepository = historiqueRepository;
                this.alerteService = alerteService;
        }

        // ------------------3
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

                                alerteService.envoyerAlerte(
                                                "vaccin_en_retard",
                                                "urgent",
                                                "Vaccin en retard — " + h.getVache().getNumeroBoucle(),
                                                "Vaccin " + h.getProtocoleVaccin().getNomVaccin()
                                                                + " en retard depuis le " + prochaineDate + ".",
                                                h.getVache().getId());
                        }
                }

                return enRetard;
        }

        // ------------------2
        public List<HistoriqueVaccin> getVaccinsARevoirParMois(int mois, int annee) {

                List<HistoriqueVaccin> derniers = historiqueRepository.findLastByVaccin();

                List<HistoriqueVaccin> result = new ArrayList<>();

                for (HistoriqueVaccin h : derniers) {

                        LocalDate prochaineDate = h.getDateVaccination()
                                        .plusDays(h.getProtocoleVaccin().getDureeRappelJours());

                        if (prochaineDate.getMonthValue() == mois &&
                                        prochaineDate.getYear() == annee) {
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

        // ------------------1
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

                // Envoyer ou mettre à jour une alerte adaptée pour chaque vaccin prioritaire
                sorted.forEach(h -> {
                        LocalDate prochaineDate = h.getDateVaccination()
                                        .plusDays(h.getProtocoleVaccin().getDureeRappelJours());

                        String niveauAlerte;
                        String titreAlerte;

                        // On compare par rapport à la date du jour (3 juillet 2026)
                        // Si la date limite de rappel est dépassée (ex: 2025) -> URGENT
                        if (prochaineDate.isBefore(today)) {
                                niveauAlerte = "urgent";
                                titreAlerte = "Retard critique : Vaccin " + h.getProtocoleVaccin().getNomVaccin();
                        } else {
                                // Sinon, c'est une alerte préventive pour le futur -> ATTENTION
                                niveauAlerte = "attention";
                                titreAlerte = "Rappel à planifier : Vaccin " + h.getProtocoleVaccin().getNomVaccin();
                        }

                        // On utilise ici le code unique commun 'vaccin_prioritaire' présent dans ta
                        // base
                        alerteService.envoyerAlerte(
                                        "vaccin_prioritaire",
                                        niveauAlerte,
                                        titreAlerte,
                                        "Le vaccin " + h.getProtocoleVaccin().getNomVaccin()
                                                        + " est requis pour la vache " + h.getVache().getNumeroBoucle()
                                                        + " (Date prévue : " + prochaineDate + ").",
                                        h.getVache().getId());
                });

                return sorted;
        }

        public List<VaccinStatDTO> getStatistiquesVaccins() {

                return historiqueRepository.findAll()
                                .stream()
                                .collect(Collectors.groupingBy(
                                                h -> h.getProtocoleVaccin().getNomVaccin()))
                                .entrySet()
                                .stream()
                                .map(entry -> {

                                        List<HistoriqueVaccin> list = entry.getValue();

                                        long nombreBovins = list.stream()
                                                        .map(h -> h.getVache().getId())
                                                        .distinct()
                                                        .count();

                                        HistoriqueVaccin last = list.stream()
                                                        .max(Comparator.comparing(HistoriqueVaccin::getDateVaccination))
                                                        .orElse(null);

                                        LocalDate lastDate = (last != null)
                                                        ? last.getDateVaccination()
                                                        : null;

                                        LocalDate nextDate = (last != null)
                                                        ? lastDate.plusDays(
                                                                        last.getProtocoleVaccin().getDureeRappelJours())
                                                        : null;

                                        return new VaccinStatDTO(
                                                        entry.getKey(),
                                                        nombreBovins,
                                                        lastDate,
                                                        nextDate);
                                })
                                .toList();
        }
}