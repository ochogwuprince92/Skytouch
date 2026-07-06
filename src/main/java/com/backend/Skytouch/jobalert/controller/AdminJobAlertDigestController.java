package com.backend.Skytouch.jobalert.controller;

import com.backend.Skytouch.jobalert.apimodel.JobAlertDigestRunResponse;
import com.backend.Skytouch.jobalert.service.JobAlertDigestService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/job-alerts")
@RequiredArgsConstructor
public class AdminJobAlertDigestController {

    private final JobAlertDigestService digestService;

    @PostMapping("/digest/run")
    @PreAuthorize("hasRole('ADMIN')")
    public JobAlertDigestRunResponse runDigest() {
        return digestService.runDigest();
    }
}
