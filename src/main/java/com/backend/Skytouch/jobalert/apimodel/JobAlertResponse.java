package com.backend.Skytouch.jobalert.apimodel;

import com.backend.Skytouch.common.enums.EmploymentType;
import com.backend.Skytouch.common.enums.WorkMode;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class JobAlertResponse {

    private final UUID id;
    private final String name;
    private final String keyword;
    private final EmploymentType employmentType;
    private final WorkMode workMode;
    private final String locationState;
    private final String industry;
    private final boolean active;
    private final LocalDateTime createdAt;
}
