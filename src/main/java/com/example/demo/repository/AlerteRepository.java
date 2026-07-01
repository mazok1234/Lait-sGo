package com.example.demo.repository;

import com.example.demo.entity.Alerte;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AlerteRepository extends JpaRepository<Alerte, Long> {

    List<Alerte> findByAcquitteeFalseOrderByCreatedAtDesc();

    List<Alerte> findByAcquitteeFalseAndNiveau_CodeOrderByCreatedAtDesc(String niveauCode);

    List<Alerte> findByAcquitteeFalseAndType_IdOrderByCreatedAtDesc(Integer typeId);

    List<Alerte> findByAcquitteeFalseAndNiveau_CodeAndType_IdOrderByCreatedAtDesc(
        String niveauCode, Integer typeId
    );
}