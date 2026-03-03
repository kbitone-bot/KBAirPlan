package com.flightplan.demo.controller;

import com.flightplan.demo.entity.Base;
import com.flightplan.demo.repository.BaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bases")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BaseController {
    
    private final BaseRepository baseRepository;
    
    @GetMapping
    public ResponseEntity<List<Base>> getAllBases() {
        return ResponseEntity.ok(baseRepository.findAll());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Base> getBase(@PathVariable Long id) {
        return baseRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<Base> createBase(@RequestBody Base base) {
        return ResponseEntity.ok(baseRepository.save(base));
    }
}
