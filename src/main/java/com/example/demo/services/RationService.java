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
        RationRepository.NutritionProjection nutrition = rationRepository.findNutritionByRationId(rationId);
        if (nutrition == null) {
            return new NutritionVm(BigDecimal.ZERO, BigDecimal.ZERO);
        }

        return new NutritionVm(
            nutrition.getUflTotal() != null ? nutrition.getUflTotal() : BigDecimal.ZERO,
            nutrition.getCoutTotalAr() != null ? nutrition.getCoutTotalAr() : BigDecimal.ZERO
        );
        }

        public List<RationActiveVacheVm> getRationsActivesVaches() {
        return rationRepository.findRationsActivesVaches().stream()
            .map(v -> new RationActiveVacheVm(
                v.getVacheId(),
                v.getNumeroBoucle(),
                v.getJoursEnLait(),
                v.getPhaseActuelle(),
                v.getRationId(),
                v.getRationRecommandee()
            ))
            .toList();
    }

    public record RationCardVm(Long id, String nom, String stade, long nbVaches) {
    }

    public record StadeVm(Integer id, String libelle, Integer jourMin, Integer jourMax) {
    }

    public record NutritionVm(BigDecimal uflTotal, BigDecimal coutTotalAr) {
    }

    public record RationActiveVacheVm(Long vacheId,
                                      String numeroBoucle,
                                      Integer joursEnLait,
                                      String phaseActuelle,
                                      Long rationId,
                                      String rationRecommandee) {
    }
}
