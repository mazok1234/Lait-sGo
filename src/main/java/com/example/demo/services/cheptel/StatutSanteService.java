package com.example.demo.services.cheptel;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.cheptel.RefStatutSante;
import com.example.demo.entity.cheptel.Vache;
import com.example.demo.entity.cheptel.VacheHistoriqueSante;
import com.example.demo.repository.cheptel.RefStatutSanteRepository;
import com.example.demo.repository.cheptel.VacheHistoriqueSanteRepository;

@Service
public class StatutSanteService {
    private final RefStatutSanteRepository statutRepository;
    private final VacheHistoriqueSanteRepository historiqueRepository;

    public StatutSanteService(RefStatutSanteRepository statutRepository, VacheHistoriqueSanteRepository historiqueRepository) {
        this.statutRepository = statutRepository;
        this.historiqueRepository = historiqueRepository;
    }

    public List<RefStatutSante> findAll() {
        return statutRepository.findAll();
    }

    public RefStatutSante getById(Integer id) {
        return statutRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Statut santé introuvable: " + id));
    }

    public RefStatutSante getByLibelle(String libelle) {
        return statutRepository.findByLibelle(libelle)
                .orElseThrow(() -> new IllegalStateException("Statut santé introuvable: " + libelle));
    }

    public Optional<VacheHistoriqueSante> getHistoriqueActuel(Long vacheId) {
        return historiqueRepository.findByVache_IdAndDateFinIsNull(vacheId);
    }

    public Optional<RefStatutSante> getStatutActuel(Long vacheId) {
        return getHistoriqueActuel(vacheId).map(VacheHistoriqueSante::getStatut);
    }

    public List<Long> findVacheIdsByStatut(Integer statutId) {
        return historiqueRepository.findByStatut_IdAndDateFinIsNull(statutId).stream()
                .map(h -> h.getVache().getId())
                .toList();
    }

    @Transactional
    public void ouvrirInitial(Vache vache, Integer statutId, LocalDate date) {
        VacheHistoriqueSante h = new VacheHistoriqueSante();
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
        Optional<VacheHistoriqueSante> actuel = getHistoriqueActuel(vache.getId());
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
