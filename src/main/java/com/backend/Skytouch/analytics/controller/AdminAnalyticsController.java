package com.backend.Skytouch.analytics.controller;

import com.backend.Skytouch.analytics.apimodel.AdminPlatformAnalyticsResponse;
import com.backend.Skytouch.analytics.service.AdminAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminAnalyticsController {

    private final AdminAnalyticsService adminAnalyticsService;

    @GetMapping("/analytics")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminPlatformAnalyticsResponse getPlatformAnalytics() {
        return adminAnalyticsService.getPlatformAnalytics();
    }
}
