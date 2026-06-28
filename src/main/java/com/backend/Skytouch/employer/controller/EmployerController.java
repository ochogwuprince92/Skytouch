package com.backend.Skytouch.employer.controller;

import com.backend.Skytouch.authentication.security.SecurityUtils;
import com.backend.Skytouch.employer.apimodel.EmployerDashboardResponse;
import com.backend.Skytouch.employer.apimodel.EmployerProfileRequest;
import com.backend.Skytouch.employer.apimodel.EmployerResponse;
import com.backend.Skytouch.employer.service.EmployerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/employers")
@RequiredArgsConstructor
public class EmployerController {

    private final EmployerService employerService;

    @GetMapping("/me")
    public EmployerResponse getMe() {
        return employerService.findByEmail(SecurityUtils.getCurrentUser().getEmail());
    }

    @GetMapping("/me/dashboard")
    public EmployerDashboardResponse getDashboard() {
        return employerService.getDashboard(SecurityUtils.getCurrentUser().getEmail());
    }

    @PatchMapping("/me/profile")
    public EmployerResponse updateProfile(@Valid @RequestBody EmployerProfileRequest request) {
        return employerService.updateProfile(SecurityUtils.getCurrentUser().getEmail(), request);
    }

    @GetMapping
    public List<EmployerResponse> list() {
        return employerService.findAll();
    }

    @GetMapping("/{id}")
    public EmployerResponse getById(@PathVariable UUID id) {
        return employerService.findById(id);
    }
}
