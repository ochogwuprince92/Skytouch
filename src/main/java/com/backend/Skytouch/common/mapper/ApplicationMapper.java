package com.backend.Skytouch.common.mapper;

import com.backend.Skytouch.application.apimodel.ApplicationResponse;
import com.backend.Skytouch.application.entity.JobApplication;
import com.backend.Skytouch.common.enums.ApplicationStatus;
import com.backend.Skytouch.job.entity.Job;
import com.backend.Skytouch.jobseeker.entity.JobSeeker;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ApplicationMapper {

    public JobApplication toEntity(Job job, JobSeeker jobSeeker, String coverLetter) {
        return JobApplication.builder()
                .job(job)
                .jobSeeker(jobSeeker)
                .status(ApplicationStatus.SUBMITTED)
                .coverLetter(coverLetter)
                .cvUrl(jobSeeker.getCvUrl())
                .seekerName(buildSeekerName(jobSeeker))
                .build();
    }

    public ApplicationResponse toResponse(JobApplication application) {
        Job job = application.getJob();
        JobSeeker seeker = application.getJobSeeker();
        return ApplicationResponse.builder()
                .id(application.getId())
                .jobId(job.getId())
                .jobTitle(job.getTitle())
                .companyId(job.getCompany().getId())
                .companyName(job.getCompany().getName())
                .jobSeekerId(seeker.getId())
                .seekerName(application.getSeekerName())
                .seekerEmail(seeker.getUser().getEmail())
                .status(application.getStatus())
                .coverLetter(application.getCoverLetter())
                .cvUrl(application.getCvUrl())
                .appliedAt(application.getAppliedAt())
                .updatedAt(application.getUpdatedAt())
                .build();
    }

    private String buildSeekerName(JobSeeker seeker) {
        String first = seeker.getFirstName();
        String last = seeker.getLastName();
        if (StringUtils.hasText(first) && StringUtils.hasText(last)) {
            return first.trim() + " " + last.trim();
        }
        if (StringUtils.hasText(first)) {
            return first.trim();
        }
        return seeker.getUser().getEmail();
    }
}
