package com.backend.Skytouch.jobseeker.apimodel;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JobSeekerDashboardStats {

    private final long applicationsCount;
    private final long savedJobsCount;
    private final long interviewsCount;
    private final long pendingOffersCount;
    private final long jobAlertsCount;
}
