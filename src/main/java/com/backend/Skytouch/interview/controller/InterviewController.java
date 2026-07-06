package com.backend.Skytouch.interview.controller;

import com.backend.Skytouch.authentication.security.SecurityUtils;
import com.backend.Skytouch.common.apimodel.PageResponse;
import com.backend.Skytouch.interview.apimodel.InterviewCreateRequest;
import com.backend.Skytouch.interview.apimodel.InterviewResponse;
import com.backend.Skytouch.interview.apimodel.InterviewUpdateRequest;
import com.backend.Skytouch.interview.service.InterviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    @PostMapping("/api/applications/{applicationId}/interviews")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('EMPLOYER')")
    public InterviewResponse schedule(
            @PathVariable UUID applicationId,
            @Valid @RequestBody InterviewCreateRequest request) {
        return interviewService.schedule(SecurityUtils.getCurrentUser().getEmail(), applicationId, request);
    }

    @GetMapping("/api/applications/{applicationId}/interviews")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'JOB_SEEKER')")
    public InterviewResponse getForApplication(@PathVariable UUID applicationId) {
        return interviewService.findForApplication(SecurityUtils.getCurrentUser().getEmail(), applicationId);
    }

    @PatchMapping("/api/interviews/{id}")
    @PreAuthorize("hasRole('EMPLOYER')")
    public InterviewResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody InterviewUpdateRequest request) {
        return interviewService.update(SecurityUtils.getCurrentUser().getEmail(), id, request);
    }

    @GetMapping("/api/interviews/me")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public PageResponse<InterviewResponse> getMyUpcoming(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return interviewService.findMyUpcoming(SecurityUtils.getCurrentUser().getEmail(), page, size);
    }
}
