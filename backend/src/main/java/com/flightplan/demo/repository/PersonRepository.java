package com.flightplan.demo.repository;

import com.flightplan.demo.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
    
    List<Person> findByBaseId(Long baseId);
    
    List<Person> findByBaseIdAndRole(Long baseId, Person.Role role);
    
    List<Person> findByBaseIdAndStatus(Long baseId, Person.Status status);
    
    @Query("SELECT p FROM Person p WHERE p.base.id = :baseId AND p.status = 'ACTIVE' AND p.role = :role")
    List<Person> findActiveByBaseAndRole(@Param("baseId") Long baseId, @Param("role") Person.Role role);
}
