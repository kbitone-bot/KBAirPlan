package com.flightplan.demo.repository;

import com.flightplan.demo.entity.FlightPlanDraft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FlightPlanDraftRepository extends JpaRepository<FlightPlanDraft, Long> {
    
    List<FlightPlanDraft> findByBaseId(Long baseId);
    
    @Query("SELECT d FROM FlightPlanDraft d WHERE d.base.id = :baseId AND d.startDate <= :endDate AND d.endDate >= :startDate")
    List<FlightPlanDraft> findByBaseIdAndDateRange(
            @Param("baseId") Long baseId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
