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
    List<Alerte> findByAcquitteeFalseAndType_CodeOrderByCreatedAtDesc(String typeCode);
    List<Alerte> findByAcquitteeFalseAndType_CodeStartingWithOrderByCreatedAtDesc(String typeCodePrefix);

    // Filtre niveau + type combinés
    List<Alerte> findByAcquitteeFalseAndNiveau_CodeAndType_CodeOrderByCreatedAtDesc(
            String niveauCode, String typeCode);
    List<Alerte> findByAcquitteeFalseAndNiveau_CodeAndType_CodeStartingWithOrderByCreatedAtDesc(
            String niveauCode, String typeCodePrefix);

    // Récupérer alerte active par vache + type (mise à jour de gravité)
    Optional<Alerte> findTopByVacheIdAndType_CodeAndAcquitteeFalseOrderByCreatedAtDesc(
            Long vacheId, String typeCode);
    Optional<Alerte> findTopByVacheIdIsNullAndType_CodeAndAcquitteeFalseOrderByCreatedAtDesc(
            String typeCode);

    // Acquittement automatique
    List<Alerte> findByType_CodeAndVacheIdAndAcquitteeFalse(String typeCode, Long vacheId);
    List<Alerte> findByType_CodeAndAcquitteeFalse(String typeCode);

    // Pour detacherVache
    List<Alerte> findByVacheId(Long vacheId);

    Optional<Alerte> findTopByVacheIdAndType_CodeAndAcquitteeTrueOrderByCreatedAtDesc(
    Long vacheId, String typeCode);

    // Variantes "startingWith" pour gérer les types dynamiques (ex: stock_aliment_bas_123)
    Optional<Alerte> findTopByVacheIdAndType_CodeStartingWithAndAcquitteeFalseOrderByCreatedAtDesc(
            Long vacheId, String typeCodePrefix);
    Optional<Alerte> findTopByVacheIdIsNullAndType_CodeStartingWithAndAcquitteeFalseOrderByCreatedAtDesc(
            String typeCodePrefix);

    List<Alerte> findByType_CodeStartingWithAndVacheIdAndAcquitteeFalse(String typeCodePrefix, Long vacheId);
    List<Alerte> findByType_CodeStartingWithAndAcquitteeFalse(String typeCodePrefix);
}
