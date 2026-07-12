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

    // Filtre par typeAlerte
    List<Alerte> findByAcquitteeFalseAndTypeAlerteOrderByCreatedAtDesc(String typeAlerte);
    List<Alerte> findByAcquitteeFalseAndTypeAlerteStartingWithOrderByCreatedAtDesc(String typeAlertePrefix);

    // Filtre niveau + typeAlerte combinés
    List<Alerte> findByAcquitteeFalseAndNiveau_CodeAndTypeAlerteOrderByCreatedAtDesc(
            String niveauCode, String typeAlerte);
    List<Alerte> findByAcquitteeFalseAndNiveau_CodeAndTypeAlerteStartingWithOrderByCreatedAtDesc(
            String niveauCode, String typeAlertePrefix);

    // Récupérer alerte active par vache + typeAlerte (mise à jour de gravité)
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
    Optional<Alerte> findTopByVacheIdIsNullAndTypeAlerteAndAcquitteeTrueOrderByCreatedAtDesc(
            String typeAlerte);

    Optional<Alerte> findTopByVacheIdAndTypeAlerteStartingWithAndAcquitteeTrueOrderByCreatedAtDesc(
            Long vacheId, String typeAlertePrefix);
    Optional<Alerte> findTopByVacheIdIsNullAndTypeAlerteStartingWithAndAcquitteeTrueOrderByCreatedAtDesc(
            String typeAlertePrefix);

    // Variantes "startingWith" pour gérer les types dynamiques (ex: stock_aliment_bas_123)
    Optional<Alerte> findTopByVacheIdAndTypeAlerteStartingWithAndAcquitteeFalseOrderByCreatedAtDesc(
            Long vacheId, String typeAlertePrefix);
    Optional<Alerte> findTopByVacheIdIsNullAndTypeAlerteStartingWithAndAcquitteeFalseOrderByCreatedAtDesc(
            String typeAlertePrefix);

    List<Alerte> findByTypeAlerteStartingWithAndVacheIdAndAcquitteeFalse(String typeAlertePrefix, Long vacheId);
    List<Alerte> findByTypeAlerteStartingWithAndAcquitteeFalse(String typeAlertePrefix);
}
