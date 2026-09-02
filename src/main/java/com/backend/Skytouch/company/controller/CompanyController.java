package com.backend.Skytouch.company.controller;

import com.backend.Skytouch.authentication.security.SecurityUtils;
import com.backend.Skytouch.company.apimodel.CompanyCreateRequest;
import com.backend.Skytouch.company.apimodel.CompanyResponse;
import com.backend.Skytouch.company.apimodel.CompanyUpdateRequest;
import com.backend.Skytouch.company.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompanyResponse create(@Valid @RequestBody CompanyCreateRequest request) {
        return companyService.createForEmployer(SecurityUtils.getCurrentUser().getEmail(), request);
    }

    @GetMapping("/me")
    public CompanyResponse getMyCompany() {
        return companyService.findMyCompany(SecurityUtils.getCurrentUser().getEmail());
    }

    @PatchMapping("/me")
    public CompanyResponse updateMyCompany(@Valid @RequestBody CompanyUpdateRequest request) {
        return companyService.updateMyCompany(SecurityUtils.getCurrentUser().getEmail(), request);
    }

    @GetMapping("/{id}")
    public CompanyResponse getById(@PathVariable UUID id) {
        return companyService.findById(id);
    }
}
