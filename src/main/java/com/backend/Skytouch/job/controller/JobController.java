package com.backend.Skytouch.job.controller;

import com.backend.Skytouch.application.apimodel.ApplicationCreateRequest;
import com.backend.Skytouch.application.apimodel.ApplicationResponse;
import com.backend.Skytouch.application.apimodel.ApplicationStatusUpdateRequest;
import com.backend.Skytouch.application.service.ApplicationExportService;
import com.backend.Skytouch.application.service.ApplicationService;
import com.backend.Skytouch.authentication.security.SecurityUtils;
import com.backend.Skytouch.common.apimodel.PageResponse;
import com.backend.Skytouch.common.enums.EmploymentType;
import com.backend.Skytouch.common.enums.UserRole;
import com.backend.Skytouch.common.enums.WorkMode;
import com.backend.Skytouch.job.apimodel.JobCreateRequest;
import com.backend.Skytouch.job.apimodel.JobResponse;
import com.backend.Skytouch.job.apimodel.JobUpdateRequest;
import com.backend.Skytouch.job.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;
    private final ApplicationService applicationService;
    private final ApplicationExportService applicationExportService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public JobResponse create(@Valid @RequestBody JobCreateRequest request) {
        return jobService.create(SecurityUtils.getCurrentUser().getEmail(), request);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('EMPLOYER')")
    public PageResponse<JobResponse> getMyCompanyJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return jobService.findMyCompanyJobs(SecurityUtils.getCurrentUser().getEmail(), page, size);
    }

    @GetMapping
    public PageResponse<JobResponse> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) EmploymentType employmentType,
            @RequestParam(required = false) WorkMode workMode,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String industry,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return jobService.search(keyword, employmentType, workMode, state, industry, page, size);
    }

    @GetMapping("/{id}")
    public JobResponse getById(@PathVariable UUID id) {
        try {
            var user = SecurityUtils.getCurrentUser();
            boolean isEmployer = user.getRole() == UserRole.EMPLOYER;
            boolean isJobSeeker = user.getRole() == UserRole.JOB_SEEKER;
            return jobService.findById(id, user.getEmail(), isEmployer, isJobSeeker);
        } catch (Exception e) {
            // Allow unauthenticated access to view active jobs
            return jobService.findByIdPublic(id);
        }
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public JobResponse update(@PathVariable UUID id, @Valid @RequestBody JobUpdateRequest request) {
        return jobService.update(SecurityUtils.getCurrentUser().getEmail(), id, request);
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public JobResponse publish(@PathVariable UUID id) {
        return jobService.publish(SecurityUtils.getCurrentUser().getEmail(), id);
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public JobResponse close(@PathVariable UUID id) {
        return jobService.close(SecurityUtils.getCurrentUser().getEmail(), id);
    }

    @PostMapping(value = "/{id}/applications", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ApplicationResponse apply(
            @PathVariable UUID id,
            @Valid @ModelAttribute ApplicationCreateRequest request) {
        return applicationService.apply(SecurityUtils.getCurrentUser().getEmail(), id, request);
    }

    @GetMapping("/{id}/applications")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public PageResponse<ApplicationResponse> getJobApplications(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return applicationService.findApplicationsForJob(
                SecurityUtils.getCurrentUser().getEmail(), id, page, size);
    }

    @GetMapping("/{id}/applications/{applicationId}")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ApplicationResponse getApplication(
            @PathVariable UUID id,
            @PathVariable UUID applicationId) {
        return applicationService.findApplicationForJob(
                SecurityUtils.getCurrentUser().getEmail(), id, applicationId);
    }

    @GetMapping("/{id}/applications/export")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<byte[]> exportJobApplications(@PathVariable UUID id) {
        byte[] csv = applicationExportService.exportJobApplications(
                SecurityUtils.getCurrentUser().getEmail(), id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"job-applications-" + id + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @PatchMapping("/{id}/applications/{applicationId}")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ApplicationResponse updateApplicationStatus(
            @PathVariable UUID id,
            @PathVariable UUID applicationId,
            @Valid @RequestBody ApplicationStatusUpdateRequest request) {
        return applicationService.updateStatus(
                SecurityUtils.getCurrentUser().getEmail(), id, applicationId, request);
    }
}
