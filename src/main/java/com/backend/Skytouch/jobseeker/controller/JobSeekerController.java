package com.backend.Skytouch.jobseeker.controller;

import com.backend.Skytouch.authentication.security.SecurityUtils;
import com.backend.Skytouch.jobseeker.apimodel.JobSeekerDashboardResponse;
import com.backend.Skytouch.jobseeker.apimodel.JobSeekerKycRequest;
import com.backend.Skytouch.jobseeker.apimodel.JobSeekerOnboardingRequest;
import com.backend.Skytouch.jobseeker.apimodel.JobSeekerResponse;
import com.backend.Skytouch.jobseeker.service.JobSeekerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/job-seekers")
@RequiredArgsConstructor
public class JobSeekerController {

    private final JobSeekerService jobSeekerService;

    @GetMapping("/me")
    public JobSeekerResponse getMe() {
        return jobSeekerService.findByEmail(SecurityUtils.getCurrentUser().getEmail());
    }

    @GetMapping("/me/dashboard")
    public JobSeekerDashboardResponse getDashboard() {
        return jobSeekerService.getDashboard(SecurityUtils.getCurrentUser().getEmail());
    }

    @PatchMapping(value = "/me/onboarding", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public JobSeekerResponse updateOnboarding(@Valid @ModelAttribute JobSeekerOnboardingRequest request) {
        return jobSeekerService.updateOnboarding(
                SecurityUtils.getCurrentUser().getEmail(),
                request);
    }

    @PatchMapping("/me/kyc")
    public JobSeekerResponse updateKyc(@Valid @RequestBody JobSeekerKycRequest request) {
        return jobSeekerService.updateKyc(SecurityUtils.getCurrentUser().getEmail(), request);
    }

    @GetMapping
    public List<JobSeekerResponse> list() {
        return jobSeekerService.findAll();
    }

    @GetMapping("/{id}")
    public JobSeekerResponse getById(@PathVariable UUID id) {
        return jobSeekerService.findById(id);
    }
}
