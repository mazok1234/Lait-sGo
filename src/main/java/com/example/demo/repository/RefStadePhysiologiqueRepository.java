package com.example.demo.repository;

import com.example.demo.entity.RefStadePhysiologique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RefStadePhysiologiqueRepository extends JpaRepository<RefStadePhysiologique, Integer> {

	@Query(value = """
		SELECT COUNT(*) > 0
		FROM ration r
		WHERE r.id_stade_physiologique = :stadeId
	""", nativeQuery = true)
	boolean isUsedByRation(@Param("stadeId") Integer stadeId);
}
