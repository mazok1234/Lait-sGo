package com.example.demo.repository.cheptel;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.cheptel.Vache;

@Repository
public interface VacheRepository extends JpaRepository<Vache, Long>, JpaSpecificationExecutor<Vache> {
    Optional<Vache> findByNumeroBoucle(String numeroBoucle);

    long countByMereIsNotNull();

    long countByMere_Id(Long mereId);

    @Query("select count(v) from Vache v where "
            + "(v.scoreLocomotion is not null and v.scoreLocomotion < 3) "
            + "or (v.scoreBcs is not null and (v.scoreBcs < :bcsMin or v.scoreBcs > :bcsMax))")
    long countASurveiller(@Param("bcsMin") BigDecimal bcsMin, @Param("bcsMax") BigDecimal bcsMax);
}
