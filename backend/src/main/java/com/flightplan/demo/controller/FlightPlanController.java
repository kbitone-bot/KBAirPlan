package com.flightplan.demo.controller;

import com.flightplan.demo.dto.*;
import com.flightplan.demo.service.FlightPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/plans")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FlightPlanController {
    
    private final FlightPlanService flightPlanService;
    
    @PostMapping("/draft/generate")
    public ResponseEntity<DraftResponse> generateDraft(@RequestBody GenerateDraftRequest request) {
        DraftResponse response = flightPlanService.generateDraft(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/draft/{draftId}")
    public ResponseEntity<DraftResponse> getDraft(@PathVariable Long draftId) {
        DraftResponse response = flightPlanService.getDraft(draftId);
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/draft/{draftId}/items/{itemId}")
    public ResponseEntity<FlightPlanItemResponse> updateItem(
            @PathVariable Long draftId,
            @PathVariable Long itemId,
            @RequestBody UpdateItemRequest request) {
        FlightPlanItemResponse response = flightPlanService.updateItem(draftId, itemId, request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/draft/{draftId}/recompute")
    public ResponseEntity<DraftResponse> recomputeDraft(
            @PathVariable Long draftId,
            @RequestParam Long weightConfigId) {
        DraftResponse response = flightPlanService.recomputeDraft(draftId, weightConfigId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/summary/month")
    public ResponseEntity<MonthlySummaryResponse> getMonthlySummary(
            @RequestParam Long baseId,
            @RequestParam String month) {
        MonthlySummaryResponse response = flightPlanService.getMonthlySummary(baseId, month);
        return ResponseEntity.ok(response);
    }
}
