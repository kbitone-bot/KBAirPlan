package com.flightplan.demo.repository;

import com.flightplan.demo.entity.FlightPlanItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface FlightPlanItemRepository extends JpaRepository<FlightPlanItem, Long> {
    
    List<FlightPlanItem> findByDraftId(Long draftId);
    
    @Query("SELECT f FROM FlightPlanItem f WHERE f.draft.id = :draftId AND f.flightDate = :date")
    List<FlightPlanItem> findByDraftIdAndDate(@Param("draftId") Long draftId, @Param("date") LocalDate date);
    
    @Query("SELECT f FROM FlightPlanItem f WHERE f.draft.base.id = :baseId AND f.flightDate BETWEEN :startDate AND :endDate")
    List<FlightPlanItem> findByBaseIdAndDateRange(
            @Param("baseId") Long baseId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    @Query("SELECT f FROM FlightPlanItem f JOIN f.assignedPersonIds personId WHERE personId = :personId AND f.flightDate = :date")
    List<FlightPlanItem> findByPersonIdAndDate(@Param("personId") Long personId, @Param("date") LocalDate date);
}
