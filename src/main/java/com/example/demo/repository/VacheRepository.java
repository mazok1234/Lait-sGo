package com.example.demo.repository;

import com.example.demo.entity.Vache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VacheRepository extends JpaRepository<Vache, Long> {
}