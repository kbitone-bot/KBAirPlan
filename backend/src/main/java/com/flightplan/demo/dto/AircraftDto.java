package com.flightplan.demo.dto;

import com.flightplan.demo.entity.Aircraft;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AircraftDto {
    private Long id;
    private Aircraft.Type type;
    private String model;
    private String tailNumber;
    private Long baseId;
    private String baseName;
    private Boolean available;
    private Integer maxCrew;
    private LocalDateTime createdAt;
}
