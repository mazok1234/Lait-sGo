package com.example.demo.services.cheptel;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.cheptel.RefStatutLactationVache;
import com.example.demo.entity.cheptel.Vache;
import com.example.demo.entity.cheptel.VacheHistoriqueLactation;
import com.example.demo.repository.cheptel.RefStatutLactationVacheRepository;
import com.example.demo.repository.cheptel.VacheHistoriqueLactationRepository;

@Service
public class StatutLactationVacheService {
    private final RefStatutLactationVacheRepository statutRepository;
    private final VacheHistoriqueLactationRepository historiqueRepository;

    public StatutLactationVacheService(RefStatutLactationVacheRepository statutRepository,
            VacheHistoriqueLactationRepository historiqueRepository) {
        this.statutRepository = statutRepository;
        this.historiqueRepository = historiqueRepository;
    }

    public List<RefStatutLactationVache> findAll() {
        return statutRepository.findAll();
    }

    public RefStatutLactationVache getById(Integer id) {
        return statutRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Statut lactation introuvable: " + id));
    }

    public RefStatutLactationVache getByLibelle(String libelle) {
        return statutRepository.findByLibelle(libelle)
                .orElseThrow(() -> new IllegalStateException("Statut lactation introuvable: " + libelle));
    }

    public Optional<VacheHistoriqueLactation> getHistoriqueActuel(Long vacheId) {
        return historiqueRepository.findByVache_IdAndDateFinIsNull(vacheId);
    }

    public Optional<RefStatutLactationVache> getStatutActuel(Long vacheId) {
        return getHistoriqueActuel(vacheId).map(VacheHistoriqueLactation::getStatut);
    }

    public List<Long> findVacheIdsByStatut(Integer statutId) {
        return historiqueRepository.findByStatut_IdAndDateFinIsNull(statutId).stream()
                .map(h -> h.getVache().getId())
                .toList();
    }

    @Transactional
    public void ouvrirInitial(Vache vache, Integer statutId, LocalDate date) {
        VacheHistoriqueLactation h = new VacheHistoriqueLactation();
        h.setVache(vache);
        h.setStatut(getById(statutId));
        h.setDateDebut(date);
        historiqueRepository.save(h);
    }

    @Transactional
    public void supprimerHistorique(Long vacheId) {
        historiqueRepository.deleteByVache_Id(vacheId);
    }

    @Transactional
    public void changerStatut(Vache vache, Integer nouveauStatutId, LocalDate date) {
        Optional<VacheHistoriqueLactation> actuel = getHistoriqueActuel(vache.getId());
        if (actuel.isPresent() && actuel.get().getStatut().getId().equals(nouveauStatutId)) {
            return;
        }
        actuel.ifPresent(h -> {
            h.setDateFin(date);
            historiqueRepository.save(h);
        });
        ouvrirInitial(vache, nouveauStatutId, date);
    }
}
