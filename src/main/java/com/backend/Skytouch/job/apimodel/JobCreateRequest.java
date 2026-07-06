package com.backend.Skytouch.job.apimodel;

import com.backend.Skytouch.common.enums.EmploymentType;
import com.backend.Skytouch.common.enums.WorkMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class JobCreateRequest {

    @NotBlank(message = "Job title is required")
    @Size(max = 255, message = "Job title must not exceed 255 characters")
    private String title;

    @NotBlank(message = "Job description is required")
    private String description;

    private String requirements;

    @NotNull(message = "Employment type is required")
    private EmploymentType employmentType;

    @NotNull(message = "Work mode is required")
    private WorkMode workMode;

    private Long salaryMin;

    private Long salaryMax;

    @Size(max = 10, message = "Salary currency must not exceed 10 characters")
    private String salaryCurrency;

    @Size(max = 100, message = "Location state must not exceed 100 characters")
    private String locationState;

    @Size(max = 100, message = "Location LGA must not exceed 100 characters")
    private String locationLga;
}
