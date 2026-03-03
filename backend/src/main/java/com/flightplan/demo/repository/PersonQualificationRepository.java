package com.flightplan.demo.repository;

import com.flightplan.demo.entity.PersonQualification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonQualificationRepository extends JpaRepository<PersonQualification, Long> {
    
    List<PersonQualification> findByPersonId(Long personId);
    
    List<PersonQualification> findByPersonIdIn(List<Long> personIds);
    
    List<PersonQualification> findByQualificationId(Long qualificationId);
    
    boolean existsByPersonIdAndQualificationId(Long personId, Long qualificationId);
}
