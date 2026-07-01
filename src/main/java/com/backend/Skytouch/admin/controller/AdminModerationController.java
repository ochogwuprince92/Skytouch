package com.backend.Skytouch.admin.controller;

import com.backend.Skytouch.admin.apimodel.CompanyModerationResponse;
import com.backend.Skytouch.admin.service.AdminModerationService;
import com.backend.Skytouch.authentication.security.SecurityUtils;
import com.backend.Skytouch.common.apimodel.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminModerationController {

    private final AdminModerationService adminModerationService;

    @GetMapping("/companies/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<CompanyModerationResponse> getPendingCompanies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return adminModerationService.findPendingCompanies(page, size);
    }

    @PatchMapping("/companies/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public CompanyModerationResponse approveCompany(@PathVariable UUID id) {
        return adminModerationService.approveCompany(id, SecurityUtils.getCurrentUser().getId());
    }

    @PatchMapping("/companies/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public CompanyModerationResponse rejectCompany(@PathVariable UUID id) {
        return adminModerationService.rejectCompany(id, SecurityUtils.getCurrentUser().getId());
    }

    @PatchMapping("/users/{id}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    public void suspendUser(@PathVariable UUID id) {
        adminModerationService.suspendUser(id, SecurityUtils.getCurrentUser().getId());
    }

    @PatchMapping("/jobs/{id}/close")
    @PreAuthorize("hasRole('ADMIN')")
    public void forceCloseJob(@PathVariable UUID id) {
        adminModerationService.forceCloseJob(id, SecurityUtils.getCurrentUser().getId());
    }
}
