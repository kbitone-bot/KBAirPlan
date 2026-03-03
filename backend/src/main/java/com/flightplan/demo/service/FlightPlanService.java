package com.flightplan.demo.service;

import com.flightplan.demo.algorithm.FlightPlanAlgorithmService;
import com.flightplan.demo.dto.*;
import com.flightplan.demo.entity.*;
import com.flightplan.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlightPlanService {
    
    private final FlightPlanDraftRepository draftRepository;
    private final FlightPlanItemRepository itemRepository;
    private final PersonRepository personRepository;
    private final AircraftRepository aircraftRepository;
    private final MissionTemplateRepository missionTemplateRepository;
    private final WeightConfigRepository weightConfigRepository;
    private final AirspaceRepository airspaceRepository;
    private final FlightPlanAlgorithmService algorithmService;
    
    @Transactional
    public DraftResponse generateDraft(GenerateDraftRequest request) {
        return algorithmService.generateDraft(request);
    }
    
    @Transactional(readOnly = true)
    public DraftResponse getDraft(Long draftId) {
        FlightPlanDraft draft = draftRepository.findById(draftId)
                .orElseThrow(() -> new RuntimeException("Draft not found: " + draftId));
        return convertToDraftResponse(draft);
    }
    
    @Transactional
    public FlightPlanItemResponse updateItem(Long draftId, Long itemId, UpdateItemRequest request) {
        FlightPlanItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found: " + itemId));
        
        if (!item.getDraft().getId().equals(draftId)) {
            throw new RuntimeException("Item does not belong to draft");
        }
        
        // 배정 인원 업데이트
        if (request.getAssignedPersonIds() != null) {
            item.setAssignedPersonIds(new ArrayList<>(request.getAssignedPersonIds()));
        }
        
        if (request.getNotes() != null) {
            item.setNotes(request.getNotes());
        }
        
        item = itemRepository.save(item);
        return convertToItemResponse(item);
    }
    
    @Transactional
    public DraftResponse recomputeDraft(Long draftId, Long weightConfigId) {
        return algorithmService.recomputeDraft(draftId, weightConfigId);
    }
    
    @Transactional(readOnly = true)
    public MonthlySummaryResponse getMonthlySummary(Long baseId, String month) {
        YearMonth yearMonth = YearMonth.parse(month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        
        List<FlightPlanItem> items = itemRepository.findByBaseIdAndDateRange(baseId, startDate, endDate);
        Base base = new Base();
        base.setId(baseId);
        base.setName(baseId == 1 ? "포항" : "제주");
        
        // 통계 계산
        int totalFlights = items.size();
        Map<Long, Integer> personFlightCount = new HashMap<>();
        Map<Long, Integer> aircraftFlightCount = new HashMap<>();
        Map<String, Integer> missionsByType = new HashMap<>();
        Map<String, Integer> dailyFlights = new HashMap<>();
        
        for (FlightPlanItem item : items) {
            // 인원별 통계
            for (Long personId : item.getAssignedPersonIds()) {
                personFlightCount.merge(personId, 1, Integer::sum);
            }
            
            // 기처리별 통계
            aircraftFlightCount.merge(item.getAircraft().getId(), 1, Integer::sum);
            
            // 임문별 통계
            missionsByType.merge(item.getMissionTemplate().getMissionName(), 1, Integer::sum);
            
            // 일별 통계
            dailyFlights.merge(item.getFlightDate().toString(), 1, Integer::sum);
        }
        
        // 인원 상세 통계
        List<MonthlySummaryResponse.PersonStats> personStats = personFlightCount.entrySet().stream()
                .map(entry -> {
                    Person person = personRepository.findById(entry.getKey()).orElse(null);
                    return MonthlySummaryResponse.PersonStats.builder()
                            .personId(entry.getKey())
                            .name(person != null ? person.getName() : "Unknown")
                            .role(person != null ? person.getRole().name() : "Unknown")
                            .flightCount(entry.getValue())
                            .flightHours(entry.getValue() * 2)
                            .build();
                })
                .sorted(Comparator.comparingInt(MonthlySummaryResponse.PersonStats::getFlightCount).reversed())
                .collect(Collectors.toList());
        
        // 기체 상세 통계
        List<MonthlySummaryResponse.AircraftStats> aircraftStats = aircraftFlightCount.entrySet().stream()
                .map(entry -> {
                    Aircraft aircraft = aircraftRepository.findById(entry.getKey()).orElse(null);
                    return MonthlySummaryResponse.AircraftStats.builder()
                            .aircraftId(entry.getKey())
                            .model(aircraft != null ? aircraft.getModel() : "Unknown")
                            .tailNumber(aircraft != null ? aircraft.getTailNumber() : "Unknown")
                            .flightCount(entry.getValue())
                            .flightHours(entry.getValue() * 2)
                            .build();
                })
                .sorted(Comparator.comparingInt(MonthlySummaryResponse.AircraftStats::getFlightCount).reversed())
                .collect(Collectors.toList());
        
        // 일별 통계
        List<MonthlySummaryResponse.DailyStats> dailyStats = dailyFlights.entrySet().stream()
                .map(entry -> MonthlySummaryResponse.DailyStats.builder()
                        .date(entry.getKey())
                        .flightCount(entry.getValue())
                        .personnelCount(0)
                        .build())
                .sorted(Comparator.comparing(MonthlySummaryResponse.DailyStats::getDate))
                .collect(Collectors.toList());
        
        return MonthlySummaryResponse.builder()
                .month(month)
                .baseId(baseId)
                .baseName(base.getName())
                .summary(MonthlySummaryResponse.SummaryData.builder()
                        .totalFlights(totalFlights)
                        .totalPersonnel(personStats.size())
                        .totalAircraft(aircraftStats.size())
                        .avgPersonsPerFlight(totalFlights > 0 ? 
                                personFlightCount.values().stream().mapToInt(Integer::intValue).sum() / (double) totalFlights : 0)
                        .missionsByType(missionsByType)
                        .build())
                .personStats(personStats)
                .aircraftStats(aircraftStats)
                .dailyStats(dailyStats)
                .build();
    }
    
    // === 남은 메서드들 ===
    
    private DraftResponse convertToDraftResponse(FlightPlanDraft draft) {
        List<FlightPlanItemResponse> itemResponses = draft.getItems().stream()
                .map(this::convertToItemResponse)
                .collect(Collectors.toList());
        
        List<DraftResponse.Violation> violations = new ArrayList<>();
        int hardViolations = 0;
        
        for (FlightPlanItem item : draft.getItems()) {
            int required = item.getMissionTemplate().getRequiredPilotCount() + 
                          item.getMissionTemplate().getRequiredCrewCount();
            if (item.getAssignedPersonIds().size() < required) {
                violations.add(DraftResponse.Violation.builder()
                        .itemId(item.getId())
                        .type("HARD")
                        .severity("HIGH")
                        .message("요구 인원 미달: " + item.getAssignedPersonIds().size() + "/" + required)
                        .build());
                hardViolations++;
            }
        }
        
        double avgScore = itemResponses.isEmpty() ? 0 : 
                itemResponses.stream().mapToDouble(FlightPlanItemResponse::getScore).average().orElse(0);
        
        return DraftResponse.builder()
                .id(draft.getId())
                .baseId(draft.getBase().getId())
                .baseName(draft.getBase().getName())
                .periodType(draft.getPeriodType())
                .startDate(draft.getStartDate())
                .endDate(draft.getEndDate())
                .weightConfigId(draft.getWeightConfig().getId())
                .weightConfigName(draft.getWeightConfig().getName())
                .status(draft.getStatus())
                .totalScore(draft.getTotalScore())
                .items(itemResponses)
                .scoreSummary(DraftResponse.ScoreSummary.builder()
                        .totalFlights(itemResponses.size())
                        .assignedFlights((int) itemResponses.stream()
                                .filter(i -> !i.getAssignedPersons().isEmpty()).count())
                        .hardViolations(hardViolations)
                        .softViolations(0)
                        .averageScore(avgScore)
                        .fairnessScore(avgScore * 0.3)
                        .skillScore(avgScore * 0.3)
                        .fatigueScore(avgScore * 0.2)
                        .build())
                .violations(violations)
                .createdAt(draft.getCreatedAt())
                .build();
    }
    
    private FlightPlanItemResponse convertToItemResponse(FlightPlanItem item) {
        List<FlightPlanItemResponse.AssignedPersonResponse> assignedPersons = 
                item.getAssignedPersonIds().stream()
                        .map(personId -> {
                            Person person = personRepository.findById(personId).orElse(null);
                            return FlightPlanItemResponse.AssignedPersonResponse.builder()
                                    .personId(personId)
                                    .name(person != null ? person.getName() : "Unknown")
                                    .rank(person != null ? person.getRank() : "")
                                    .role(person != null ? person.getRole().name() : "")
                                    .build();
                        })
                        .collect(Collectors.toList());
        
        return FlightPlanItemResponse.builder()
                .id(item.getId())
                .flightDate(item.getFlightDate())
                .startTime(item.getStartTime())
                .endTime(item.getEndTime())
                .aircraftId(item.getAircraft().getId())
                .aircraftModel(item.getAircraft().getModel())
                .tailNumber(item.getAircraft().getTailNumber())
                .missionTemplateId(item.getMissionTemplate().getId())
                .missionName(item.getMissionTemplate().getMissionName())
                .airspaceId(item.getAirspace() != null ? item.getAirspace().getId() : null)
                .airspaceName(item.getAirspace() != null ? item.getAirspace().getName() : null)
                .notes(item.getNotes())
                .score(item.getScore())
                .status(item.getStatus())
                .assignedPersons(assignedPersons)
                .violations(new ArrayList<>())
                .explanation(FlightPlanItemResponse.AssignmentExplanation.builder()
                        .skillContribution(item.getScore() != null ? item.getScore() * 0.3 : 0)
                        .fairnessContribution(item.getScore() != null ? item.getScore() * 0.3 : 0)
                        .fatiguePenalty(item.getScore() != null ? item.getScore() * -0.2 : 0)
                        .continuityPenalty(item.getScore() != null ? item.getScore() * -0.1 : 0)
                        .totalScore(item.getScore() != null ? item.getScore() : 0)
                        .reason("숙련도 + 공평성 가중치 기반 배정")
                        .build())
                .build();
    }
}
