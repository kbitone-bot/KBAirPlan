package com.flightplan.demo.service;

import com.flightplan.demo.dto.PersonDto;
import com.flightplan.demo.entity.*;
import com.flightplan.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PersonService {
    
    private final PersonRepository personRepository;
    private final BaseRepository baseRepository;
    private final PersonQualificationRepository personQualificationRepository;
    private final QualificationRepository qualificationRepository;
    
    @Transactional(readOnly = true)
    public List<PersonDto> getAllPersons(Long baseId) {
        List<Person> persons = baseId != null ? 
                personRepository.findByBaseId(baseId) : 
                personRepository.findAll();
        
        return persons.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public PersonDto getPerson(Long id) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Person not found: " + id));
        return convertToDto(person);
    }
    
    @Transactional
    public PersonDto createPerson(PersonDto dto) {
        Base base = baseRepository.findById(dto.getBaseId())
                .orElseThrow(() -> new RuntimeException("Base not found"));
        
        Person person = Person.builder()
                .base(base)
                .role(dto.getRole())
                .name(dto.getName())
                .rank(dto.getRank())
                .status(dto.getStatus() != null ? dto.getStatus() : Person.Status.ACTIVE)
                .totalFlightHours(dto.getTotalFlightHours())
                .monthlyFlightCount(dto.getMonthlyFlightCount())
                .build();
        
        person = personRepository.save(person);
        return convertToDto(person);
    }
    
    @Transactional
    public PersonDto updatePerson(Long id, PersonDto dto) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Person not found: " + id));
        
        if (dto.getName() != null) person.setName(dto.getName());
        if (dto.getRank() != null) person.setRank(dto.getRank());
        if (dto.getStatus() != null) person.setStatus(dto.getStatus());
        if (dto.getTotalFlightHours() != null) person.setTotalFlightHours(dto.getTotalFlightHours());
        if (dto.getMonthlyFlightCount() != null) person.setMonthlyFlightCount(dto.getMonthlyFlightCount());
        
        person = personRepository.save(person);
        return convertToDto(person);
    }
    
    @Transactional
    public void deletePerson(Long id) {
        personRepository.deleteById(id);
    }
    
    private PersonDto convertToDto(Person person) {
        List<PersonQualification> pqs = personQualificationRepository.findByPersonId(person.getId());
        
        List<PersonDto.QualificationDto> quals = pqs.stream()
                .map(pq -> PersonDto.QualificationDto.builder()
                        .id(pq.getQualification().getId())
                        .name(pq.getQualification().getName())
                        .type(pq.getQualification().getType().name())
                        .validFrom(pq.getValidFrom() != null ? pq.getValidFrom().toString() : null)
                        .validTo(pq.getValidTo() != null ? pq.getValidTo().toString() : null)
                        .build())
                .collect(Collectors.toList());
        
        return PersonDto.builder()
                .id(person.getId())
                .baseId(person.getBase().getId())
                .baseName(person.getBase().getName())
                .role(person.getRole())
                .name(person.getName())
                .rank(person.getRank())
                .status(person.getStatus())
                .totalFlightHours(person.getTotalFlightHours())
                .monthlyFlightCount(person.getMonthlyFlightCount())
                .qualifications(quals)
                .createdAt(person.getCreatedAt())
                .build();
    }
}
