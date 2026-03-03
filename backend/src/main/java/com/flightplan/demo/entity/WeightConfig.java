package com.flightplan.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "weight_configs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeightConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private Double fairnessWeight;
    
    @Column(nullable = false)
    private Double skillWeight;
    
    @Column(nullable = false)
    private Double fatigueWeight;
    
    @Column(nullable = false)
    private Double continuityWeight;
    
    @Column(nullable = false)
    private Double baseBalanceWeight;
    
    @Column
    private Boolean isDefault;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    public static WeightConfig getDefault() {
        return WeightConfig.builder()
                .name("기본 설정")
                .fairnessWeight(1.0)
                .skillWeight(1.0)
                .fatigueWeight(1.0)
                .continuityWeight(0.5)
                .baseBalanceWeight(0.5)
                .isDefault(true)
                .build();
    }
}
