package com.backend.Skytouch.application.apimodel;

import com.backend.Skytouch.common.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ApplicationStatusUpdateRequest {

    @NotNull(message = "Application status is required")
    private ApplicationStatus status;
}
