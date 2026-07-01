package com.backend.Skytouch.common.mapper;

import com.backend.Skytouch.common.enums.JobStatus;
import com.backend.Skytouch.company.entity.Company;
import com.backend.Skytouch.job.apimodel.JobCreateRequest;
import com.backend.Skytouch.job.apimodel.JobResponse;
import com.backend.Skytouch.job.apimodel.JobUpdateRequest;
import com.backend.Skytouch.job.entity.Job;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class JobMapper {

    public Job toEntity(JobCreateRequest request, Company company) {
        return Job.builder()
                .company(company)
                .title(request.getTitle().trim())
                .description(request.getDescription().trim())
                .requirements(request.getRequirements())
                .employmentType(request.getEmploymentType())
                .workMode(request.getWorkMode())
                .salaryMin(request.getSalaryMin())
                .salaryMax(request.getSalaryMax())
                .salaryCurrency(StringUtils.hasText(request.getSalaryCurrency())
                        ? request.getSalaryCurrency().trim()
                        : "NGN")
                .locationState(request.getLocationState())
                .locationLga(request.getLocationLga())
                .status(JobStatus.DRAFT)
                .build();
    }

    public void applyUpdate(Job job, JobUpdateRequest request) {
        if (StringUtils.hasText(request.getTitle())) {
            job.setTitle(request.getTitle().trim());
        }
        if (StringUtils.hasText(request.getDescription())) {
            job.setDescription(request.getDescription().trim());
        }
        if (request.getRequirements() != null) {
            job.setRequirements(request.getRequirements());
        }
        if (request.getEmploymentType() != null) {
            job.setEmploymentType(request.getEmploymentType());
        }
        if (request.getWorkMode() != null) {
            job.setWorkMode(request.getWorkMode());
        }
        if (request.getSalaryMin() != null) {
            job.setSalaryMin(request.getSalaryMin());
        }
        if (request.getSalaryMax() != null) {
            job.setSalaryMax(request.getSalaryMax());
        }
        if (StringUtils.hasText(request.getSalaryCurrency())) {
            job.setSalaryCurrency(request.getSalaryCurrency().trim());
        }
        if (request.getLocationState() != null) {
            job.setLocationState(request.getLocationState());
        }
        if (request.getLocationLga() != null) {
            job.setLocationLga(request.getLocationLga());
        }
    }

    public JobResponse toResponse(Job job) {
        return toResponse(job, null);
    }

    public JobResponse toResponse(Job job, Boolean saved) {
        Company company = job.getCompany();
        return JobResponse.builder()
                .id(job.getId())
                .companyId(company.getId())
                .companyName(company.getName())
                .title(job.getTitle())
                .description(job.getDescription())
                .requirements(job.getRequirements())
                .employmentType(job.getEmploymentType())
                .workMode(job.getWorkMode())
                .salaryMin(job.getSalaryMin())
                .salaryMax(job.getSalaryMax())
                .salaryCurrency(job.getSalaryCurrency())
                .locationState(job.getLocationState())
                .locationLga(job.getLocationLga())
                .status(job.getStatus())
                .publishedAt(job.getPublishedAt())
                .closedAt(job.getClosedAt())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .saved(saved)
                .build();
    }
}
