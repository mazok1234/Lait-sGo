package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Lactation;

public interface LactationRepository extends JpaRepository<Lactation, Long> {
}