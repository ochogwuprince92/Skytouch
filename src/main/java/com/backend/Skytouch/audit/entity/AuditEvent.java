package com.backend.Skytouch.audit.entity;

import com.backend.Skytouch.common.enums.AuditAction;
import com.backend.Skytouch.common.enums.AuditTargetType;
import com.backend.Skytouch.user.entity.Users;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_events")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEvent {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "admin_user_id", nullable = false)
    private Users adminUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditAction action;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private AuditTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    @Column(columnDefinition = "TEXT")
    private String details;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
