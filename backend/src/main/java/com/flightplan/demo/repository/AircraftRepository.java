package com.flightplan.demo.repository;

import com.flightplan.demo.entity.Aircraft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AircraftRepository extends JpaRepository<Aircraft, Long> {
    
    List<Aircraft> findByBaseId(Long baseId);
    
    List<Aircraft> findByBaseIdAndType(Long baseId, Aircraft.Type type);
    
    List<Aircraft> findByBaseIdAndAvailable(Long baseId, Boolean available);
    
    List<Aircraft> findByBaseIdAndTypeAndAvailable(Long baseId, Aircraft.Type type, Boolean available);
}
