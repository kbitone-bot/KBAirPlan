package com.flightplan.demo.dto;

import lombok.Data;

import java.util.List;

@Data
public class UpdateItemRequest {
    private List<Long> assignedPersonIds;
    private String notes;
}
