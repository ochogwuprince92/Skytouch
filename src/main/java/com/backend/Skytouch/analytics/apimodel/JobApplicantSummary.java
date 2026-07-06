package com.backend.Skytouch.analytics.apimodel;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class JobApplicantSummary {

    private final UUID jobId;
    private final String jobTitle;
    private final long applicantCount;
    private final long hiredCount;
}
