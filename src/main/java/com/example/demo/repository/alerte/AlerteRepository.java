package com.example.demo.repository.alerte;

import com.example.demo.entity.alerte.Alerte;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AlerteRepository extends JpaRepository<Alerte, Long> {

    List<Alerte> findByAcquitteeFalseOrderByCreatedAtDesc();

    List<Alerte> findByAcquitteeTrueOrderByCreatedAtDesc();

    List<Alerte> findByAcquitteeFalseAndNiveau_CodeOrderByCreatedAtDesc(String niveauCode);

    List<Alerte> findByAcquitteeFalseAndTypeAlerteOrderByCreatedAtDesc(String typeAlerte);
    List<Alerte> findByAcquitteeFalseAndTypeAlerteStartingWithOrderByCreatedAtDesc(String typeAlertePrefix);

    List<Alerte> findByAcquitteeFalseAndNiveau_CodeAndTypeAlerteOrderByCreatedAtDesc(
            String niveauCode, String typeAlerte);
    List<Alerte> findByAcquitteeFalseAndNiveau_CodeAndTypeAlerteStartingWithOrderByCreatedAtDesc(
            String niveauCode, String typeAlertePrefix);

    Optional<Alerte> findTopByVacheIdAndTypeAlerteAndAcquitteeFalseOrderByCreatedAtDesc(
            Long vacheId, String typeAlerte);
    Optional<Alerte> findTopByVacheIdIsNullAndTypeAlerteAndAcquitteeFalseOrderByCreatedAtDesc(
            String typeAlerte);

    List<Alerte> findByTypeAlerteAndVacheIdAndAcquitteeFalse(String typeAlerte, Long vacheId);
    List<Alerte> findByTypeAlerteAndAcquitteeFalse(String typeAlerte);

    List<Alerte> findByVacheId(Long vacheId);

    Optional<Alerte> findTopByVacheIdAndTypeAlerteAndAcquitteeTrueOrderByCreatedAtDesc(
            Long vacheId, String typeAlerte);
    Optional<Alerte> findTopByVacheIdIsNullAndTypeAlerteAndAcquitteeTrueOrderByCreatedAtDesc(
            String typeAlerte);

    Optional<Alerte> findTopByVacheIdAndTypeAlerteStartingWithAndAcquitteeTrueOrderByCreatedAtDesc(
            Long vacheId, String typeAlertePrefix);
    Optional<Alerte> findTopByVacheIdIsNullAndTypeAlerteStartingWithAndAcquitteeTrueOrderByCreatedAtDesc(
            String typeAlertePrefix);

    Optional<Alerte> findTopByVacheIdAndTypeAlerteStartingWithAndAcquitteeFalseOrderByCreatedAtDesc(
            Long vacheId, String typeAlertePrefix);
    Optional<Alerte> findTopByVacheIdIsNullAndTypeAlerteStartingWithAndAcquitteeFalseOrderByCreatedAtDesc(
            String typeAlertePrefix);

    List<Alerte> findByTypeAlerteStartingWithAndVacheIdAndAcquitteeFalse(String typeAlertePrefix, Long vacheId);
    List<Alerte> findByTypeAlerteStartingWithAndAcquitteeFalse(String typeAlertePrefix);
}
