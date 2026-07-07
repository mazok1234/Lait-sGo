package com.example.demo.services.reproduction;

import com.example.demo.dto.AlerteReproductionDTO;
import com.example.demo.entity.reproduction.Reproduction;
import com.example.demo.repository.reproduction.ReproductionRepository;
import com.example.demo.services.alerte.AlerteService; // ← ajouté
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ReproductionAlerteService {

    @Autowired
    private ReproductionRepository reproductionRepository;

    @Autowired
    private AlerteService alerteService; // ← ajouté

    public List<AlerteReproductionDTO> getAlertesVelage() {
        List<Reproduction> reproductions = reproductionRepository.findAll();
        List<AlerteReproductionDTO> alertes = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (Reproduction r : reproductions) {
            if (r.getStatutIA() == null
                    || !r.getStatutIA().equals("gestante")
                    || r.getDateVelageReel() != null) {
                continue;
            }

            LocalDate dateVelagePrevue = r.getDateIA().plusDays(280);
            long joursRestants = ChronoUnit.DAYS.between(today, dateVelagePrevue);

            if (joursRestants <= 0) continue;

            AlerteReproductionDTO alerte = new AlerteReproductionDTO();
            alerte.setId(r.getId());
            alerte.setVacheId(r.getVache().getId());
            alerte.setNumeroBoucle(r.getVache().getNumeroBoucle());
            alerte.setDateIA(r.getDateIA());
            alerte.setDateVelagePrevue(dateVelagePrevue);
            alerte.setJoursRestants(joursRestants);

            String niveauUrgence;
            if (joursRestants <= 30) {
                niveauUrgence = "urgent";
            } else if (joursRestants <= 60) {
                niveauUrgence = "attention";
            } else {
                niveauUrgence = "info";
            }
            alerte.setNiveauUrgence(niveauUrgence);

            // ← INJECTION ALERTE — avec tous les détails du module Reproduction
            alerteService.envoyerAlerte(
                "rappel_velage",
                niveauUrgence,
                "Rappel vêlage — " + r.getVache().getNumeroBoucle(),
                "Vêlage prévu le " + dateVelagePrevue
                    + " — dans " + joursRestants + " jour(s)"
                    + " (IA enregistrée le " + r.getDateIA() + ").",
                r.getVache().getId()
            );
            // ← FIN INJECTION

            alertes.add(alerte);
        }

        alertes.sort(Comparator
                .comparing(AlerteReproductionDTO::getNiveauUrgence,
                        (u1, u2) -> {
                            int o1 = u1.equals("urgent") ? 1 : u1.equals("attention") ? 2 : 3;
                            int o2 = u2.equals("urgent") ? 1 : u2.equals("attention") ? 2 : 3;
                            return Integer.compare(o1, o2);
                        })
                .thenComparing(AlerteReproductionDTO::getJoursRestants));

        return alertes;
    }
}