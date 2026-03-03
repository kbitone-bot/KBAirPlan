package com.flightplan.demo.repository;

import com.flightplan.demo.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    List<AuditLog> findByDraftId(Long draftId);
    
    List<AuditLog> findByItemId(Long itemId);
}
