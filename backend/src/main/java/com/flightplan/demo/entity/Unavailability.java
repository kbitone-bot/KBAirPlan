package com.flightplan.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "unavailabilities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Unavailability {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Type type;
    
    @Column(nullable = false)
    private LocalDate startDate;
    
    @Column
    private LocalTime startTime;
    
    @Column(nullable = false)
    private LocalDate endDate;
    
    @Column
    private LocalTime endTime;
    
    @Column
    private String reason;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    public enum Type {
        LEAVE, TRAINING, SICK, MISSION, OTHER
    }
    
    public boolean overlaps(LocalDate date, LocalTime start, LocalTime end) {
        if (date.isBefore(startDate) || date.isAfter(endDate)) {
            return false;
        }
        if (date.isEqual(startDate) && startTime != null && start.isAfter(endTime)) {
            return false;
        }
        if (date.isEqual(endDate) && endTime != null && end.isBefore(endTime)) {
            return false;
        }
        return true;
    }
    
    public boolean isUnavailableAt(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }
}
