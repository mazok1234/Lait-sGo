package com.example.demo.repository;

import com.example.demo.entity.Vache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface VacheRepository extends JpaRepository<Vache, Long> {
    Optional<Vache> findByNumeroBoucle(String numeroBoucle);

    long countByStatut_Code(String code);
}