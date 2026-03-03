package com.flightplan.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "person_qualifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonQualification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "qualification_id", nullable = false)
    private Qualification qualification;
    
    @Column
    private LocalDate validFrom;
    
    @Column
    private LocalDate validTo;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    public boolean isValidAt(LocalDate date) {
        if (validFrom == null || validTo == null) return true;
        return !date.isBefore(validFrom) && !date.isAfter(validTo);
    }
}
