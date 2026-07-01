package com.example.demo.repository;

import com.example.demo.entity.RationAliment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RationAlimentRepository extends JpaRepository<RationAliment, Long> {
    List<RationAliment> findByRationIdOrderByIdAsc(Long rationId);
}
