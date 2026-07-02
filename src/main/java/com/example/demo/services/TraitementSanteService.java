package com.example.demo.services;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.EvenementSante;
import com.example.demo.entity.Maladie;
import com.example.demo.entity.Medicament;
import com.example.demo.entity.TraitementSante;
import com.example.demo.entity.Vache;
import com.example.demo.repository.EvenementSanteRepository;
import com.example.demo.repository.MaladieRepository;
import com.example.demo.repository.MedicamentRepository; // Import essentiel
import com.example.demo.repository.TraitementSanteRepository;
import com.example.demo.repository.VacheRepository;

@Service
public class TraitementSanteService {

    private final TraitementSanteRepository traitementRepository;
    private final EvenementSanteRepository evenementRepository;
    private final VacheRepository vacheRepository;
    private final MaladieRepository maladieRepository;
    private final MedicamentRepository medicamentRepository;

    public TraitementSanteService(TraitementSanteRepository traitementRepository,
                                  EvenementSanteRepository evenementRepository,
                                  VacheRepository vacheRepository,
                                  MaladieRepository maladieRepository,
                                  MedicamentRepository medicamentRepository) {
        this.traitementRepository = traitementRepository;
        this.evenementRepository = evenementRepository;
        this.vacheRepository = vacheRepository;
        this.maladieRepository = maladieRepository;
        this.medicamentRepository = medicamentRepository;
    }

    public List<TraitementSante> findAll() {
        return traitementRepository.findAllByOrderByDateDebutDesc();
    }

    public TraitementSante findById(Long id) {
        TraitementSante traitement = traitementRepository.findById(id).orElse(null);
        if (traitement != null) {
            syncIds(traitement);
        }
        return traitement;
    }

    public TraitementSante createEmptyForm() {
        TraitementSante traitement = new TraitementSante();
        traitement.setDateDebut(LocalDate.now());
        traitement.setDelaiAttenteJ(0);
        return traitement;
    }

    @Transactional // Sécurise l'écriture simultanée dans les 2 tables
    public TraitementSante save(TraitementSante traitement) {
        EvenementSante evenement = null;

        // Si le traitement existe déjà, on va chercher son Événement lié en BDD
        if (traitement.getId() != null) {
            TraitementSante existant = traitementRepository.findById(traitement.getId()).orElse(null);
            if (existant != null) {
                evenement = existant.getEvenementSante();
            }
        }

        // Si aucun événement trouvé (Nouveau traitement), on l'instancie
        if (evenement == null) {
            evenement = new EvenementSante();
        }

        // Récupération sécurisée des entités liées
        Vache vache = vacheRepository.findById(traitement.getVacheId())
                .orElseThrow(() -> new IllegalArgumentException("Vache introuvable"));
        Maladie maladie = maladieRepository.findById(traitement.getMaladieId())
                .orElseThrow(() -> new IllegalArgumentException("Maladie introuvable"));
        Medicament medicament = medicamentRepository.findById(traitement.getMedicamentId())
                .orElseThrow(() -> new IllegalArgumentException("Medicament introuvable"));

        // Remplissage de la 1ère table (EvenementSante)
        evenement.setVache(vache);
        evenement.setMaladie(maladie);
        evenement.setDateEvenement(traitement.getDateDebut());
        if (evenement.getDescription() == null || evenement.getDescription().isBlank()) {
            evenement.setDescription(maladie.getNom());
        }
        evenement = evenementRepository.save(evenement); // Sauvegarde Table 1

        // Remplissage de la 2ème table (TraitementSante)
        traitement.setEvenementSante(evenement);
        traitement.setMedicament(medicament);
        traitement.setDateFin(calculerDateFin(traitement.getDateDebut(), traitement.getDureeTraitement()));

        TraitementSante saved = traitementRepository.save(traitement); // Sauvegarde Table 2
        syncIds(saved);
        return saved;
    }

    @Transactional
    public void deleteById(Long id) {
        TraitementSante traitement = traitementRepository.findById(id).orElse(null);
        if (traitement == null) {
            return;
        }

        EvenementSante evenement = traitement.getEvenementSante();
        traitementRepository.delete(traitement);
        if (evenement != null) {
            evenementRepository.delete(evenement);
        }
    }

    public List<Vache> findAllVaches() {
        return vacheRepository.findAll();
    }

    public List<Maladie> findAllMaladies() {
        return maladieRepository.findAll();
    }

    public List<Medicament> findAllMedicaments() {
        return medicamentRepository.findAll();
    }

    private LocalDate calculerDateFin(LocalDate dateDebut, Integer dureeTraitement) {
        if (dateDebut == null || dureeTraitement == null || dureeTraitement < 1) {
            return null;
        }
        return dateDebut.plusDays(dureeTraitement.longValue() - 1L);
    }

    private void syncIds(TraitementSante traitement) {
        if (traitement.getEvenementSante() != null) {
            if (traitement.getEvenementSante().getVache() != null) {
                traitement.setVacheId(traitement.getEvenementSante().getVache().getId());
            }
            if (traitement.getEvenementSante().getMaladie() != null) {
                traitement.setMaladieId(traitement.getEvenementSante().getMaladie().getId());
            }
        }
        if (traitement.getMedicament() != null) {
            traitement.setMedicamentId(traitement.getMedicament().getId());
        }
    }
}