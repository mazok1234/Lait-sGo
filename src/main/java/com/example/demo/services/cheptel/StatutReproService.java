package com.example.demo.services.cheptel;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.cheptel.RefStatutRepro;
import com.example.demo.entity.cheptel.Vache;
import com.example.demo.entity.cheptel.VacheHistoriqueRepro;
import com.example.demo.repository.cheptel.RefStatutReproRepository;
import com.example.demo.repository.cheptel.VacheHistoriqueReproRepository;

@Service
public class StatutReproService {
    private final RefStatutReproRepository statutRepository;
    private final VacheHistoriqueReproRepository historiqueRepository;

    public StatutReproService(RefStatutReproRepository statutRepository, VacheHistoriqueReproRepository historiqueRepository) {
        this.statutRepository = statutRepository;
        this.historiqueRepository = historiqueRepository;
    }

    public List<RefStatutRepro> findAll() {
        return statutRepository.findAll();
    }

    public RefStatutRepro getById(Integer id) {
        return statutRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Statut repro introuvable: " + id));
    }

    public RefStatutRepro getByLibelle(String libelle) {
        return statutRepository.findByLibelle(libelle)
                .orElseThrow(() -> new IllegalStateException("Statut repro introuvable: " + libelle));
    }

    public Optional<VacheHistoriqueRepro> getHistoriqueActuel(Long vacheId) {
        return historiqueRepository.findByVache_IdAndDateFinIsNull(vacheId);
    }

    public Optional<RefStatutRepro> getStatutActuel(Long vacheId) {
        return getHistoriqueActuel(vacheId).map(VacheHistoriqueRepro::getStatut);
    }

    public List<Long> findVacheIdsByStatut(Integer statutId) {
        return historiqueRepository.findByStatut_IdAndDateFinIsNull(statutId).stream()
                .map(h -> h.getVache().getId())
                .toList();
    }

    public List<VacheHistoriqueRepro> getHistoriqueOuvertParStatut(Integer statutId) {
        return historiqueRepository.findByStatut_IdAndDateFinIsNull(statutId);
    }

    @Transactional
    public void ouvrirInitial(Vache vache, Integer statutId, LocalDate date) {
        VacheHistoriqueRepro h = new VacheHistoriqueRepro();
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
        Optional<VacheHistoriqueRepro> actuel = getHistoriqueActuel(vache.getId());
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
