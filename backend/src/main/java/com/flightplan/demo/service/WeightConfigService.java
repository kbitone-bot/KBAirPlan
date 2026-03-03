package com.flightplan.demo.service;

import com.flightplan.demo.dto.WeightConfigRequest;
import com.flightplan.demo.dto.WeightConfigResponse;
import com.flightplan.demo.entity.WeightConfig;
import com.flightplan.demo.repository.WeightConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WeightConfigService {
    
    private final WeightConfigRepository weightConfigRepository;
    
    @Transactional(readOnly = true)
    public List<WeightConfigResponse> getAllConfigs() {
        return weightConfigRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public WeightConfigResponse getConfig(Long id) {
        WeightConfig config = weightConfigRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Config not found: " + id));
        return convertToResponse(config);
    }
    
    @Transactional(readOnly = true)
    public WeightConfigResponse getDefaultConfig() {
        WeightConfig config = weightConfigRepository.findByIsDefaultTrue()
                .orElse(WeightConfig.getDefault());
        return convertToResponse(config);
    }
    
    @Transactional
    public WeightConfigResponse createConfig(WeightConfigRequest request) {
        WeightConfig config = WeightConfig.builder()
                .name(request.getName())
                .fairnessWeight(request.getFairnessWeight())
                .skillWeight(request.getSkillWeight())
                .fatigueWeight(request.getFatigueWeight())
                .continuityWeight(request.getContinuityWeight())
                .baseBalanceWeight(request.getBaseBalanceWeight())
                .isDefault(request.getIsDefault() != null ? request.getIsDefault() : false)
                .build();
        
        config = weightConfigRepository.save(config);
        return convertToResponse(config);
    }
    
    @Transactional
    public WeightConfigResponse updateConfig(Long id, WeightConfigRequest request) {
        WeightConfig config = weightConfigRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Config not found: " + id));
        
        if (request.getName() != null) config.setName(request.getName());
        if (request.getFairnessWeight() != null) config.setFairnessWeight(request.getFairnessWeight());
        if (request.getSkillWeight() != null) config.setSkillWeight(request.getSkillWeight());
        if (request.getFatigueWeight() != null) config.setFatigueWeight(request.getFatigueWeight());
        if (request.getContinuityWeight() != null) config.setContinuityWeight(request.getContinuityWeight());
        if (request.getBaseBalanceWeight() != null) config.setBaseBalanceWeight(request.getBaseBalanceWeight());
        if (request.getIsDefault() != null) config.setIsDefault(request.getIsDefault());
        
        config = weightConfigRepository.save(config);
        return convertToResponse(config);
    }
    
    @Transactional
    public void deleteConfig(Long id) {
        weightConfigRepository.deleteById(id);
    }
    
    private WeightConfigResponse convertToResponse(WeightConfig config) {
        return WeightConfigResponse.builder()
                .id(config.getId())
                .name(config.getName())
                .fairnessWeight(config.getFairnessWeight())
                .skillWeight(config.getSkillWeight())
                .fatigueWeight(config.getFatigueWeight())
                .continuityWeight(config.getContinuityWeight())
                .baseBalanceWeight(config.getBaseBalanceWeight())
                .isDefault(config.getIsDefault())
                .createdAt(config.getCreatedAt())
                .build();
    }
}
