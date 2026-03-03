package com.flightplan.demo.dto;

import com.flightplan.demo.entity.FlightPlanItem;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
public class FlightPlanItemResponse {
    private Long id;
    private LocalDate flightDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Long aircraftId;
    private String aircraftModel;
    private String tailNumber;
    private Long missionTemplateId;
    private String missionName;
    private Long airspaceId;
    private String airspaceName;
    private List<AssignedPersonResponse> assignedPersons;
    private String notes;
    private Double score;
    private FlightPlanItem.Status status;
    private List<String> violations;
    private AssignmentExplanation explanation;
    
    @Data
    @Builder
    public static class AssignedPersonResponse {
        private Long personId;
        private String name;
        private String rank;
        private String role;
    }
    
    @Data
    @Builder
    public static class AssignmentExplanation {
        private double skillContribution;
        private double fairnessContribution;
        private double fatiguePenalty;
        private double continuityPenalty;
        private double totalScore;
        private String reason;
    }
}
