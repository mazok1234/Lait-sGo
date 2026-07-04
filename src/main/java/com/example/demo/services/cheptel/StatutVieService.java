package com.example.demo.services.cheptel;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.cheptel.RefStatutVie;
import com.example.demo.entity.cheptel.Vache;
import com.example.demo.entity.cheptel.VacheHistoriqueVie;
import com.example.demo.repository.cheptel.RefStatutVieRepository;
import com.example.demo.repository.cheptel.VacheHistoriqueVieRepository;

@Service
public class StatutVieService {
    private final RefStatutVieRepository statutRepository;
    private final VacheHistoriqueVieRepository historiqueRepository;

    public StatutVieService(RefStatutVieRepository statutRepository, VacheHistoriqueVieRepository historiqueRepository) {
        this.statutRepository = statutRepository;
        this.historiqueRepository = historiqueRepository;
    }

    public List<RefStatutVie> findAll() {
        return statutRepository.findAll();
    }

    public RefStatutVie getById(Integer id) {
        return statutRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Statut vie introuvable: " + id));
    }

    public RefStatutVie getByLibelle(String libelle) {
        return statutRepository.findByLibelle(libelle)
                .orElseThrow(() -> new IllegalStateException("Statut vie introuvable: " + libelle));
    }

    public Optional<VacheHistoriqueVie> getHistoriqueActuel(Long vacheId) {
        return historiqueRepository.findByVache_IdAndDateFinIsNull(vacheId);
    }

    public Optional<RefStatutVie> getStatutActuel(Long vacheId) {
        return getHistoriqueActuel(vacheId).map(VacheHistoriqueVie::getStatut);
    }

    public List<Long> findVacheIdsByStatut(Integer statutId) {
        return historiqueRepository.findByStatut_IdAndDateFinIsNull(statutId).stream()
                .map(h -> h.getVache().getId())
                .toList();
    }

    @Transactional
    public void ouvrirInitial(Vache vache, Integer statutId, LocalDate date) {
        VacheHistoriqueVie h = new VacheHistoriqueVie();
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
        Optional<VacheHistoriqueVie> actuel = getHistoriqueActuel(vache.getId());
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
