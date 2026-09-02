package com.backend.Skytouch.interview.apimodel;

import com.backend.Skytouch.common.enums.InterviewMode;
import com.backend.Skytouch.common.enums.InterviewStatus;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class InterviewUpdateRequest {

    private LocalDateTime scheduledAt;

    @Min(15)
    private Integer durationMinutes;

    private InterviewMode mode;

    private String locationOrLink;

    private InterviewStatus status;

    private String notes;
}
