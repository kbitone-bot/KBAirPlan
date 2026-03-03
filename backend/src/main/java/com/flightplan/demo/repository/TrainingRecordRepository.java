package com.flightplan.demo.repository;

import com.flightplan.demo.entity.TrainingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingRecordRepository extends JpaRepository<TrainingRecord, Long> {
    
    List<TrainingRecord> findByPersonId(Long personId);
}
