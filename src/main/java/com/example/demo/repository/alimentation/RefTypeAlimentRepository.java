package com.example.demo.repository.alimentation;

import com.example.demo.entity.alimentation.RefTypeAliment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefTypeAlimentRepository extends JpaRepository<RefTypeAliment, Integer> {
}
