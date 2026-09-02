package com.backend.Skytouch.audit.controller;

import com.backend.Skytouch.audit.apimodel.AuditEventResponse;
import com.backend.Skytouch.audit.service.AuditService;
import com.backend.Skytouch.common.apimodel.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminAuditController {

    private final AuditService auditService;

    @GetMapping("/audit-events")
    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<AuditEventResponse> listAuditEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return auditService.findAll(page, size);
    }
}
