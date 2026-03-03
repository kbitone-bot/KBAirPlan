package com.flightplan.demo.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class MonthlySummaryResponse {
    private String month;
    private Long baseId;
    private String baseName;
    private SummaryData summary;
    private List<PersonStats> personStats;
    private List<AircraftStats> aircraftStats;
    private List<DailyStats> dailyStats;
    
    @Data
    @Builder
    public static class SummaryData {
        private int totalFlights;
        private int totalPersonnel;
        private int totalAircraft;
        private double avgPersonsPerFlight;
        private Map<String, Integer> missionsByType;
    }
    
    @Data
    @Builder
    public static class PersonStats {
        private Long personId;
        private String name;
        private String role;
        private int flightCount;
        private int flightHours;
    }
    
    @Data
    @Builder
    public static class AircraftStats {
        private Long aircraftId;
        private String model;
        private String tailNumber;
        private int flightCount;
        private int flightHours;
    }
    
    @Data
    @Builder
    public static class DailyStats {
        private String date;
        private int flightCount;
        private int personnelCount;
    }
}
