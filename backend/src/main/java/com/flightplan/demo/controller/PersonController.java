package com.flightplan.demo.controller;

import com.flightplan.demo.dto.PersonDto;
import com.flightplan.demo.service.PersonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/personnel")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PersonController {
    
    private final PersonService personService;
    
    @GetMapping
    public ResponseEntity<List<PersonDto>> getAllPersons(@RequestParam(required = false) Long baseId) {
        return ResponseEntity.ok(personService.getAllPersons(baseId));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<PersonDto> getPerson(@PathVariable Long id) {
        return ResponseEntity.ok(personService.getPerson(id));
    }
    
    @PostMapping
    public ResponseEntity<PersonDto> createPerson(@RequestBody PersonDto dto) {
        return ResponseEntity.ok(personService.createPerson(dto));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<PersonDto> updatePerson(@PathVariable Long id, @RequestBody PersonDto dto) {
        return ResponseEntity.ok(personService.updatePerson(id, dto));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePerson(@PathVariable Long id) {
        personService.deletePerson(id);
        return ResponseEntity.ok().build();
    }
}
