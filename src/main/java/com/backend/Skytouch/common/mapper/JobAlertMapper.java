package com.backend.Skytouch.common.mapper;

import com.backend.Skytouch.jobalert.apimodel.JobAlertCreateRequest;
import com.backend.Skytouch.jobalert.apimodel.JobAlertResponse;
import com.backend.Skytouch.jobalert.apimodel.JobAlertUpdateRequest;
import com.backend.Skytouch.jobalert.entity.JobAlert;
import com.backend.Skytouch.jobseeker.entity.JobSeeker;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class JobAlertMapper {

    public JobAlert toEntity(JobSeeker jobSeeker, JobAlertCreateRequest request) {
        return JobAlert.builder()
                .jobSeeker(jobSeeker)
                .name(trimToNull(request.getName()))
                .keyword(trimToNull(request.getKeyword()))
                .employmentType(request.getEmploymentType())
                .workMode(request.getWorkMode())
                .locationState(trimToNull(request.getLocationState()))
                .industry(trimToNull(request.getIndustry()))
                .active(true)
                .build();
    }

    public void applyUpdate(JobAlert alert, JobAlertUpdateRequest request) {
        if (request.getName() != null) {
            alert.setName(trimToNull(request.getName()));
        }
        if (request.getKeyword() != null) {
            alert.setKeyword(trimToNull(request.getKeyword()));
        }
        if (request.getEmploymentType() != null) {
            alert.setEmploymentType(request.getEmploymentType());
        }
        if (request.getWorkMode() != null) {
            alert.setWorkMode(request.getWorkMode());
        }
        if (request.getLocationState() != null) {
            alert.setLocationState(trimToNull(request.getLocationState()));
        }
        if (request.getIndustry() != null) {
            alert.setIndustry(trimToNull(request.getIndustry()));
        }
        if (request.getActive() != null) {
            alert.setActive(request.getActive());
        }
    }

    public JobAlertResponse toResponse(JobAlert alert) {
        return JobAlertResponse.builder()
                .id(alert.getId())
                .name(alert.getName())
                .keyword(alert.getKeyword())
                .employmentType(alert.getEmploymentType())
                .workMode(alert.getWorkMode())
                .locationState(alert.getLocationState())
                .industry(alert.getIndustry())
                .active(alert.isActive())
                .createdAt(alert.getCreatedAt())
                .build();
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
