package com.flightplan.demo.dto;

import com.flightplan.demo.entity.Aircraft;
import com.flightplan.demo.entity.FlightPlanDraft;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class GenerateDraftRequest {
    private Long baseId;
    private FlightPlanDraft.PeriodType periodType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Aircraft.Type aircraftType;
    private List<Long> missionTemplateIds;
    private Long weightConfigId;
    private Integer flightsPerDay;
}
