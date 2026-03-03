package com.flightplan.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "flight_plan_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightPlanItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "draft_id", nullable = false)
    private FlightPlanDraft draft;
    
    @Column(nullable = false)
    private LocalDate flightDate;
    
    @Column(nullable = false)
    private LocalTime startTime;
    
    @Column(nullable = false)
    private LocalTime endTime;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aircraft_id", nullable = false)
    private Aircraft aircraft;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_template_id", nullable = false)
    private MissionTemplate missionTemplate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "airspace_id")
    private Airspace airspace;
    
    @ElementCollection
    @CollectionTable(name = "flight_plan_assigned_persons", joinColumns = @JoinColumn(name = "flight_plan_item_id"))
    @Column(name = "person_id")
    @Builder.Default
    private List<Long> assignedPersonIds = new ArrayList<>();
    
    @Column
    private String notes;
    
    @Column
    private Double score;
    
    @Column
    @Enumerated(EnumType.STRING)
    private Status status;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    public enum Status {
        SCHEDULED, CONFIRMED, CANCELLED, COMPLETED
    }
}
