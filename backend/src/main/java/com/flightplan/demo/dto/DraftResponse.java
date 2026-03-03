package com.flightplan.demo.dto;

import com.flightplan.demo.entity.FlightPlanDraft;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class DraftResponse {
    private Long id;
    private Long baseId;
    private String baseName;
    private FlightPlanDraft.PeriodType periodType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long weightConfigId;
    private String weightConfigName;
    private FlightPlanDraft.Status status;
    private Double totalScore;
    private List<FlightPlanItemResponse> items;
    private ScoreSummary scoreSummary;
    private List<Violation> violations;
    private LocalDateTime createdAt;
    
    @Data
    @Builder
    public static class ScoreSummary {
        private int totalFlights;
        private int assignedFlights;
        private int hardViolations;
        private int softViolations;
        private double averageScore;
        private double fairnessScore;
        private double skillScore;
        private double fatigueScore;
    }
    
    @Data
    @Builder
    public static class Violation {
        private Long itemId;
        private String type;
        private String severity;
        private String message;
    }
}
