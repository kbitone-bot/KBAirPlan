package com.flightplan.demo.algorithm;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AssignmentScore {
    private double skillScore;
    private double fairnessScore;
    private double fatiguePenalty;
    private double continuityPenalty;
    private double baseBalanceScore;
    private double totalScore;
    private String explanation;
    
    public static AssignmentScore zero() {
        return AssignmentScore.builder()
                .skillScore(0)
                .fairnessScore(0)
                .fatiguePenalty(0)
                .continuityPenalty(0)
                .baseBalanceScore(0)
                .totalScore(0)
                .explanation("배정되지 않음")
                .build();
    }
}
