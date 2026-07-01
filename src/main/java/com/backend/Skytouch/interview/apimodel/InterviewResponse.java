package com.backend.Skytouch.interview.apimodel;

import com.backend.Skytouch.common.enums.InterviewMode;
import com.backend.Skytouch.common.enums.InterviewStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class InterviewResponse {

    private final UUID id;
    private final UUID applicationId;
    private final String jobTitle;
    private final String companyName;
    private final String seekerName;
    private final LocalDateTime scheduledAt;
    private final int durationMinutes;
    private final InterviewMode mode;
    private final String locationOrLink;
    private final InterviewStatus status;
    private final String notes;
    private final LocalDateTime createdAt;
}
