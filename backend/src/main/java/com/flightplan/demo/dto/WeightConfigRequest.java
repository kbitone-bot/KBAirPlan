package com.flightplan.demo.dto;

import lombok.Data;

@Data
public class WeightConfigRequest {
    private String name;
    private Double fairnessWeight;
    private Double skillWeight;
    private Double fatigueWeight;
    private Double continuityWeight;
    private Double baseBalanceWeight;
    private Boolean isDefault;
}
