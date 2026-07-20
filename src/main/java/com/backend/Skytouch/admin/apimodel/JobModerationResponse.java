package com.backend.Skytouch.admin.apimodel;

import com.backend.Skytouch.common.enums.EmploymentType;
import com.backend.Skytouch.common.enums.JobStatus;
import com.backend.Skytouch.common.enums.WorkMode;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class JobModerationResponse {

    private final UUID id;
    private final String companyName;
    private final String title;
    private final EmploymentType employmentType;
    private final WorkMode workMode;
    private final Long salaryMin;
    private final Long salaryMax;
    private final String locationState;
    private final JobStatus status;
    private final LocalDateTime createdAt;
}
