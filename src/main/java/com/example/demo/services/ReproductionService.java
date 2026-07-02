package com.example.demo.services;

import com.example.demo.dto.ReproductionDTO;
import com.example.demo.entity.Reproduction;
import com.example.demo.entity.Vache;
import com.example.demo.repository.ReproductionRepository;
import com.example.demo.repository.VacheRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReproductionService {

    @Autowired
    private ReproductionRepository reproductionRepository;

    @Autowired
    private VacheRepository vacheRepository;

    /**
     * Enregistrer une nouvelle insémination artificielle
     */
    @Transactional
    public ReproductionDTO enregistrerIA(ReproductionDTO dto) {
        // Récupérer la vache
        Vache vache = vacheRepository.findById(dto.getVacheId())
                .orElseThrow(() -> new RuntimeException("Vache non trouvée"));

        // Créer l'entité Reproduction
        Reproduction reproduction = new Reproduction();
        reproduction.setVache(vache);
        reproduction.setDateIA(dto.getDateIA());
        reproduction.setSemence(dto.getSemence());
        reproduction.setInséminateur(dto.getInséminateur());
        reproduction.setTypeInjection(dto.getTypeInjection());
        reproduction.setStatutIA("en_attente");

        // Sauvegarder
        Reproduction saved = reproductionRepository.save(reproduction);

        return convertToDTO(saved);
    }

    /**
     * Récupérer l'historique des IA d'une vache
     */
    public List<ReproductionDTO> getHistoriqueParVache(Long vacheId) {
        Vache vache = vacheRepository.findById(vacheId)
                .orElseThrow(() -> new RuntimeException("Vache non trouvée"));

        List<Reproduction> reproductions = reproductionRepository.findByVacheOrderByDateIADesc(vache);
        return reproductions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Mettre à jour le statut d'une IA
     */
    @Transactional
    public ReproductionDTO updateStatutIA(Long reproductionId, String statut, LocalDate dateConfirmation) {
        Reproduction reproduction = reproductionRepository.findById(reproductionId)
                .orElseThrow(() -> new RuntimeException("IA non trouvée"));

        reproduction.setStatutIA(statut);

        // Si statut "gestante", enregistrer la date de confirmation
        if ("gestante".equals(statut)) {
            reproduction.setGestationConfirmee(true);
            reproduction.setDateConfirmationGest(dateConfirmation != null ? dateConfirmation : LocalDate.now());
        }

        // Si statut "échouée", réinitialiser la gestation
        if ("echouee".equals(statut)) {
            reproduction.setGestationConfirmee(false);
            reproduction.setDateConfirmationGest(null);
        }

        Reproduction updated = reproductionRepository.save(reproduction);
        return convertToDTO(updated);
    }

    /**
     * Récupérer une IA par ID
     */
    public ReproductionDTO getReproductionById(Long id) {
        Reproduction reproduction = reproductionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("IA non trouvée"));
        return convertToDTO(reproduction);
    }

    /**
     * Récupérer toutes les IA en attente
     */
    public List<ReproductionDTO> getIAEnAttente() {
        List<Reproduction> reproductions = reproductionRepository.findByStatutIA("en_attente");
        return reproductions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Supprimer une IA (logique métier)
     */
    @Transactional
    public void deleteIA(Long reproductionId) {
        reproductionRepository.deleteById(reproductionId);
    }

    /**
     * Convertir Reproduction en DTO
     */
    private ReproductionDTO convertToDTO(Reproduction reproduction) {
        ReproductionDTO dto = new ReproductionDTO();
        dto.setId(reproduction.getId());
        dto.setVacheId(reproduction.getVache().getId());
        dto.setNumeroBoucleVache(reproduction.getVache().getNumeroBoucle());
        dto.setDateIA(reproduction.getDateIA());
        dto.setSemence(reproduction.getSemence());
        dto.setInséminateur(reproduction.getInséminateur());
        dto.setTypeInjection(reproduction.getTypeInjection());
        dto.setStatutIA(reproduction.getStatutIA());
        dto.setGestationConfirmee(reproduction.getGestationConfirmee());
        dto.setDateConfirmationGest(reproduction.getDateConfirmationGest());
        dto.setDateVelageReel(reproduction.getDateVelageReel());
        dto.setSexeVeau(reproduction.getSexeVeau());
        return dto;
    }

}
