package com.flightplan.demo.repository;

import com.flightplan.demo.entity.Unavailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface UnavailabilityRepository extends JpaRepository<Unavailability, Long> {
    
    List<Unavailability> findByPersonId(Long personId);
    
    @Query("SELECT u FROM Unavailability u WHERE u.person.id = :personId AND u.startDate <= :date AND u.endDate >= :date")
    List<Unavailability> findByPersonIdAndDate(@Param("personId") Long personId, @Param("date") LocalDate date);
    
    @Query("SELECT u FROM Unavailability u WHERE u.person.id IN :personIds AND u.startDate <= :endDate AND u.endDate >= :startDate")
    List<Unavailability> findByPersonIdsAndDateRange(
            @Param("personIds") List<Long> personIds,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
