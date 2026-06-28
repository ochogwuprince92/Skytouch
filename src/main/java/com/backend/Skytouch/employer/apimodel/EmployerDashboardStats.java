package com.backend.Skytouch.employer.apimodel;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmployerDashboardStats {

    private final long activeJobsCount;
    private final long totalApplicantsCount;
    private final long draftJobsCount;
}
