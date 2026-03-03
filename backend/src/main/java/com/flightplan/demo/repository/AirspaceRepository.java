package com.flightplan.demo.repository;

import com.flightplan.demo.entity.Airspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AirspaceRepository extends JpaRepository<Airspace, Long> {
    
    List<Airspace> findByBaseId(Long baseId);
}
