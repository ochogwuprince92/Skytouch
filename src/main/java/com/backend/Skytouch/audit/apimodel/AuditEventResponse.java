package com.backend.Skytouch.audit.apimodel;

import com.backend.Skytouch.common.enums.AuditAction;
import com.backend.Skytouch.common.enums.AuditTargetType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class AuditEventResponse {

    private final UUID id;
    private final String adminEmail;
    private final AuditAction action;
    private final AuditTargetType targetType;
    private final UUID targetId;
    private final String details;
    private final LocalDateTime createdAt;
}
