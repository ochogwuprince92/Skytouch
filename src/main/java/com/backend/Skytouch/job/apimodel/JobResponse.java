package com.backend.Skytouch.job.apimodel;

import com.backend.Skytouch.common.enums.EmploymentType;
import com.backend.Skytouch.common.enums.JobStatus;
import com.backend.Skytouch.common.enums.WorkMode;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class JobResponse {

    private final UUID id;
    private final UUID companyId;
    private final String companyName;
    private final String title;
    private final String description;
    private final String requirements;
    private final EmploymentType employmentType;
    private final WorkMode workMode;
    private final Long salaryMin;
    private final Long salaryMax;
    private final String salaryCurrency;
    private final String locationState;
    private final String locationLga;
    private final JobStatus status;
    private final LocalDateTime publishedAt;
    private final LocalDateTime closedAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final Boolean saved;
}
