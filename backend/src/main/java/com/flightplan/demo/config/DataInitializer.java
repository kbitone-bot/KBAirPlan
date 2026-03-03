package com.flightplan.demo.config;

import com.flightplan.demo.entity.*;
import com.flightplan.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {
    
    private final BaseRepository baseRepository;
    private final PersonRepository personRepository;
    private final QualificationRepository qualificationRepository;
    private final AircraftRepository aircraftRepository;
    private final MissionTemplateRepository missionTemplateRepository;
    private final WeightConfigRepository weightConfigRepository;
    private final UnavailabilityRepository unavailabilityRepository;
    private final PersonQualificationRepository personQualificationRepository;
    
    private final Random random = new Random(42);
    
    @Bean
    @Profile("!test")
    public CommandLineRunner initData() {
        return args -> {
            // 더미 데이터가 이미 있으면 스킵
            if (baseRepository.count() > 0) {
                log.info("Data already exists, skipping initialization");
                return;
            }
            
            log.info("Initializing demo data...");
            
            // 1. 기지 생성
            Base pohang = createBase("포항", "경상북도 포항시");
            Base jeju = createBase("제주", "제주특별자치도");
            
            // 2. 가중치 설정 생성
            createDefaultWeightConfig();
            
            // 3. 자격증 생성
            List<Qualification> qualifications = createQualifications();
            
            // 4. 포항 기지 데이터 (40명)
            createBaseData(pohang, 40, qualifications, Aircraft.Type.FIXED, "P");
            
            // 5. 제주 기지 데이터 (40명)
            createBaseData(jeju, 40, qualifications, Aircraft.Type.ROTARY, "J");
            
            log.info("Demo data initialization completed!");
        };
    }
    
    private Base createBase(String name, String location) {
        Base base = Base.builder()
                .name(name)
                .location(location)
                .build();
        return baseRepository.save(base);
    }
    
    private void createDefaultWeightConfig() {
        WeightConfig config = WeightConfig.builder()
                .name("기본 설정")
                .fairnessWeight(1.0)
                .skillWeight(1.0)
                .fatigueWeight(1.0)
                .continuityWeight(0.5)
                .baseBalanceWeight(0.5)
                .isDefault(true)
                .build();
        weightConfigRepository.save(config);
        
        WeightConfig config2 = WeightConfig.builder()
                .name("공평성 우선")
                .fairnessWeight(2.0)
                .skillWeight(0.5)
                .fatigueWeight(1.0)
                .continuityWeight(0.5)
                .baseBalanceWeight(0.5)
                .isDefault(false)
                .build();
        weightConfigRepository.save(config2);
        
        WeightConfig config3 = WeightConfig.builder()
                .name("숙련도 우선")
                .fairnessWeight(0.5)
                .skillWeight(2.0)
                .fatigueWeight(0.5)
                .continuityWeight(0.3)
                .baseBalanceWeight(0.3)
                .isDefault(false)
                .build();
        weightConfigRepository.save(config3);
    }
    
    private List<Qualification> createQualifications() {
        List<Qualification> quals = new ArrayList<>();
        
        // 항공기 자격
        quals.add(createQualification("전투기 조종 자격", Qualification.Type.AIRCRAFT, "1급"));
        quals.add(createQualification("헬기 조종 자격", Qualification.Type.AIRCRAFT, "1급"));
        quals.add(createQualification("수송기 조종 자격", Qualification.Type.AIRCRAFT, "1급"));
        
        // 임무 자격
        quals.add(createQualification("야간 비행 자격", Qualification.Type.MISSION, "상급"));
        quals.add(createQualification("전술 훈련 자격", Qualification.Type.MISSION, "상급"));
        quals.add(createQualification("수색 구조 자격", Qualification.Type.MISSION, "상급"));
        
        // 특수 자격
        quals.add(createQualification("교관 자격", Qualification.Type.SPECIAL, "1급"));
        quals.add(createQualification("임시 기장 자격", Qualification.Type.SPECIAL, "1급"));
        
        return quals;
    }
    
    private Qualification createQualification(String name, Qualification.Type type, String level) {
        Qualification qual = Qualification.builder()
                .name(name)
                .type(type)
                .level(level)
                .build();
        return qualificationRepository.save(qual);
    }
    
    private void createBaseData(Base base, int personnelCount, 
                                 List<Qualification> qualifications, 
                                 Aircraft.Type aircraftType, String prefix) {
        // 기체 생성 (20대)
        List<Aircraft> aircrafts = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            Aircraft aircraft = Aircraft.builder()
                    .type(aircraftType)
                    .model(aircraftType == Aircraft.Type.FIXED ? "F-15K" : "KUH-1 Surion")
                    .tailNumber(prefix + "-" + String.format("%03d", i))
                    .base(base)
                    .available(true)
                    .maxCrew(aircraftType == Aircraft.Type.FIXED ? 2 : 4)
                    .build();
            aircrafts.add(aircraftRepository.save(aircraft));
        }
        
        // 임무 템플릿 생성
        createMissionTemplates(aircraftType);
        
        // 인원 생성 (조종사 20명, 승무원 20명)
        List<Person> persons = new ArrayList<>();
        
        // 조종사
        String[] pilotNames = {"김", "이", "박", "최", "정", "강", "조", "윤", "장", "임",
                               "한", "오", "서", "신", "권", "황", "안", "송", "류", "전"};
        for (int i = 0; i < 20; i++) {
            String rank = i < 5 ? "중령" : (i < 12 ? "대위" : "중위");
            Person pilot = Person.builder()
                    .base(base)
                    .role(Person.Role.PILOT)
                    .name(pilotNames[i] + "조종사" + (i + 1))
                    .rank(rank)
                    .status(Person.Status.ACTIVE)
                    .totalFlightHours(500 + random.nextInt(2000))
                    .monthlyFlightCount(random.nextInt(20))
                    .build();
            persons.add(personRepository.save(pilot));
        }
        
        // 승무원
        String[] crewNames = {"김", "이", "박", "최", "정", "강", "조", "윤", "장", "임",
                              "한", "오", "서", "신", "권", "황", "안", "송", "류", "전"};
        for (int i = 0; i < 20; i++) {
            String rank = i < 5 ? "상사" : (i < 12 ? "중사" : "하사");
            Person crew = Person.builder()
                    .base(base)
                    .role(Person.Role.CREW)
                    .name(crewNames[i] + "승무원" + (i + 1))
                    .rank(rank)
                    .status(Person.Status.ACTIVE)
                    .totalFlightHours(200 + random.nextInt(1000))
                    .monthlyFlightCount(random.nextInt(15))
                    .build();
            persons.add(personRepository.save(crew));
        }
        
        // 자격증 배정
        for (Person person : persons) {
            int qualCount = 2 + random.nextInt(3);
            List<Qualification> shuffled = new ArrayList<>(qualifications);
            Collections.shuffle(shuffled, random);
            
            for (int i = 0; i < qualCount && i < shuffled.size(); i++) {
                Qualification qual = shuffled.get(i);
                
                // 조종사는 항공기 자격 필요
                if (person.getRole() == Person.Role.PILOT && qual.getType() == Qualification.Type.AIRCRAFT) {
                    continue;
                }
                
                PersonQualification pq = PersonQualification.builder()
                        .person(person)
                        .qualification(qual)
                        .validFrom(LocalDate.now().minusYears(2))
                        .validTo(LocalDate.now().plusYears(2))
                        .build();
                personQualificationRepository.save(pq);
            }
        }
        
        // 부재 데이터 생성 (월 단위 랜덤)
        createUnavailabilityData(persons);
    }
    
    private void createMissionTemplates(Aircraft.Type type) {
        if (type == Aircraft.Type.FIXED) {
            createMissionTemplate(type, "전투 비행", 1, 0, 120);
            createMissionTemplate(type, "훈련 비행", 1, 0, 90);
            createMissionTemplate(type, "초계 비행", 1, 0, 180);
            createMissionTemplate(type, "전술 훈련", 2, 0, 150);
            createMissionTemplate(type, "야간 비행", 1, 0, 90);
        } else {
            createMissionTemplate(type, "수색 구조", 1, 2, 180);
            createMissionTemplate(type, "수송 임무", 1, 2, 120);
            createMissionTemplate(type, "훈련 비행", 1, 1, 90);
            createMissionTemplate(type, "전술 훈련", 1, 3, 150);
            createMissionTemplate(type, "야간 비행", 1, 1, 90);
        }
    }
    
    private void createMissionTemplate(Aircraft.Type type, String name, 
                                        int pilotCount, int crewCount, int duration) {
        MissionTemplate template = MissionTemplate.builder()
                .aircraftType(type)
                .missionName(name)
                .requiredPilotCount(pilotCount)
                .requiredCrewCount(crewCount)
                .durationMinutes(duration)
                .description(name + " 임무 템플릿")
                .requiredQualificationIds(new ArrayList<>())
                .build();
        missionTemplateRepository.save(template);
    }
    
    private void createUnavailabilityData(List<Person> persons) {
        // 30% 인원에게 부재 생성
        int unavailableCount = persons.size() * 3 / 10;
        Collections.shuffle(persons, random);
        
        for (int i = 0; i < unavailableCount; i++) {
            Person person = persons.get(i);
            
            // 부재 유형
            Unavailability.Type[] types = Unavailability.Type.values();
            Unavailability.Type type = types[random.nextInt(types.length)];
            
            // 부재 기간 (1~5일)
            LocalDate startDate = LocalDate.now().plusDays(random.nextInt(30));
            LocalDate endDate = startDate.plusDays(random.nextInt(5) + 1);
            
            Unavailability unavailability = Unavailability.builder()
                    .person(person)
                    .type(type)
                    .startDate(startDate)
                    .endDate(endDate)
                    .reason(type.name() + " 사유")
                    .build();
            
            unavailabilityRepository.save(unavailability);
        }
    }
}
