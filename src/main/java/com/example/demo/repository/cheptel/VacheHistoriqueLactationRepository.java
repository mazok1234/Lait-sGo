package com.example.demo.repository.cheptel;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.cheptel.VacheHistoriqueLactation;

public interface VacheHistoriqueLactationRepository extends JpaRepository<VacheHistoriqueLactation, Long> {
    Optional<VacheHistoriqueLactation> findByVache_IdAndDateFinIsNull(Long vacheId);

    List<VacheHistoriqueLactation> findByStatut_IdAndDateFinIsNull(Integer statutId);

    void deleteByVache_Id(Long vacheId);
}
