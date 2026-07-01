package com.backend.Skytouch.analytics.apimodel;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class EmployerFunnelAnalyticsResponse {

    private final boolean companyLinked;
    private final ApplicationFunnelCounts funnel;
    private final double hireRatePercent;
    private final double shortlistToHireRatePercent;
    private final List<JobApplicantSummary> topJobsByApplicants;
}
