package com.backend.Skytouch.analytics.apimodel;

import com.backend.Skytouch.common.enums.JobStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class JobFunnelAnalyticsResponse {

    private final UUID jobId;
    private final String jobTitle;
    private final JobStatus jobStatus;
    private final ApplicationFunnelCounts funnel;
    private final double hireRatePercent;
    private final double shortlistToHireRatePercent;
}
