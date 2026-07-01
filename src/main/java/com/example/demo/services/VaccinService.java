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

    public VaccinService(VacheRepository vacheRepository,
                         HistoriqueVaccinRepository historiqueRepository) {
        this.vacheRepository = vacheRepository;
        this.historiqueRepository = historiqueRepository;
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

            if (prochaineDate.getMonthValue() == mois &&
                prochaineDate.getYear() == annee) {
                result.add(h);
            }
        }

        return result;
    }

    public List<HistoriqueVaccin> getVaccinsPrioritaires() {

        List<HistoriqueVaccin> derniers = historiqueRepository.findLastByVaccin();

        LocalDate today = LocalDate.now();

        return derniers.stream()
                .sorted((a, b) -> {

                    LocalDate dateA = a.getDateVaccination()
                            .plusDays(a.getProtocoleVaccin().getDureeRappelJours());

                    LocalDate dateB = b.getDateVaccination()
                            .plusDays(b.getProtocoleVaccin().getDureeRappelJours());

                    return dateA.compareTo(dateB); // le plus urgent en premier
                })
                .toList();
    }

    public List<VaccinStatDTO> getStatistiquesVaccins() {

        return historiqueRepository.findAll()
            .stream()
            .collect(Collectors.groupingBy(
                    h -> h.getProtocoleVaccin().getNomVaccin()
            ))
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
                        ? lastDate.plusDays(last.getProtocoleVaccin().getDureeRappelJours())
                        : null;

                return new VaccinStatDTO(
                        entry.getKey(),
                        nombreBovins,
                        lastDate,
                        nextDate
                );
            })
            .toList();
    }
}