package com.backend.Skytouch.subscription.controller;

import com.backend.Skytouch.authentication.security.SecurityUtils;
import com.backend.Skytouch.company.entity.Company;
import com.backend.Skytouch.company.service.CompanyService;
import com.backend.Skytouch.subscription.apimodel.*;
import com.backend.Skytouch.subscription.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final CompanyService companyService;

    @GetMapping("/plans")
    public ResponseEntity<List<PlanResponse>> getPlans() {
        return ResponseEntity.ok(subscriptionService.getAvailablePlans());
    }

    @GetMapping("/current")
    public ResponseEntity<SubscriptionResponse> getCurrentSubscription() {
        UUID companyId = getCurrentCompanyId();
        SubscriptionResponse response = subscriptionService.getCurrentSubscription(companyId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/usage")
    public ResponseEntity<UsageResponse> getUsage() {
        UUID companyId = getCurrentCompanyId();
        UsageResponse response = subscriptionService.getUsage(companyId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/subscribe")
    public ResponseEntity<SubscriptionResponse> subscribe(@Valid @RequestBody SubscribeRequest request) {
        UUID companyId = getCurrentCompanyId();
        SubscriptionResponse response = subscriptionService.subscribe(companyId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/activate")
    public ResponseEntity<SubscriptionResponse> activate() {
        UUID companyId = getCurrentCompanyId();
        SubscriptionResponse response = subscriptionService.activateSubscription(companyId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/renew")
    public ResponseEntity<SubscriptionResponse> renew() {
        UUID companyId = getCurrentCompanyId();
        SubscriptionResponse response = subscriptionService.renewSubscription(companyId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/upgrade")
    public ResponseEntity<SubscriptionResponse> upgrade(@RequestBody UpgradeRequest request) {
        UUID companyId = getCurrentCompanyId();
        SubscriptionResponse response = subscriptionService.upgradeSubscription(companyId, request.getPlan());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cancel")
    public ResponseEntity<SubscriptionResponse> cancel() {
        UUID companyId = getCurrentCompanyId();
        SubscriptionResponse response = subscriptionService.cancelSubscription(companyId);
        return ResponseEntity.ok(response);
    }

    private UUID getCurrentCompanyId() {
        Company company = companyService.getLinkedCompany(SecurityUtils.getCurrentUser().getEmail());
        return company.getId();
    }
}
