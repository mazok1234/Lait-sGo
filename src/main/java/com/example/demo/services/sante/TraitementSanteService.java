package com.example.demo.services.sante;

import com.example.demo.services.cheptel.StatutLactationVacheService;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.sante.EvenementSante;
import com.example.demo.entity.sante.Maladie;
import com.example.demo.entity.sante.Medicament;
import com.example.demo.entity.cheptel.RefStatutLactationVache;
import com.example.demo.entity.sante.TraitementSante;
import com.example.demo.entity.cheptel.Vache;
import com.example.demo.repository.sante.EvenementSanteRepository;
import com.example.demo.repository.sante.MaladieRepository;
import com.example.demo.repository.sante.MedicamentRepository;
import com.example.demo.repository.sante.TraitementSanteRepository;
import com.example.demo.repository.cheptel.VacheRepository;

@Service
public class TraitementSanteService {
    private static final String STATUT_EN_LACTATION = "En_lactation";
    private static final String STATUT_TARIE = "Tarie";

    private final TraitementSanteRepository traitementRepository;
    private final EvenementSanteRepository evenementRepository;
    private final VacheRepository vacheRepository;
    private final MaladieRepository maladieRepository;
    private final MedicamentRepository medicamentRepository;
    private final StatutLactationVacheService statutLactationService;

    public TraitementSanteService(TraitementSanteRepository traitementRepository,
                                  EvenementSanteRepository evenementRepository,
                                  VacheRepository vacheRepository,
                                  MaladieRepository maladieRepository,
                                  MedicamentRepository medicamentRepository,
                                  StatutLactationVacheService statutLactationService) {
        this.traitementRepository = traitementRepository;
        this.evenementRepository = evenementRepository;
        this.vacheRepository = vacheRepository;
        this.maladieRepository = maladieRepository;
        this.medicamentRepository = medicamentRepository;
        this.statutLactationService = statutLactationService;
    }

    public List<TraitementSante> findAll() {
        return traitementRepository.findAllByOrderByDateDebutDesc();
    }

    public long countVachesTariees() {
        return statutLactationService.findVacheIdsByStatut(statutLactationService.getByLibelle(STATUT_TARIE).getId()).size();
    }

    @Transactional
    public void synchronizeVacheStatuses() {
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

        for (Vache vache : vacheRepository.findAll()) {
            if (vache.getId() == null) {
                continue;
            }

            boolean doitEtreTariee = vachesAvecTraitementActif.contains(vache.getId());
            String statutActuel = statutLactationService.getStatutActuel(vache.getId())
                    .map(RefStatutLactationVache::getLibelle).orElse(null);
            boolean estDejaTariee = STATUT_TARIE.equals(statutActuel);

            if (doitEtreTariee && !estDejaTariee) {
                statutLactationService.changerStatut(vache, statutLactationService.getByLibelle(STATUT_TARIE).getId(), today);
            } else if (!doitEtreTariee && estDejaTariee) {
                statutLactationService.changerStatut(vache, statutLactationService.getByLibelle(STATUT_EN_LACTATION).getId(), today);
            }
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

    @Transactional
    public TraitementSante save(TraitementSante traitement) {
        EvenementSante evenement = null;

        if (traitement.getId() != null) {
            TraitementSante existant = traitementRepository.findById(traitement.getId()).orElse(null);
            if (existant != null) {
                evenement = existant.getEvenementSante();
            }
        }

        if (evenement == null) {
            evenement = new EvenementSante();
        }

        Vache vache = vacheRepository.findById(traitement.getVacheId())
                .orElseThrow(() -> new IllegalArgumentException("Vache introuvable"));
        Maladie maladie = maladieRepository.findById(traitement.getMaladieId())
                .orElseThrow(() -> new IllegalArgumentException("Maladie introuvable"));
        Medicament medicament = medicamentRepository.findById(traitement.getMedicamentId())
                .orElseThrow(() -> new IllegalArgumentException("Medicament introuvable"));

        evenement.setVache(vache);
        evenement.setMaladie(maladie);
        evenement.setDateEvenement(traitement.getDateDebut());
        if (evenement.getDescription() == null || evenement.getDescription().isBlank()) {
            evenement.setDescription(maladie.getNom());
        }
        evenement = evenementRepository.save(evenement);

        traitement.setEvenementSante(evenement);
        traitement.setMedicament(medicament);
        traitement.setDateFin(calculerDateFin(traitement.getDateDebut(), traitement.getDureeTraitement()));

        TraitementSante saved = traitementRepository.save(traitement);
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
