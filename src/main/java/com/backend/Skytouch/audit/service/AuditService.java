package com.backend.Skytouch.audit.service;

import com.backend.Skytouch.audit.apimodel.AuditEventResponse;
import com.backend.Skytouch.audit.entity.AuditEvent;
import com.backend.Skytouch.audit.repository.AuditEventRepository;
import com.backend.Skytouch.common.apimodel.PageResponse;
import com.backend.Skytouch.common.enums.AuditAction;
import com.backend.Skytouch.common.enums.AuditTargetType;
import com.backend.Skytouch.common.exception.ResourceNotFoundException;
import com.backend.Skytouch.common.mapper.AuditMapper;
import com.backend.Skytouch.common.util.PaginationUtils;
import com.backend.Skytouch.user.entity.Users;
import com.backend.Skytouch.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditEventRepository auditEventRepository;
    private final UserRepository userRepository;
    private final AuditMapper auditMapper;

    @Transactional
    public void record(UUID adminUserId, AuditAction action, AuditTargetType targetType, UUID targetId, String details) {
        Users admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found: " + adminUserId));

        AuditEvent event = AuditEvent.builder()
                .adminUser(admin)
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .details(details)
                .build();
        auditEventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditEventResponse> findAll(int page, int size) {
        Pageable pageable = PaginationUtils.pageable(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AuditEvent> results = auditEventRepository.findAllByOrderByCreatedAtDesc(pageable);
        return PaginationUtils.mapPage(results, auditMapper::toResponse);
    }
}
