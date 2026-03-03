package com.flightplan.demo.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class WeightConfigResponse {
    private Long id;
    private String name;
    private Double fairnessWeight;
    private Double skillWeight;
    private Double fatigueWeight;
    private Double continuityWeight;
    private Double baseBalanceWeight;
    private Boolean isDefault;
    private LocalDateTime createdAt;
}
