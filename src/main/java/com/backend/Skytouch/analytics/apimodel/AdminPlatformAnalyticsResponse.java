package com.backend.Skytouch.analytics.apimodel;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminPlatformAnalyticsResponse {

    private final long totalUsers;
    private final long activeJobs;
    private final long totalApplications;
    private final long totalHires;
    private final long pendingCompanies;
    private final ApplicationFunnelCounts applicationFunnel;
    private final double platformHireRatePercent;
}
