package com.flightplan.demo.service;

import com.flightplan.demo.dto.MissionTemplateDto;
import com.flightplan.demo.entity.Aircraft;
import com.flightplan.demo.entity.MissionTemplate;
import com.flightplan.demo.repository.MissionTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MissionTemplateService {
    
    private final MissionTemplateRepository missionTemplateRepository;
    
    @Transactional(readOnly = true)
    public List<MissionTemplateDto> getAllTemplates(Aircraft.Type aircraftType) {
        List<MissionTemplate> templates;
        
        if (aircraftType != null) {
            templates = missionTemplateRepository.findByAircraftType(aircraftType);
        } else {
            templates = missionTemplateRepository.findAll();
        }
        
        return templates.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public MissionTemplateDto getTemplate(Long id) {
        MissionTemplate template = missionTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found: " + id));
        return convertToDto(template);
    }
    
    @Transactional
    public MissionTemplateDto createTemplate(MissionTemplateDto dto) {
        MissionTemplate template = MissionTemplate.builder()
                .aircraftType(dto.getAircraftType())
                .missionName(dto.getMissionName())
                .requiredPilotCount(dto.getRequiredPilotCount())
                .requiredCrewCount(dto.getRequiredCrewCount())
                .durationMinutes(dto.getDurationMinutes())
                .description(dto.getDescription())
                .requiredQualificationIds(dto.getRequiredQualificationIds())
                .build();
        
        template = missionTemplateRepository.save(template);
        return convertToDto(template);
    }
    
    @Transactional
    public MissionTemplateDto updateTemplate(Long id, MissionTemplateDto dto) {
        MissionTemplate template = missionTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found: " + id));
        
        if (dto.getMissionName() != null) template.setMissionName(dto.getMissionName());
        if (dto.getRequiredPilotCount() != null) template.setRequiredPilotCount(dto.getRequiredPilotCount());
        if (dto.getRequiredCrewCount() != null) template.setRequiredCrewCount(dto.getRequiredCrewCount());
        if (dto.getDurationMinutes() != null) template.setDurationMinutes(dto.getDurationMinutes());
        if (dto.getDescription() != null) template.setDescription(dto.getDescription());
        if (dto.getRequiredQualificationIds() != null) template.setRequiredQualificationIds(dto.getRequiredQualificationIds());
        
        template = missionTemplateRepository.save(template);
        return convertToDto(template);
    }
    
    @Transactional
    public void deleteTemplate(Long id) {
        missionTemplateRepository.deleteById(id);
    }
    
    private MissionTemplateDto convertToDto(MissionTemplate template) {
        return MissionTemplateDto.builder()
                .id(template.getId())
                .aircraftType(template.getAircraftType())
                .missionName(template.getMissionName())
                .requiredPilotCount(template.getRequiredPilotCount())
                .requiredCrewCount(template.getRequiredCrewCount())
                .durationMinutes(template.getDurationMinutes())
                .description(template.getDescription())
                .requiredQualificationIds(template.getRequiredQualificationIds())
                .createdAt(template.getCreatedAt())
                .build();
    }
}
