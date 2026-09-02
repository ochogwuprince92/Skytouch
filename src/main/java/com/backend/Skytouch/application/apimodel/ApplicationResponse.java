package com.backend.Skytouch.application.apimodel;

import com.backend.Skytouch.common.enums.ApplicationStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ApplicationResponse {

    private final UUID id;
    private final UUID jobId;
    private final String jobTitle;
    private final UUID companyId;
    private final String companyName;
    private final UUID jobSeekerId;
    private final String seekerName;
    private final String seekerEmail;
    private final ApplicationStatus status;
    private final String coverLetter;
    private final String cvUrl;
    private final LocalDateTime appliedAt;
    private final LocalDateTime updatedAt;
    private final String comment;
}
