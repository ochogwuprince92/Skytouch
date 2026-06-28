package com.backend.Skytouch.job.controller;

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
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('EMPLOYER')")
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
        var user = SecurityUtils.getCurrentUser();
        boolean isEmployer = user.getRole() == UserRole.EMPLOYER;
        return jobService.findById(id, user.getEmail(), isEmployer);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYER')")
    public JobResponse update(@PathVariable UUID id, @Valid @RequestBody JobUpdateRequest request) {
        return jobService.update(SecurityUtils.getCurrentUser().getEmail(), id, request);
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('EMPLOYER')")
    public JobResponse publish(@PathVariable UUID id) {
        return jobService.publish(SecurityUtils.getCurrentUser().getEmail(), id);
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasRole('EMPLOYER')")
    public JobResponse close(@PathVariable UUID id) {
        return jobService.close(SecurityUtils.getCurrentUser().getEmail(), id);
    }
}
