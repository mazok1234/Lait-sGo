package com.example.demo.services;

import com.example.demo.entity.Ration;
import com.example.demo.entity.RationAliment;
import com.example.demo.repository.RationAlimentRepository;
import com.example.demo.repository.RationRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class RationService {

    private final RationRepository rationRepository;
    private final RationAlimentRepository rationAlimentRepository;

    public RationService(RationRepository rationRepository,
                         RationAlimentRepository rationAlimentRepository) {
        this.rationRepository = rationRepository;
        this.rationAlimentRepository = rationAlimentRepository;
    }

    public List<RationCardVm> getRationCards() {
        return rationRepository.findAllCards().stream()
                .map(card -> new RationCardVm(
                        card.getId(),
                        card.getNom(),
                        card.getStade(),
                        rationRepository.countVachesForRation(card.getId())
                ))
                .toList();
    }

    public Ration findById(Long id) {
        return rationRepository.findById(id).orElse(null);
    }

    public Ration saveRation(Ration ration) {
        return rationRepository.save(ration);
    }

    public List<StadeVm> getStades() {
        return rationRepository.findAllStades().stream()
                .map(s -> new StadeVm(s.getId(), s.getLibelle(), s.getJourMin(), s.getJourMax()))
                .toList();
    }

    public List<RationAliment> findAlimentsByRationId(Long rationId) {
        return rationAlimentRepository.findByRationIdOrderByIdAsc(rationId);
    }

    public void saveRationAliment(RationAliment rationAliment) {
        rationAlimentRepository.save(rationAliment);
    }

    public void deleteRationAliment(Long id) {
        rationAlimentRepository.deleteById(id);
    }

    public NutritionVm calculateNutrition(Long rationId) {
        List<RationAliment> lignes = findAlimentsByRationId(rationId);
        BigDecimal uflTotal = BigDecimal.ZERO;
        BigDecimal coutTotal = BigDecimal.ZERO;

        for (RationAliment ligne : lignes) {
            BigDecimal qte = ligne.getQuantiteKg() != null ? ligne.getQuantiteKg() : BigDecimal.ZERO;
            BigDecimal ufl = ligne.getAliment() != null && ligne.getAliment().getUfl() != null
                    ? ligne.getAliment().getUfl() : BigDecimal.ZERO;
            BigDecimal prixKg = ligne.getAliment() != null && ligne.getAliment().getPrixParKilo() != null
                    ? ligne.getAliment().getPrixParKilo() : BigDecimal.ZERO;

            uflTotal = uflTotal.add(qte.multiply(ufl));
            coutTotal = coutTotal.add(qte.multiply(prixKg));
        }

        return new NutritionVm(uflTotal, coutTotal);
    }

    public record RationCardVm(Long id, String nom, String stade, long nbVaches) {
    }

    public record StadeVm(Integer id, String libelle, Integer jourMin, Integer jourMax) {
    }

    public record NutritionVm(BigDecimal uflTotal, BigDecimal coutTotalAr) {
    }
}
