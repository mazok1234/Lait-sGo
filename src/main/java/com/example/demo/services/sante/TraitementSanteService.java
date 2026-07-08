package com.example.demo.services.sante;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.EvenementSanteFormDTO;
import com.example.demo.dto.TraitementLigneDTO;
import com.example.demo.entity.cheptel.RefStatutLactationVache;
import com.example.demo.entity.cheptel.Vache;
import com.example.demo.entity.sante.EvenementSante;
import com.example.demo.entity.sante.Maladie;
import com.example.demo.entity.sante.Medicament;
import com.example.demo.entity.sante.TraitementSante;
import com.example.demo.repository.cheptel.VacheRepository;
import com.example.demo.repository.sante.EvenementSanteRepository;
import com.example.demo.repository.sante.MaladieRepository;
import com.example.demo.repository.sante.MedicamentRepository;
import com.example.demo.repository.sante.TraitementSanteRepository;
import com.example.demo.services.cheptel.StatutLactationVacheService;

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
        return statutLactationService
                .findVacheIdsByStatut(statutLactationService.getByLibelle(STATUT_TARIE).getId())
                .size();
    }

    public List<Vache> findAllVaches() { return vacheRepository.findAll(); }
    public List<Maladie> findAllMaladies() { return maladieRepository.findAll(); }
    public List<Medicament> findAllMedicaments() { return medicamentRepository.findAll(); }

    /** Prépare un formulaire vierge avec une ligne de médicament par défaut. */
    public EvenementSanteFormDTO createEmptyForm() {
        EvenementSanteFormDTO form = new EvenementSanteFormDTO();
        form.setDateEvenement(LocalDate.now());
        TraitementLigneDTO ligne = new TraitementLigneDTO();
        ligne.setDateDebut(LocalDate.now());
        ligne.setDelaiAttenteJ(0);
        ligne.setNbrMedicament(1);
        form.getLignes().add(ligne);
        return form;
    }

    /** Charge un événement existant + ses lignes de traitement pour l'édition. */
    public EvenementSanteFormDTO findFormById(Long evenementId) {
        EvenementSante evenement = evenementRepository.findById(evenementId).orElse(null);
        if (evenement == null) {
            return null;
        }

        EvenementSanteFormDTO form = new EvenementSanteFormDTO();
        form.setId(evenement.getId());
        form.setVacheId(evenement.getVache().getId());
        form.setMaladieId(evenement.getMaladie().getId());
        form.setDateEvenement(evenement.getDateEvenement());
        form.setDescription(evenement.getDescription());

        List<TraitementSante> lignesExistantes = traitementRepository.findByEvenementSanteId(evenementId);
        for (TraitementSante t : lignesExistantes) {
            TraitementLigneDTO ligne = new TraitementLigneDTO();
            ligne.setId(t.getId());
            ligne.setMedicamentId(t.getMedicament().getId());
            ligne.setNbrMedicament(t.getNbrMedicament());
            ligne.setDose(t.getDose());
            ligne.setUnite(t.getUnite());
            ligne.setDureeTraitement(t.getDureeTraitement());
            ligne.setDelaiAttenteJ(t.getDelaiAttenteJ());
            ligne.setDateDebut(t.getDateDebut());
            form.getLignes().add(ligne);
        }

        if (form.getLignes().isEmpty()) {
            form.getLignes().add(new TraitementLigneDTO());
        }

        return form;
    }

    @Transactional
    public void saveForm(EvenementSanteFormDTO form) {
        EvenementSante evenement = (form.getId() != null)
                ? evenementRepository.findById(form.getId()).orElse(new EvenementSante())
                : new EvenementSante();

        Vache vache = vacheRepository.findById(form.getVacheId())
                .orElseThrow(() -> new IllegalArgumentException("Vache introuvable"));
        Maladie maladie = maladieRepository.findById(form.getMaladieId())
                .orElseThrow(() -> new IllegalArgumentException("Maladie introuvable"));

        evenement.setVache(vache);
        evenement.setMaladie(maladie);
        evenement.setDateEvenement(form.getDateEvenement());
        evenement.setDescription(
                (form.getDescription() == null || form.getDescription().isBlank())
                        ? maladie.getNom()
                        : form.getDescription()
        );
        evenement = evenementRepository.save(evenement);

        boolean auMoinsUneLigneValide = false;

        for (TraitementLigneDTO ligne : form.getLignes()) {
            if (ligne.getMedicamentId() == null) {
                continue; // ligne vide ignorée
            }
            auMoinsUneLigneValide = true;

            Medicament medicament = medicamentRepository.findById(ligne.getMedicamentId())
                    .orElseThrow(() -> new IllegalArgumentException("Medicament introuvable"));

            if (ligne.getNbrMedicament() == null || ligne.getNbrMedicament() < 1) {
                throw new IllegalArgumentException("Le nombre de medicaments doit etre au moins 1");
            }
            if (ligne.getDureeTraitement() == null || ligne.getDureeTraitement() < 1) {
                throw new IllegalArgumentException("La duree de traitement doit etre au moins 1 jour");
            }

            TraitementSante t = (ligne.getId() != null)
                    ? traitementRepository.findById(ligne.getId()).orElse(new TraitementSante())
                    : new TraitementSante();

            t.setEvenementSante(evenement);
            t.setMedicament(medicament);
            t.setNbrMedicament(ligne.getNbrMedicament());
            t.setDose(ligne.getDose());
            t.setUnite(ligne.getUnite());
            t.setDureeTraitement(ligne.getDureeTraitement());
            t.setDelaiAttenteJ(ligne.getDelaiAttenteJ() != null ? ligne.getDelaiAttenteJ() : 0);
            t.setDateDebut(ligne.getDateDebut());
            t.setDateFin(calculerDateFin(ligne.getDateDebut(), ligne.getDureeTraitement()));

            traitementRepository.save(t);
        }

        if (!auMoinsUneLigneValide) {
            throw new IllegalArgumentException("Au moins un medicament doit etre renseigne");
        }

        synchronizeVacheStatuses();
    }

    @Transactional
    public void deleteById(Long id) {
        TraitementSante traitement = traitementRepository.findById(id).orElse(null);
        if (traitement == null) {
            return;
        }
        traitementRepository.delete(traitement);
        synchronizeVacheStatuses();
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

    public List<EvenementSante> findAllEvenements() {
    return evenementRepository.findAllByOrderByDateEvenementDesc();
    }

    public EvenementSante findEvenementById(Long id) {
    return evenementRepository.findById(id).orElse(null);
    }

    @Transactional
    public void deleteEvenement(Long evenementId) {
    EvenementSante evenement = evenementRepository.findById(evenementId).orElse(null);
    if (evenement == null) {
        return;
    }
    evenementRepository.delete(evenement); // cascade + orphanRemoval supprime les traitements liés
    synchronizeVacheStatuses();
    }
}