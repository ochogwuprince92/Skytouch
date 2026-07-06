package com.backend.Skytouch.jobalert.controller;

import com.backend.Skytouch.authentication.security.SecurityUtils;
import com.backend.Skytouch.common.apimodel.PageResponse;
import com.backend.Skytouch.jobalert.apimodel.JobAlertCreateRequest;
import com.backend.Skytouch.jobalert.apimodel.JobAlertResponse;
import com.backend.Skytouch.jobalert.apimodel.JobAlertUpdateRequest;
import com.backend.Skytouch.jobalert.service.JobAlertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/job-alerts")
@RequiredArgsConstructor
public class JobAlertController {

    private final JobAlertService jobAlertService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public JobAlertResponse create(@Valid @RequestBody JobAlertCreateRequest request) {
        return jobAlertService.create(SecurityUtils.getCurrentUser().getEmail(), request);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public PageResponse<JobAlertResponse> getMyAlerts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return jobAlertService.findMyAlerts(SecurityUtils.getCurrentUser().getEmail(), page, size);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public JobAlertResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody JobAlertUpdateRequest request) {
        return jobAlertService.update(SecurityUtils.getCurrentUser().getEmail(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public void delete(@PathVariable UUID id) {
        jobAlertService.delete(SecurityUtils.getCurrentUser().getEmail(), id);
    }
}
