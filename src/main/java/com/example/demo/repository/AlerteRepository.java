package com.example.demo.repository;

import com.example.demo.entity.Alerte;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AlerteRepository extends JpaRepository<Alerte, Long> {

    List<Alerte> findByAcquitteeFalseOrderByCreatedAtDesc();

    List<Alerte> findByAcquitteeFalseAndNiveau_CodeOrderByCreatedAtDesc(String niveauCode);

    List<Alerte> findByAcquitteeFalseAndType_IdOrderByCreatedAtDesc(Integer typeId);

    List<Alerte> findByAcquitteeFalseAndNiveau_CodeAndType_IdOrderByCreatedAtDesc(
            String niveauCode, Integer typeId);

    // Vérifier si une alerte non acquittée du même type existe déjà pour cette
    // vache
    boolean existsByVacheIdAndType_CodeAndAcquitteeFalse(Long vacheId, String typeCode);

    // Pour les alertes globales (vache_id null) — ex: stock
    boolean existsByVacheIdIsNullAndType_CodeAndAcquitteeFalse(String typeCode);
}