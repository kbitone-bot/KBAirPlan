package com.flightplan.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column
    private Long draftId;
    
    @Column
    private Long itemId;
    
    @Column(nullable = false)
    private String action;
    
    @Column(columnDefinition = "TEXT")
    private String beforeJson;
    
    @Column(columnDefinition = "TEXT")
    private String afterJson;
    
    @Column
    private String userId;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
}
