package com.example.demo.repository.alimentation;

import com.example.demo.entity.alimentation.Aliment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlimentRepository extends JpaRepository<Aliment, Long> {
}
