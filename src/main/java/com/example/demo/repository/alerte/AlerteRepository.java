package com.example.demo.repository.alerte;

import com.example.demo.entity.alerte.Alerte;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AlerteRepository extends JpaRepository<Alerte, Long> {

    // Dashboard principal — non acquittées
    List<Alerte> findByAcquitteeFalseOrderByCreatedAtDesc();

    // Acquittées — affichées en bas de liste
    List<Alerte> findByAcquitteeTrueOrderByCreatedAtDesc();

    // Filtre par niveau
    List<Alerte> findByAcquitteeFalseAndNiveau_CodeOrderByCreatedAtDesc(String niveauCode);

    // Filtre par type
    List<Alerte> findByAcquitteeFalseAndTypeAlerteOrderByCreatedAtDesc(String typeAlerte);

    // Filtre niveau + type combinés
    List<Alerte> findByAcquitteeFalseAndNiveau_CodeAndTypeAlerteOrderByCreatedAtDesc(
            String niveauCode, String typeAlerte);

    // Récupérer alerte active par vache + type (mise à jour de gravité)
    Optional<Alerte> findTopByVacheIdAndTypeAlerteAndAcquitteeFalseOrderByCreatedAtDesc(
            Long vacheId, String typeAlerte);
    Optional<Alerte> findTopByVacheIdIsNullAndTypeAlerteAndAcquitteeFalseOrderByCreatedAtDesc(
            String typeAlerte);

    // Acquittement automatique
    List<Alerte> findByTypeAlerteAndVacheIdAndAcquitteeFalse(String typeAlerte, Long vacheId);
    List<Alerte> findByTypeAlerteAndAcquitteeFalse(String typeAlerte);

    // Pour detacherVache
    List<Alerte> findByVacheId(Long vacheId);

    Optional<Alerte> findTopByVacheIdAndTypeAlerteAndAcquitteeTrueOrderByCreatedAtDesc(
    Long vacheId, String typeAlerte);
}