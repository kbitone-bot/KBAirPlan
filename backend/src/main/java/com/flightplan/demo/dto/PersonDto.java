package com.flightplan.demo.dto;

import com.flightplan.demo.entity.Person;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PersonDto {
    private Long id;
    private Long baseId;
    private String baseName;
    private Person.Role role;
    private String name;
    private String rank;
    private Person.Status status;
    private Integer totalFlightHours;
    private Integer monthlyFlightCount;
    private List<QualificationDto> qualifications;
    private LocalDateTime createdAt;
    
    @Data
    @Builder
    public static class QualificationDto {
        private Long id;
        private String name;
        private String type;
        private String validFrom;
        private String validTo;
    }
}
