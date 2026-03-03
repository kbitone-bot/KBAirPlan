package com.flightplan.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mission_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissionTemplate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Aircraft.Type aircraftType;
    
    @Column(nullable = false)
    private String missionName;
    
    @Column(nullable = false)
    private Integer requiredPilotCount;
    
    @Column(nullable = false)
    private Integer requiredCrewCount;
    
    @Column
    private Integer durationMinutes;
    
    @Column
    private String description;
    
    @ElementCollection
    @CollectionTable(name = "mission_required_qualifications", joinColumns = @JoinColumn(name = "mission_template_id"))
    @Column(name = "qualification_id")
    private List<Long> requiredQualificationIds = new ArrayList<>();
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
