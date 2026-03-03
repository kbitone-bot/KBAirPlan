package com.flightplan.demo.service;

import com.flightplan.demo.dto.AircraftDto;
import com.flightplan.demo.entity.Aircraft;
import com.flightplan.demo.entity.Base;
import com.flightplan.demo.repository.AircraftRepository;
import com.flightplan.demo.repository.BaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AircraftService {
    
    private final AircraftRepository aircraftRepository;
    private final BaseRepository baseRepository;
    
    @Transactional(readOnly = true)
    public List<AircraftDto> getAllAircraft(Long baseId, Aircraft.Type type) {
        List<Aircraft> aircrafts;
        
        if (baseId != null && type != null) {
            aircrafts = aircraftRepository.findByBaseIdAndType(baseId, type);
        } else if (baseId != null) {
            aircrafts = aircraftRepository.findByBaseId(baseId);
        } else {
            aircrafts = aircraftRepository.findAll();
        }
        
        return aircrafts.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public AircraftDto getAircraft(Long id) {
        Aircraft aircraft = aircraftRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Aircraft not found: " + id));
        return convertToDto(aircraft);
    }
    
    @Transactional
    public AircraftDto createAircraft(AircraftDto dto) {
        Base base = baseRepository.findById(dto.getBaseId())
                .orElseThrow(() -> new RuntimeException("Base not found"));
        
        Aircraft aircraft = Aircraft.builder()
                .type(dto.getType())
                .model(dto.getModel())
                .tailNumber(dto.getTailNumber())
                .base(base)
                .available(dto.getAvailable() != null ? dto.getAvailable() : true)
                .maxCrew(dto.getMaxCrew())
                .build();
        
        aircraft = aircraftRepository.save(aircraft);
        return convertToDto(aircraft);
    }
    
    @Transactional
    public AircraftDto updateAircraft(Long id, AircraftDto dto) {
        Aircraft aircraft = aircraftRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Aircraft not found: " + id));
        
        if (dto.getModel() != null) aircraft.setModel(dto.getModel());
        if (dto.getTailNumber() != null) aircraft.setTailNumber(dto.getTailNumber());
        if (dto.getAvailable() != null) aircraft.setAvailable(dto.getAvailable());
        if (dto.getMaxCrew() != null) aircraft.setMaxCrew(dto.getMaxCrew());
        
        aircraft = aircraftRepository.save(aircraft);
        return convertToDto(aircraft);
    }
    
    @Transactional
    public void deleteAircraft(Long id) {
        aircraftRepository.deleteById(id);
    }
    
    private AircraftDto convertToDto(Aircraft aircraft) {
        return AircraftDto.builder()
                .id(aircraft.getId())
                .type(aircraft.getType())
                .model(aircraft.getModel())
                .tailNumber(aircraft.getTailNumber())
                .baseId(aircraft.getBase().getId())
                .baseName(aircraft.getBase().getName())
                .available(aircraft.getAvailable())
                .maxCrew(aircraft.getMaxCrew())
                .createdAt(aircraft.getCreatedAt())
                .build();
    }
}
