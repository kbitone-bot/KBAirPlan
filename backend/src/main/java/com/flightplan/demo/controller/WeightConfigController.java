package com.flightplan.demo.controller;

import com.flightplan.demo.dto.WeightConfigRequest;
import com.flightplan.demo.dto.WeightConfigResponse;
import com.flightplan.demo.service.WeightConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/config/weights")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class WeightConfigController {
    
    private final WeightConfigService weightConfigService;
    
    @GetMapping
    public ResponseEntity<List<WeightConfigResponse>> getAllConfigs() {
        return ResponseEntity.ok(weightConfigService.getAllConfigs());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<WeightConfigResponse> getConfig(@PathVariable Long id) {
        return ResponseEntity.ok(weightConfigService.getConfig(id));
    }
    
    @GetMapping("/default")
    public ResponseEntity<WeightConfigResponse> getDefaultConfig() {
        return ResponseEntity.ok(weightConfigService.getDefaultConfig());
    }
    
    @PostMapping
    public ResponseEntity<WeightConfigResponse> createConfig(@RequestBody WeightConfigRequest request) {
        return ResponseEntity.ok(weightConfigService.createConfig(request));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<WeightConfigResponse> updateConfig(
            @PathVariable Long id, 
            @RequestBody WeightConfigRequest request) {
        return ResponseEntity.ok(weightConfigService.updateConfig(id, request));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConfig(@PathVariable Long id) {
        weightConfigService.deleteConfig(id);
        return ResponseEntity.ok().build();
    }
}
