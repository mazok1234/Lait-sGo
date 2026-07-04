package com.example.demo.repository.production;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.production.Lactation;

public interface LactationRepository extends JpaRepository<Lactation, Long> {
    Optional<Lactation> findFirstByVache_IdAndStatut_CodeOrderByDateDebutDesc(Long vacheId, String statutCode);

    long countByVache_Id(Long vacheId);
}
