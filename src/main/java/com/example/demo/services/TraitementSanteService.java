package com.example.demo.services;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.EvenementSante;
import com.example.demo.entity.Maladie;
import com.example.demo.entity.Medicament;
import com.example.demo.entity.RefStatutVache;
import com.example.demo.entity.TraitementSante;
import com.example.demo.entity.Vache;
import com.example.demo.repository.EvenementSanteRepository;
import com.example.demo.repository.MaladieRepository;
import com.example.demo.repository.MedicamentRepository; // Import essentiel
import com.example.demo.repository.RefStatutVacheRepository;
import com.example.demo.repository.TraitementSanteRepository;
import com.example.demo.repository.VacheRepository;

@Service
public class TraitementSanteService {

    private static final String STATUT_EN_LACTATION = "en_lactation";
    private static final String STATUT_TARIE = "tarie";

    private final TraitementSanteRepository traitementRepository;
    private final EvenementSanteRepository evenementRepository;
    private final VacheRepository vacheRepository;
    private final MaladieRepository maladieRepository;
    private final MedicamentRepository medicamentRepository;
    private final RefStatutVacheRepository statutVacheRepository;

    public TraitementSanteService(TraitementSanteRepository traitementRepository,
                                  EvenementSanteRepository evenementRepository,
                                  VacheRepository vacheRepository,
                                  MaladieRepository maladieRepository,
                                  MedicamentRepository medicamentRepository,
                                  RefStatutVacheRepository statutVacheRepository) {
        this.traitementRepository = traitementRepository;
        this.evenementRepository = evenementRepository;
        this.vacheRepository = vacheRepository;
        this.maladieRepository = maladieRepository;
        this.medicamentRepository = medicamentRepository;
        this.statutVacheRepository = statutVacheRepository;
    }

    public List<TraitementSante> findAll() {
        return traitementRepository.findAllByOrderByDateDebutDesc();
    }

    public long countVachesTariees() {
        return vacheRepository.countByStatut_Code(STATUT_TARIE);
    }

    @Transactional
    public void synchronizeVacheStatuses() {
        RefStatutVache statutTarie = getStatutRequired(STATUT_TARIE);
        RefStatutVache statutLactation = getStatutRequired(STATUT_EN_LACTATION);

        Set<Long> vachesAvecTraitementActif = new HashSet<>();
        LocalDate today = LocalDate.now();

        for (TraitementSante traitement : traitementRepository.findAllByOrderByDateDebutDesc()) {
            if (isTreatmentActif(traitement, today)) {
                Vache vache = traitement.getEvenementSante() != null ? traitement.getEvenementSante().getVache() : null;
                if (vache != null && vache.getId() != null) {
                    vachesAvecTraitementActif.add(vache.getId());
                }
            }
        }

        List<Vache> vaches = vacheRepository.findAll();
        boolean changed = false;

        for (Vache vache : vaches) {
            if (vache.getId() == null) {
                continue;
            }

            boolean doitEtreTariee = vachesAvecTraitementActif.contains(vache.getId());
            boolean estDejaTariee = vache.getStatut() != null && STATUT_TARIE.equals(vache.getStatut().getCode());

            if (doitEtreTariee && !estDejaTariee) {
                vache.setStatut(statutTarie);
                changed = true;
            } else if (!doitEtreTariee && estDejaTariee) {
                vache.setStatut(statutLactation);
                changed = true;
            }
        }

        if (changed) {
            vacheRepository.saveAll(vaches);
        }
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
        synchronizeVacheStatuses();
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
        synchronizeVacheStatuses();
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

    private boolean isTreatmentActif(TraitementSante traitement, LocalDate today) {
        if (traitement == null || traitement.getDateDebut() == null) {
            return false;
        }

        LocalDate dateFinTraitement = calculerDateFin(traitement.getDateDebut(), traitement.getDureeTraitement());
        if (dateFinTraitement == null || traitement.getDelaiAttenteJ() == null) {
            return false;
        }

        LocalDate dateFinAttente = dateFinTraitement.plusDays(traitement.getDelaiAttenteJ().longValue());
        return !today.isBefore(traitement.getDateDebut()) && !today.isAfter(dateFinAttente);
    }

    private RefStatutVache getStatutRequired(String code) {
        return statutVacheRepository.findByCode(code)
                .orElseThrow(() -> new IllegalStateException("Statut vache introuvable: " + code));
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