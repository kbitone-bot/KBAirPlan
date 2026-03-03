package com.flightplan.demo.algorithm;

import com.flightplan.demo.dto.*;
import com.flightplan.demo.entity.*;
import com.flightplan.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlightPlanAlgorithmService {
    
    private final FlightPlanDraftRepository draftRepository;
    private final FlightPlanItemRepository itemRepository;
    private final PersonRepository personRepository;
    private final AircraftRepository aircraftRepository;
    private final MissionTemplateRepository missionTemplateRepository;
    private final WeightConfigRepository weightConfigRepository;
    private final UnavailabilityRepository unavailabilityRepository;
    private final ConstraintChecker constraintChecker;
    
    private static final int MAX_ITERATIONS = 100;
    private static final double IMPROVEMENT_THRESHOLD = 0.01;
    
    /**
     * 1단계: Greedy 알고리즘으로 초안 생성
     */
    @Transactional
    public DraftResponse generateDraft(GenerateDraftRequest request) {
        log.info("Generating draft for base {} from {} to {}", 
                request.getBaseId(), request.getStartDate(), request.getEndDate());
        
        // 설정 로드
        WeightConfig weightConfig = weightConfigRepository.findById(request.getWeightConfigId())
                .orElse(WeightConfig.getDefault());
        
        Base base = new Base();
        base.setId(request.getBaseId());
        
        // Draft 생성
        FlightPlanDraft draft = FlightPlanDraft.builder()
                .base(base)
                .periodType(request.getPeriodType())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .weightConfig(weightConfig)
                .status(FlightPlanDraft.Status.DRAFT)
                .build();
        
        draft = draftRepository.save(draft);
        
        // 사용 가능한 자원 로드
        List<Aircraft> aircrafts = aircraftRepository.findByBaseIdAndTypeAndAvailable(
                request.getBaseId(), request.getAircraftType(), true);
        List<MissionTemplate> missions = missionTemplateRepository.findByAircraftType(request.getAircraftType());
        List<Person> pilots = personRepository.findActiveByBaseAndRole(request.getBaseId(), Person.Role.PILOT);
        List<Person> crews = personRepository.findActiveByBaseAndRole(request.getBaseId(), Person.Role.CREW);
        
        // 부재 정보 로드
        List<Long> allPersonIds = new ArrayList<>();
        allPersonIds.addAll(pilots.stream().map(Person::getId).toList());
        allPersonIds.addAll(crews.stream().map(Person::getId).toList());
        
        // Greedy 배정
        List<FlightPlanItem> items = greedyAssignment(
                draft, aircrafts, missions, pilots, crews, 
                request.getStartDate(), request.getEndDate(), 
                request.getFlightsPerDay() != null ? request.getFlightsPerDay() : 3,
                weightConfig);
        
        draft.setItems(items);
        
        // 2단계: Local Search로 개선
        List<FlightPlanItem> improvedItems = localSearchOptimization(draft, items, weightConfig);
        draft.setItems(improvedItems);
        
        // 스코어 계산
        double totalScore = calculateDraftScore(improvedItems, weightConfig);
        draft.setTotalScore(totalScore);
        
        draftRepository.save(draft);
        
        return convertToDraftResponse(draft);
    }
    
    /**
     * Greedy 배정 알고리즘
     */
    private List<FlightPlanItem> greedyAssignment(
            FlightPlanDraft draft,
            List<Aircraft> aircrafts,
            List<MissionTemplate> missions,
            List<Person> pilots,
            List<Person> crews,
            LocalDate startDate,
            LocalDate endDate,
            int flightsPerDay,
            WeightConfig weights) {
        
        List<FlightPlanItem> items = new ArrayList<>();
        Map<Long, Integer> assignmentCounts = new HashMap<>();
        
        Random random = new Random(42); // 재현성을 위한 고정 시드
        
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            // 하루 비행 일정 생성
            List<LocalTime[]> timeSlots = generateTimeSlots(flightsPerDay);
            
            for (int i = 0; i < flightsPerDay && i < aircrafts.size(); i++) {
                Aircraft aircraft = aircrafts.get(i % aircrafts.size());
                MissionTemplate mission = missions.get(random.nextInt(missions.size()));
                LocalTime[] slot = timeSlots.get(i);
                
                FlightPlanItem item = FlightPlanItem.builder()
                        .draft(draft)
                        .flightDate(date)
                        .startTime(slot[0])
                        .endTime(slot[1])
                        .aircraft(aircraft)
                        .missionTemplate(mission)
                        .status(FlightPlanItem.Status.SCHEDULED)
                        .assignedPersonIds(new ArrayList<>())
                        .build();
                
                // 필수 인원 배정 (파일럿)
                List<Person> assignedPilots = assignBestPersons(
                        pilots, mission.getRequiredPilotCount(), 
                        date, slot[0], slot[1], assignmentCounts, 
                        item.getAssignedPersonIds(), mission, weights);
                
                for (Person p : assignedPilots) {
                    item.getAssignedPersonIds().add(p.getId());
                    assignmentCounts.merge(p.getId(), 1, Integer::sum);
                }
                
                // 필수 인원 배정 (크루)
                List<Person> assignedCrews = assignBestPersons(
                        crews, mission.getRequiredCrewCount(),
                        date, slot[0], slot[1], assignmentCounts,
                        item.getAssignedPersonIds(), mission, weights);
                
                for (Person c : assignedCrews) {
                    item.getAssignedPersonIds().add(c.getId());
                    assignmentCounts.merge(c.getId(), 1, Integer::sum);
                }
                
                // 스코어 계산
                item.setScore(calculateItemScore(item, pilots, crews, weights, assignmentCounts));
                
                items.add(item);
            }
        }
        
        return items;
    }
    
    /**
     * 가장 적합한 인원 배정
     */
    private List<Person> assignBestPersons(
            List<Person> candidates,
            int count,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            Map<Long, Integer> assignmentCounts,
            List<Long> alreadyAssigned,
            MissionTemplate mission,
            WeightConfig weights) {
        
        List<Person> selected = new ArrayList<>();
        Set<Long> used = new HashSet<>(alreadyAssigned);
        
        for (int i = 0; i < count; i++) {
            Person best = null;
            double bestScore = Double.NEGATIVE_INFINITY;
            
            for (Person candidate : candidates) {
                if (used.contains(candidate.getId())) continue;
                
                // 하드 제약 확인
                List<String> violations = constraintChecker.checkHardConstraints(
                        candidate, date, startTime, endTime, 
                        selected, mission);
                
                if (!violations.isEmpty()) continue;
                
                // 점수 계산
                double score = evaluatePerson(candidate, date, assignmentCounts, weights);
                
                if (score > bestScore) {
                    bestScore = score;
                    best = candidate;
                }
            }
            
            if (best != null) {
                selected.add(best);
                used.add(best.getId());
            }
        }
        
        return selected;
    }
    
    /**
     * 인원 평가 함수
     */
    private double evaluatePerson(Person person, LocalDate date, 
                                   Map<Long, Integer> assignmentCounts, 
                                   WeightConfig weights) {
        double score = 0;
        
        // 숙련도 (비행 시간 기반)
        int flightHours = person.getTotalFlightHours() != null ? person.getTotalFlightHours() : 0;
        score += weights.getSkillWeight() * Math.min(flightHours / 100.0, 10);
        
        // 공평성 (적게 배정된 사람 선호)
        int currentCount = assignmentCounts.getOrDefault(person.getId(), 0);
        double avgCount = assignmentCounts.values().stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0);
        score += weights.getFairnessWeight() * Math.max(0, (avgCount - currentCount) * 2);
        
        // 연속 배정 페널티
        int consecutive = assignmentCounts.getOrDefault(person.getId() + date.minusDays(1).toEpochDay(), 0);
        score -= weights.getContinuityWeight() * consecutive * 3;
        
        // 피로도 (주말 페널티)
        if (date.getDayOfWeek().getValue() >= 6) {
            score -= weights.getFatigueWeight() * 2;
        }
        
        return score;
    }
    
    /**
     * 2단계: Local Search 최적화
     */
    private List<FlightPlanItem> localSearchOptimization(
            FlightPlanDraft draft,
            List<FlightPlanItem> items,
            WeightConfig weights) {
        
        double currentScore = calculateDraftScore(items, weights);
        int iterations = 0;
        
        while (iterations < MAX_ITERATIONS) {
            boolean improved = false;
            
            // 스왑 시도: 두 아이템 간 인원 교환
            for (int i = 0; i < items.size(); i++) {
                for (int j = i + 1; j < items.size(); j++) {
                    FlightPlanItem item1 = items.get(i);
                    FlightPlanItem item2 = items.get(j);
                    
                    // 같은 날, 시간이 겹치면 스왑 불가
                    if (item1.getFlightDate().equals(item2.getFlightDate()) &&
                        timesOverlap(item1.getStartTime(), item1.getEndTime(),
                                   item2.getStartTime(), item2.getEndTime())) {
                        continue;
                    }
                    
                    // 스왑 시도
                    if (trySwapPersons(item1, item2, weights)) {
                        double newScore = calculateDraftScore(items, weights);
                        if (newScore > currentScore + IMPROVEMENT_THRESHOLD) {
                            currentScore = newScore;
                            improved = true;
                        } else {
                            // 되돌리기
                            undoSwap(item1, item2);
                        }
                    }
                }
            }
            
            if (!improved) break;
            iterations++;
        }
        
        log.info("Local search completed after {} iterations, final score: {}", iterations, currentScore);
        return items;
    }
    
    private boolean trySwapPersons(FlightPlanItem item1, FlightPlanItem item2, WeightConfig weights) {
        // 간단한 스왑: 같은 역할을 가진 인원만 교환 가능하다고 가정
        // 실제 구현에서는 역할 정보를 활용한 더 정교한 로직 필요
        List<Long> persons1 = new ArrayList<>(item1.getAssignedPersonIds());
        List<Long> persons2 = new ArrayList<>(item2.getAssignedPersonIds());
        
        if (persons1.isEmpty() || persons2.isEmpty()) return false;
        
        // 첫 번째 인원 교환
        Long temp = persons1.get(0);
        persons1.set(0, persons2.get(0));
        persons2.set(0, temp);
        
        item1.setAssignedPersonIds(persons1);
        item2.setAssignedPersonIds(persons2);
        
        return true;
    }
    
    private void undoSwap(FlightPlanItem item1, FlightPlanItem item2) {
        List<Long> persons1 = new ArrayList<>(item1.getAssignedPersonIds());
        List<Long> persons2 = new ArrayList<>(item2.getAssignedPersonIds());
        
        if (persons1.isEmpty() || persons2.isEmpty()) return;
        
        Long temp = persons1.get(0);
        persons1.set(0, persons2.get(0));
        persons2.set(0, temp);
        
        item1.setAssignedPersonIds(persons1);
        item2.setAssignedPersonIds(persons2);
    }
    
    private boolean timesOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return start1.isBefore(end2) && end1.isAfter(start2);
    }
    
    /**
     * 항목별 스코어 계산
     */
    private double calculateItemScore(FlightPlanItem item, List<Person> pilots, List<Person> crews,
                                       WeightConfig weights, Map<Long, Integer> assignmentCounts) {
        double score = 0;
        List<Long> assignedIds = item.getAssignedPersonIds();
        
        for (Long personId : assignedIds) {
            Person person = findPersonById(personId, pilots, crews);
            if (person != null) {
                score += evaluatePerson(person, item.getFlightDate(), assignmentCounts, weights);
            }
        }
        
        // 페널티: 요구 인원 미달
        int requiredCount = item.getMissionTemplate().getRequiredPilotCount() + 
                           item.getMissionTemplate().getRequiredCrewCount();
        if (assignedIds.size() < requiredCount) {
            score -= 50 * (requiredCount - assignedIds.size());
        }
        
        return score;
    }
    
    /**
     * Draft 전체 스코어 계산
     */
    private double calculateDraftScore(List<FlightPlanItem> items, WeightConfig weights) {
        return items.stream()
                .mapToDouble(FlightPlanItem::getScore)
                .sum();
    }
    
    private Person findPersonById(Long id, List<Person> pilots, List<Person> crews) {
        return pilots.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElse(crews.stream()
                        .filter(c -> c.getId().equals(id))
                        .findFirst()
                        .orElse(null));
    }
    
    private List<LocalTime[]> generateTimeSlots(int count) {
        List<LocalTime[]> slots = new ArrayList<>();
        int[] startHours = {8, 10, 13, 15}; // 오전 8시, 10시, 오후 1시, 3시
        
        for (int i = 0; i < count && i < startHours.length; i++) {
            LocalTime start = LocalTime.of(startHours[i], 0);
            LocalTime end = start.plusHours(2);
            slots.add(new LocalTime[]{start, end});
        }
        
        return slots;
    }
    
    /**
     * 재계산 (가중치 변경 시)
     */
    @Transactional
    public DraftResponse recomputeDraft(Long draftId, Long weightConfigId) {
        FlightPlanDraft draft = draftRepository.findById(draftId)
                .orElseThrow(() -> new RuntimeException("Draft not found"));
        
        WeightConfig newConfig = weightConfigRepository.findById(weightConfigId)
                .orElse(WeightConfig.getDefault());
        
        draft.setWeightConfig(newConfig);
        
        // 스코어 재계산
        List<FlightPlanItem> items = itemRepository.findByDraftId(draftId);
        Map<Long, Integer> assignmentCounts = new HashMap<>();
        
        for (FlightPlanItem item : items) {
            for (Long personId : item.getAssignedPersonIds()) {
                assignmentCounts.merge(personId, 1, Integer::sum);
            }
        }
        
        List<Person> pilots = personRepository.findActiveByBaseAndRole(draft.getBase().getId(), Person.Role.PILOT);
        List<Person> crews = personRepository.findActiveByBaseAndRole(draft.getBase().getId(), Person.Role.CREW);
        
        for (FlightPlanItem item : items) {
            item.setScore(calculateItemScore(item, pilots, crews, newConfig, assignmentCounts));
        }
        
        double totalScore = calculateDraftScore(items, newConfig);
        draft.setTotalScore(totalScore);
        
        draftRepository.save(draft);
        
        return convertToDraftResponse(draft);
    }
    
    /**
     * Response 변환
     */
    private DraftResponse convertToDraftResponse(FlightPlanDraft draft) {
        List<FlightPlanItemResponse> itemResponses = draft.getItems().stream()
                .map(this::convertToItemResponse)
                .collect(Collectors.toList());
        
        // 위반 사항 수집
        List<DraftResponse.Violation> violations = new ArrayList<>();
        int hardViolations = 0;
        int softViolations = 0;
        
        for (FlightPlanItem item : draft.getItems()) {
            if (item.getAssignedPersonIds().isEmpty()) {
                violations.add(DraftResponse.Violation.builder()
                        .itemId(item.getId())
                        .type("HARD")
                        .severity("CRITICAL")
                        .message("배정된 인원 없음")
                        .build());
                hardViolations++;
            }
            
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
                        .softViolations(softViolations)
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
                .assignedPersons(new ArrayList<>()) // 실제로는 조회해서 채워야 함
                .explanation(FlightPlanItemResponse.AssignmentExplanation.builder()
                        .skillContribution(item.getScore() * 0.3)
                        .fairnessContribution(item.getScore() * 0.3)
                        .fatiguePenalty(item.getScore() * -0.2)
                        .continuityPenalty(item.getScore() * -0.1)
                        .totalScore(item.getScore())
                        .reason("숙련도 + 공평성 가중치 기반 배정")
                        .build())
                .build();
    }
}
