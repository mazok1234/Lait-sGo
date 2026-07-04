package com.example.demo.repository.alimentation;

import com.example.demo.entity.alimentation.AffectationRationVache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AffectationRationVacheRepository extends JpaRepository<AffectationRationVache, Long> {
    List<AffectationRationVache> findAllByOrderByDateDebutDescIdDesc();
}
