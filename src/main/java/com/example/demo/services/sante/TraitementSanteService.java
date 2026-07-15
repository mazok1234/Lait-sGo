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
import com.example.demo.entity.sante.MedicamentFille;
import com.example.demo.entity.sante.TraitementSante;
import com.example.demo.repository.cheptel.VacheRepository;
import com.example.demo.repository.sante.EvenementSanteRepository;
import com.example.demo.repository.sante.MaladieRepository;
import com.example.demo.repository.sante.MedicamentFilleRepository;
import com.example.demo.repository.sante.MedicamentRepository;
import com.example.demo.repository.sante.TraitementSanteRepository;
import com.example.demo.services.alerte.AlerteService;
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
    private final MedicamentFilleRepository medicamentFilleRepository;
    private final StatutLactationVacheService statutLactationService;
    private final AlerteService alerteService;

    public TraitementSanteService(TraitementSanteRepository traitementRepository,
            EvenementSanteRepository evenementRepository,
            VacheRepository vacheRepository,
            MaladieRepository maladieRepository,
            MedicamentRepository medicamentRepository,
            MedicamentFilleRepository medicamentFilleRepository,
            StatutLactationVacheService statutLactationService,
            AlerteService alerteService) {
        this.traitementRepository = traitementRepository;
        this.evenementRepository = evenementRepository;
        this.vacheRepository = vacheRepository;
        this.maladieRepository = maladieRepository;
        this.medicamentRepository = medicamentRepository;
        this.medicamentFilleRepository = medicamentFilleRepository;
        this.statutLactationService = statutLactationService;
        this.alerteService = alerteService;
    }

    public List<TraitementSante> findAll() {
        return traitementRepository.findAllByOrderByDateDebutDesc();
    }

    public long countVachesTariees() {
        return statutLactationService
                .findVacheIdsByStatut(statutLactationService.getByLibelle(STATUT_TARIE).getId())
                .size();
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

    public List<Medicament> findMedicamentsByMaladie(Long maladieId) {
        if (maladieId == null) {
            return List.of();
        }
        return medicamentRepository.findByMaladieId(maladieId);
    }

    public List<MedicamentFille> findFillesByMedicament(Long medicamentId) {
        if (medicamentId == null) {
            return List.of();
        }
        return medicamentFilleRepository.findByMedicamentId(medicamentId);
    }

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
            if (t.getMedicamentFille() != null) {
                ligne.setMedicamentId(t.getMedicamentFille().getMedicament().getId());
                ligne.setMedicamentFilleId(t.getMedicamentFille().getId());
                ligne.setDose(t.getMedicamentFille().getDose());
                ligne.setUnite(t.getMedicamentFille().getUnite());
                ligne.setPrixUnitaire(t.getMedicamentFille().getPrixUnitaire());
            }
            ligne.setNbrMedicament(t.getNbrMedicament());
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
                        : form.getDescription());
        evenement = evenementRepository.save(evenement);

        boolean auMoinsUneLigneValide = false;

        for (TraitementLigneDTO ligne : form.getLignes()) {
            if (isBlankLine(ligne)) {
                continue;
            }
            auMoinsUneLigneValide = true;

            if (ligne.getMedicamentId() == null) {
                throw new IllegalArgumentException("Le medicament est obligatoire");
            }
            if (ligne.getDose() == null) {
                throw new IllegalArgumentException("La dose est obligatoire");
            }
            if (ligne.getUnite() == null || ligne.getUnite().isBlank()) {
                throw new IllegalArgumentException("L'unite est obligatoire");
            }
            if (ligne.getPrixUnitaire() == null) {
                throw new IllegalArgumentException("Le prix unitaire est obligatoire");
            }
            if (ligne.getNbrMedicament() == null || ligne.getNbrMedicament() < 1) {
                throw new IllegalArgumentException("Le nombre de medicaments doit etre au moins 1");
            }
            if (ligne.getDureeTraitement() == null || ligne.getDureeTraitement() < 1) {
                throw new IllegalArgumentException("La duree de traitement doit etre au moins 1 jour");
            }
            if (ligne.getDateDebut() == null) {
                throw new IllegalArgumentException("La date de debut est obligatoire");
            }

            Medicament medicament = medicamentRepository.findById(ligne.getMedicamentId())
                    .orElseThrow(() -> new IllegalArgumentException("Medicament introuvable"));

            MedicamentFille medicamentFille = (ligne.getMedicamentFilleId() != null)
                    ? medicamentFilleRepository.findById(ligne.getMedicamentFilleId()).orElse(new MedicamentFille())
                    : new MedicamentFille();

            medicamentFille.setMedicament(medicament);
            medicamentFille.setDose(ligne.getDose());
            medicamentFille.setUnite(ligne.getUnite().trim());
            medicamentFille.setPrixUnitaire(ligne.getPrixUnitaire());
            medicamentFille = medicamentFilleRepository.save(medicamentFille);

            TraitementSante t = (ligne.getId() != null)
                    ? traitementRepository.findById(ligne.getId()).orElse(new TraitementSante())
                    : new TraitementSante();

            t.setEvenementSante(evenement);
            t.setMedicamentFille(medicamentFille);
            t.setNbrMedicament(ligne.getNbrMedicament());
            t.setDureeTraitement(ligne.getDureeTraitement());
            Integer delaiAttente = ligne.getDelaiAttenteJ();
            if (delaiAttente == null) {
                Integer delaiDefaut = medicamentFille.getDelaiAttenteLaitDefaut();
                delaiAttente = delaiDefaut != null ? delaiDefaut : 0;
            }
            t.setDelaiAttenteJ(delaiAttente);
            t.setDateDebut(ligne.getDateDebut());
            t.setDateFin(calculerDateFinTraitement(ligne.getDateDebut(), ligne.getDureeTraitement(), delaiAttente));

            traitementRepository.save(t);
        }

        if (!auMoinsUneLigneValide) {
            throw new IllegalArgumentException("Au moins un medicament doit etre renseigne");
        }

        synchronizeVacheStatuses();
    }

    private boolean isBlankLine(TraitementLigneDTO ligne) {
        return ligne.getMedicamentId() == null
                && ligne.getDose() == null
                && (ligne.getUnite() == null || ligne.getUnite().isBlank())
                && ligne.getPrixUnitaire() == null;
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
                statutLactationService.changerStatut(vache, statutLactationService.getByLibelle(STATUT_TARIE).getId(),
                        today);
            } else if (!doitEtreTariee && estDejaTariee) {
                statutLactationService.changerStatut(vache,
                        statutLactationService.getByLibelle(STATUT_EN_LACTATION).getId(), today);
            }
        }
    }

    private LocalDate calculerDateFinTraitement(LocalDate dateDebut, Integer dureeTraitement, Integer delaiAttenteJ) {
        if (dateDebut == null || dureeTraitement == null || dureeTraitement < 1) {
            return null;
        }
        int delai = delaiAttenteJ != null && delaiAttenteJ > 0 ? delaiAttenteJ : 0;
        return dateDebut.plusDays(dureeTraitement.longValue() - 1L + delai);
    }

    private boolean isTreatmentActif(TraitementSante traitement, LocalDate today) {
        if (traitement == null || traitement.getDateDebut() == null) {
            return false;
        }
        LocalDate dateFinTraitement = traitement.getDateFin();
        if (dateFinTraitement == null) {
            dateFinTraitement = calculerDateFinTraitement(traitement.getDateDebut(), traitement.getDureeTraitement(),
                    traitement.getDelaiAttenteJ());
        }
        if (dateFinTraitement == null) {
            return false;
        }
        return !today.isBefore(traitement.getDateDebut()) && !today.isAfter(dateFinTraitement);
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
        evenementRepository.delete(evenement);
        synchronizeVacheStatuses();
    }

    public void genererAlertesTraitementsActifs() {
        LocalDate today = LocalDate.now();
        traitementRepository.findAllByOrderByDateDebutDesc().stream()
                .filter(traitement -> isTreatmentActif(traitement, today))
                .map(traitement -> traitement.getEvenementSante() != null ? traitement.getEvenementSante().getVache()
                        : null)
                .filter(vache -> vache != null && vache.getId() != null)
                .distinct()
                .forEach(vache -> {
                    alerteService.envoyerAlerte(
                            "traitement_en_cours",
                            "attention",
                            "Traitement en cours — " + vache.getNumeroBoucle(),
                            "Vache en traitement actif. Vérifier le dossier santé.",
                            vache.getId());
                });
    }
}