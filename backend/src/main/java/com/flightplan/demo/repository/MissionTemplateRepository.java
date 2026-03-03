package com.flightplan.demo.repository;

import com.flightplan.demo.entity.Aircraft;
import com.flightplan.demo.entity.MissionTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MissionTemplateRepository extends JpaRepository<MissionTemplate, Long> {
    
    List<MissionTemplate> findByAircraftType(Aircraft.Type aircraftType);
}
