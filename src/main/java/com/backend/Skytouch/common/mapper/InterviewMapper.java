package com.backend.Skytouch.common.mapper;

import com.backend.Skytouch.application.entity.JobApplication;
import com.backend.Skytouch.interview.apimodel.InterviewResponse;
import com.backend.Skytouch.interview.entity.Interview;
import com.backend.Skytouch.job.entity.Job;
import org.springframework.stereotype.Component;

@Component
public class InterviewMapper {

    public InterviewResponse toResponse(Interview interview) {
        JobApplication application = interview.getApplication();
        Job job = application.getJob();
        return InterviewResponse.builder()
                .id(interview.getId())
                .applicationId(application.getId())
                .jobTitle(job.getTitle())
                .companyName(job.getCompany().getName())
                .seekerName(application.getSeekerName())
                .scheduledAt(interview.getScheduledAt())
                .durationMinutes(interview.getDurationMinutes())
                .mode(interview.getMode())
                .locationOrLink(interview.getLocationOrLink())
                .status(interview.getStatus())
                .notes(interview.getNotes())
                .createdAt(interview.getCreatedAt())
                .build();
    }
}
