package com.backend.Skytouch.application.controller;

import com.backend.Skytouch.application.apimodel.ApplicationCreateRequest;
import com.backend.Skytouch.application.apimodel.ApplicationResponse;
import com.backend.Skytouch.application.service.ApplicationExportService;
import com.backend.Skytouch.application.service.ApplicationService;
import com.backend.Skytouch.authentication.security.SecurityUtils;
import com.backend.Skytouch.common.apimodel.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;
    private final ApplicationExportService applicationExportService;

    @GetMapping("/me")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public PageResponse<ApplicationResponse> getMyApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return applicationService.findMyApplications(SecurityUtils.getCurrentUser().getEmail(), page, size);
    }

    @GetMapping("/me/export")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<byte[]> exportMyApplications() {
        byte[] csv = applicationExportService.exportMyApplications(
                SecurityUtils.getCurrentUser().getEmail());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"my-applications.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping("/me/{id}")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ApplicationResponse getMyApplication(@PathVariable UUID id) {
        return applicationService.findMyApplication(SecurityUtils.getCurrentUser().getEmail(), id);
    }

    @PostMapping("/me/{id}/withdraw")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ApplicationResponse withdraw(@PathVariable UUID id) {
        return applicationService.withdraw(SecurityUtils.getCurrentUser().getEmail(), id);
    }
}
