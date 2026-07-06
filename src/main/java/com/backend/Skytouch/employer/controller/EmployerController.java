package com.backend.Skytouch.employer.controller;

import com.backend.Skytouch.analytics.apimodel.EmployerFunnelAnalyticsResponse;
import com.backend.Skytouch.analytics.apimodel.JobFunnelAnalyticsResponse;
import com.backend.Skytouch.analytics.service.EmployerAnalyticsService;
import com.backend.Skytouch.application.service.ApplicationExportService;
import com.backend.Skytouch.authentication.security.SecurityUtils;
import com.backend.Skytouch.common.apimodel.PageResponse;
import com.backend.Skytouch.employer.apimodel.EmployerDashboardResponse;
import com.backend.Skytouch.employer.apimodel.EmployerProfileRequest;
import com.backend.Skytouch.employer.apimodel.EmployerResponse;
import com.backend.Skytouch.employer.service.EmployerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/employers")
@RequiredArgsConstructor
public class EmployerController {

    private final EmployerService employerService;
    private final EmployerAnalyticsService employerAnalyticsService;
    private final ApplicationExportService applicationExportService;

    @GetMapping("/me")
    public EmployerResponse getMe() {
        return employerService.findByEmail(SecurityUtils.getCurrentUser().getEmail());
    }

    @GetMapping("/me/dashboard")
    public EmployerDashboardResponse getDashboard() {
        return employerService.getDashboard(SecurityUtils.getCurrentUser().getEmail());
    }

    @GetMapping("/me/analytics")
    public EmployerFunnelAnalyticsResponse getAnalytics() {
        return employerAnalyticsService.getFunnelAnalytics(SecurityUtils.getCurrentUser().getEmail());
    }

    @GetMapping("/me/analytics/jobs/{jobId}")
    public JobFunnelAnalyticsResponse getJobAnalytics(@PathVariable UUID jobId) {
        return employerAnalyticsService.getJobFunnelAnalytics(
                SecurityUtils.getCurrentUser().getEmail(), jobId);
    }

    @GetMapping("/me/applications/export")
    public ResponseEntity<byte[]> exportCompanyApplications() {
        byte[] csv = applicationExportService.exportCompanyApplications(
                SecurityUtils.getCurrentUser().getEmail());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"company-applications.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @PatchMapping("/me/profile")
    public EmployerResponse updateProfile(@Valid @RequestBody EmployerProfileRequest request) {
        return employerService.updateProfile(SecurityUtils.getCurrentUser().getEmail(), request);
    }

    @GetMapping
    public PageResponse<EmployerResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return employerService.findAll(page, size);
    }

    @GetMapping("/{id}")
    public EmployerResponse getById(@PathVariable UUID id) {
        return employerService.findById(id);
    }
}
