package com.flightplan.demo.controller;

import com.flightplan.demo.dto.MissionTemplateDto;
import com.flightplan.demo.entity.Aircraft;
import com.flightplan.demo.service.MissionTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/templates")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MissionTemplateController {
    
    private final MissionTemplateService missionTemplateService;
    
    @GetMapping
    public ResponseEntity<List<MissionTemplateDto>> getAllTemplates(
            @RequestParam(required = false) Aircraft.Type aircraftType) {
        return ResponseEntity.ok(missionTemplateService.getAllTemplates(aircraftType));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<MissionTemplateDto> getTemplate(@PathVariable Long id) {
        return ResponseEntity.ok(missionTemplateService.getTemplate(id));
    }
    
    @PostMapping
    public ResponseEntity<MissionTemplateDto> createTemplate(@RequestBody MissionTemplateDto dto) {
        return ResponseEntity.ok(missionTemplateService.createTemplate(dto));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<MissionTemplateDto> updateTemplate(@PathVariable Long id, @RequestBody MissionTemplateDto dto) {
        return ResponseEntity.ok(missionTemplateService.updateTemplate(id, dto));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        missionTemplateService.deleteTemplate(id);
        return ResponseEntity.ok().build();
    }
}
