package com.example.demo.repository.alerte;

import com.example.demo.entity.alerte.Alerte;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AlerteRepository extends JpaRepository<Alerte, Long> {
    List<Alerte> findByAcquitteeFalseOrderByCreatedAtDesc();

    List<Alerte> findByVacheId(Long vacheId);

    List<Alerte> findByAcquitteeFalseAndNiveau_CodeOrderByCreatedAtDesc(String niveauCode);

    List<Alerte> findByAcquitteeFalseAndType_IdOrderByCreatedAtDesc(Integer typeId);

    List<Alerte> findByAcquitteeFalseAndNiveau_CodeAndType_IdOrderByCreatedAtDesc(
            String niveauCode, Integer typeId);

    boolean existsByVacheIdAndType_CodeAndAcquitteeFalse(Long vacheId, String typeCode);

    boolean existsByVacheIdIsNullAndType_CodeAndAcquitteeFalse(String typeCode);

    Optional<Alerte> findByVacheIdAndType_CodeAndAcquitteeFalse(Long vacheId, String typeCode);

    Optional<Alerte> findByVacheIdIsNullAndType_CodeAndAcquitteeFalse(String typeCode);
}
