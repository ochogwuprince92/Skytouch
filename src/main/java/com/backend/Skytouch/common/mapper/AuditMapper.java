package com.backend.Skytouch.common.mapper;

import com.backend.Skytouch.audit.apimodel.AuditEventResponse;
import com.backend.Skytouch.audit.entity.AuditEvent;
import org.springframework.stereotype.Component;

@Component
public class AuditMapper {

    public AuditEventResponse toResponse(AuditEvent event) {
        return AuditEventResponse.builder()
                .id(event.getId())
                .adminEmail(event.getAdminUser().getEmail())
                .action(event.getAction())
                .targetType(event.getTargetType())
                .targetId(event.getTargetId())
                .details(event.getDetails())
                .createdAt(event.getCreatedAt())
                .build();
    }
}
