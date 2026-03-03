package com.flightplan.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "training_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;
    
    @Column(nullable = false)
    private String courseName;
    
    @Column
    private LocalDate completedAt;
    
    @Column
    private LocalDate expiresAt;
    
    @Column
    private String status;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    public boolean isValidAt(LocalDate date) {
        if (expiresAt == null) return true;
        return !date.isAfter(expiresAt);
    }
}
