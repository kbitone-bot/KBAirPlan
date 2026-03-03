package com.flightplan.demo.algorithm;

import com.flightplan.demo.entity.*;
import com.flightplan.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Component
@RequiredArgsConstructor
public class ConstraintChecker {
    
    private final UnavailabilityRepository unavailabilityRepository;
    private final PersonQualificationRepository personQualificationRepository;
    private final FlightPlanItemRepository flightPlanItemRepository;
    
    /**
     * 하드 제약 위반 여부 확인
     */
    public List<String> checkHardConstraints(
            Person person,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            List<Person> alreadyAssigned,
            MissionTemplate mission) {
        
        List<String> violations = new ArrayList<>();
        
        // 1. 부재 중 배정 금지
        if (isUnavailable(person.getId(), date, startTime, endTime)) {
            violations.add("부재 중: " + person.getName());
        }
        
        // 2. 필수 자격 미충족 금지
        if (!hasRequiredQualifications(person.getId(), mission)) {
            violations.add("필수 자격 미충족: " + person.getName());
        }
        
        // 3. 동일 시간 중복 배정 금지
        if (hasTimeConflict(person.getId(), date, startTime, endTime, alreadyAssigned)) {
            violations.add("시간 중복: " + person.getName());
        }
        
        return violations;
    }
    
    /**
     * 소프트 제약 확인 (페널티 계산용)
     */
    public SoftConstraintResult checkSoftConstraints(
            Person person,
            LocalDate date,
            List<Person> allPersonnel,
            Map<Long, Integer> assignmentCounts) {
        
        SoftConstraintResult result = new SoftConstraintResult();
        
        // 공평성: 현재까지 배정 횟수와 비교
        int currentAssignments = assignmentCounts.getOrDefault(person.getId(), 0);
        double avgAssignments = assignmentCounts.values().stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0);
        result.setFairnessDeviation(currentAssignments - avgAssignments);
        
        // 피로도: 연속 배정 확인
        result.setConsecutiveDays(countConsecutiveDays(person.getId(), date));
        
        // 주말/야간 배정 여부
        result.setWeekend(date.getDayOfWeek().getValue() >= 6);
        
        return result;
    }
    
    private boolean isUnavailable(Long personId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        List<Unavailability> unavailabilities = unavailabilityRepository.findByPersonId(personId);
        return unavailabilities.stream()
                .anyMatch(u -> u.overlaps(date, startTime, endTime));
    }
    
    private boolean hasRequiredQualifications(Long personId, MissionTemplate mission) {
        List<Long> requiredQuals = mission.getRequiredQualificationIds();
        if (requiredQuals == null || requiredQuals.isEmpty()) {
            return true;
        }
        
        List<PersonQualification> personQuals = personQualificationRepository.findByPersonId(personId);
        Set<Long> personQualIds = new HashSet<>();
        for (PersonQualification pq : personQuals) {
            if (pq.isValidAt(java.time.LocalDate.now())) {
                personQualIds.add(pq.getQualification().getId());
            }
        }
        
        return personQualIds.containsAll(requiredQuals);
    }
    
    private boolean hasTimeConflict(Long personId, LocalDate date, LocalTime startTime, LocalTime endTime, 
                                     List<Person> alreadyAssigned) {
        // 같은 항목에 이미 배정된 인원과는 중복 확인 필요 없음
        // 다른 항목과의 중복은 DB에서 확인
        List<FlightPlanItem> items = flightPlanItemRepository.findByPersonIdAndDate(personId, date);
        for (FlightPlanItem item : items) {
            if (timesOverlap(startTime, endTime, item.getStartTime(), item.getEndTime())) {
                return true;
            }
        }
        return false;
    }
    
    private boolean timesOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return start1.isBefore(end2) && end1.isAfter(start2);
    }
    
    private int countConsecutiveDays(Long personId, LocalDate date) {
        // 최근 7일간 배정 횟수 확인
        int consecutive = 0;
        for (int i = 1; i <= 7; i++) {
            LocalDate checkDate = date.minusDays(i);
            List<FlightPlanItem> items = flightPlanItemRepository.findByPersonIdAndDate(personId, checkDate);
            if (!items.isEmpty()) {
                consecutive++;
            } else {
                break;
            }
        }
        return consecutive;
    }
    
    public static class SoftConstraintResult {
        private double fairnessDeviation;
        private int consecutiveDays;
        private boolean weekend;
        private boolean nightFlight;
        
        // Getters and Setters
        public double getFairnessDeviation() { return fairnessDeviation; }
        public void setFairnessDeviation(double fairnessDeviation) { this.fairnessDeviation = fairnessDeviation; }
        public int getConsecutiveDays() { return consecutiveDays; }
        public void setConsecutiveDays(int consecutiveDays) { this.consecutiveDays = consecutiveDays; }
        public boolean isWeekend() { return weekend; }
        public void setWeekend(boolean weekend) { this.weekend = weekend; }
        public boolean isNightFlight() { return nightFlight; }
        public void setNightFlight(boolean nightFlight) { this.nightFlight = nightFlight; }
    }
}
