package com.backend.Skytouch.interview.apimodel;

import com.backend.Skytouch.common.enums.InterviewMode;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class InterviewCreateRequest {

    @NotNull
    @Future
    private LocalDateTime scheduledAt;

    @Min(15)
    private int durationMinutes = 60;

    @NotNull
    private InterviewMode mode;

    private String locationOrLink;

    private String notes;
}
