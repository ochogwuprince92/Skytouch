package com.backend.Skytouch.admin.controller;

import com.backend.Skytouch.admin.apimodel.CompanyModerationResponse;
import com.backend.Skytouch.admin.apimodel.JobModerationResponse;
import com.backend.Skytouch.admin.apimodel.UserModerationResponse;
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

    @GetMapping("/companies")
    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<CompanyModerationResponse> listCompanies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        return adminModerationService.listCompanies(page, size, status);
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<UserModerationResponse> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean emailVerified) {
        return adminModerationService.listUsers(page, size, email, status, emailVerified);
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

    @PatchMapping("/users/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public void activateUser(@PathVariable UUID id) {
        adminModerationService.activateUser(id, SecurityUtils.getCurrentUser().getId());
    }

    @PatchMapping("/companies/{id}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    public void suspendCompany(@PathVariable UUID id) {
        adminModerationService.suspendCompany(id, SecurityUtils.getCurrentUser().getId());
    }

    @PatchMapping("/companies/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public void activateCompany(@PathVariable UUID id) {
        adminModerationService.activateCompany(id, SecurityUtils.getCurrentUser().getId());
    }

    @PatchMapping("/jobs/{id}/close")
    @PreAuthorize("hasRole('ADMIN')")
    public void forceCloseJob(@PathVariable UUID id) {
        adminModerationService.forceCloseJob(id, SecurityUtils.getCurrentUser().getId());
    }

    @GetMapping("/jobs")
    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<JobModerationResponse> listJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        return adminModerationService.listJobs(page, size, status);
    }
}
