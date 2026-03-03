package com.flightplan.demo.dto;

import com.flightplan.demo.entity.Aircraft;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class MissionTemplateDto {
    private Long id;
    private Aircraft.Type aircraftType;
    private String missionName;
    private Integer requiredPilotCount;
    private Integer requiredCrewCount;
    private Integer durationMinutes;
    private String description;
    private List<Long> requiredQualificationIds;
    private LocalDateTime createdAt;
}
