package com.flightplan.demo.controller;

import com.flightplan.demo.dto.AircraftDto;
import com.flightplan.demo.entity.Aircraft;
import com.flightplan.demo.service.AircraftService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/aircrafts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AircraftController {
    
    private final AircraftService aircraftService;
    
    @GetMapping
    public ResponseEntity<List<AircraftDto>> getAllAircrafts(
            @RequestParam(required = false) Long baseId,
            @RequestParam(required = false) Aircraft.Type type) {
        return ResponseEntity.ok(aircraftService.getAllAircraft(baseId, type));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<AircraftDto> getAircraft(@PathVariable Long id) {
        return ResponseEntity.ok(aircraftService.getAircraft(id));
    }
    
    @PostMapping
    public ResponseEntity<AircraftDto> createAircraft(@RequestBody AircraftDto dto) {
        return ResponseEntity.ok(aircraftService.createAircraft(dto));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<AircraftDto> updateAircraft(@PathVariable Long id, @RequestBody AircraftDto dto) {
        return ResponseEntity.ok(aircraftService.updateAircraft(id, dto));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAircraft(@PathVariable Long id) {
        aircraftService.deleteAircraft(id);
        return ResponseEntity.ok().build();
    }
}
