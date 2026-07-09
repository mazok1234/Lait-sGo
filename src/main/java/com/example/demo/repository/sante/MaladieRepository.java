package com.example.demo.repository.sante;

import java.util.Optional;

import com.example.demo.entity.sante.Maladie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaladieRepository extends JpaRepository<Maladie, Long> {
	Optional<Maladie> findByNomIgnoreCase(String nom);
}
