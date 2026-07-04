package com.example.demo.repository.alimentation;

import com.example.demo.entity.alimentation.RationAliment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RationAlimentRepository extends JpaRepository<RationAliment, Long> {
    List<RationAliment> findByRationIdOrderByIdAsc(Long rationId);

    void deleteByRationId(Long rationId);
}
