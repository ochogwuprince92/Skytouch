package com.backend.Skytouch.interview.service;

import com.backend.Skytouch.application.entity.JobApplication;
import com.backend.Skytouch.application.repository.JobApplicationRepository;
import com.backend.Skytouch.common.apimodel.PageResponse;
import com.backend.Skytouch.common.enums.ApplicationStatus;
import com.backend.Skytouch.common.enums.InterviewStatus;
import com.backend.Skytouch.common.enums.UserRole;
import com.backend.Skytouch.common.exception.BadRequestException;
import com.backend.Skytouch.common.exception.ConflictException;
import com.backend.Skytouch.common.exception.ResourceNotFoundException;
import com.backend.Skytouch.common.mapper.InterviewMapper;
import com.backend.Skytouch.common.util.PaginationUtils;
import com.backend.Skytouch.company.entity.Company;
import com.backend.Skytouch.company.service.CompanyService;
import com.backend.Skytouch.interview.apimodel.InterviewCreateRequest;
import com.backend.Skytouch.interview.apimodel.InterviewResponse;
import com.backend.Skytouch.interview.apimodel.InterviewUpdateRequest;
import com.backend.Skytouch.interview.entity.Interview;
import com.backend.Skytouch.interview.repository.InterviewRepository;
import com.backend.Skytouch.notification.service.NotificationService;
import com.backend.Skytouch.user.entity.Users;
import com.backend.Skytouch.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InterviewService {

    private static final Set<ApplicationStatus> SCHEDULABLE_STATUSES = EnumSet.of(ApplicationStatus.SHORTLISTED);

    private final InterviewRepository interviewRepository;
    private final JobApplicationRepository applicationRepository;
    private final InterviewMapper interviewMapper;
    private final CompanyService companyService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public InterviewResponse schedule(String employerEmail, UUID applicationId, InterviewCreateRequest request) {
        JobApplication application = getEmployerApplication(employerEmail, applicationId);
        if (!SCHEDULABLE_STATUSES.contains(application.getStatus())) {
            throw new BadRequestException("Interviews can only be scheduled for shortlisted applications");
        }
        if (interviewRepository.findByApplication_Id(applicationId).isPresent()) {
            throw new ConflictException("An interview is already scheduled for this application");
        }

        Users employerUser = userRepository.findByEmailAndRole(employerEmail, UserRole.EMPLOYER)
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found: " + employerEmail));

        Interview interview = Interview.builder()
                .application(application)
                .scheduledAt(request.getScheduledAt())
                .durationMinutes(request.getDurationMinutes() > 0 ? request.getDurationMinutes() : 60)
                .mode(request.getMode())
                .locationOrLink(request.getLocationOrLink())
                .status(InterviewStatus.SCHEDULED)
                .notes(request.getNotes())
                .createdBy(employerUser)
                .build();

        application.setStatus(ApplicationStatus.INTERVIEW_SCHEDULED);
        applicationRepository.save(application);

        Interview saved = interviewRepository.save(interview);
        notificationService.notifyOnInterviewScheduled(saved);
        return interviewMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public InterviewResponse findForApplication(String email, UUID applicationId) {
        JobApplication application = getParticipantApplication(email, applicationId);
        Interview interview = interviewRepository.findByApplication_Id(application.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Interview not found for application: " + applicationId));
        return interviewMapper.toResponse(interview);
    }

    @Transactional
    public InterviewResponse update(String employerEmail, UUID interviewId, InterviewUpdateRequest request) {
        Interview interview = getEmployerInterview(employerEmail, interviewId);
        boolean rescheduled = false;

        if (request.getScheduledAt() != null) {
            interview.setScheduledAt(request.getScheduledAt());
            rescheduled = true;
        }
        if (request.getDurationMinutes() != null) {
            interview.setDurationMinutes(request.getDurationMinutes());
        }
        if (request.getMode() != null) {
            interview.setMode(request.getMode());
        }
        if (request.getLocationOrLink() != null) {
            interview.setLocationOrLink(request.getLocationOrLink());
        }
        if (request.getNotes() != null) {
            interview.setNotes(request.getNotes());
        }
        if (request.getStatus() != null) {
            interview.setStatus(request.getStatus());
        }

        Interview saved = interviewRepository.save(interview);
        if (rescheduled || request.getStatus() != null) {
            notificationService.notifyOnInterviewUpdated(saved);
        }
        return interviewMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<InterviewResponse> findMyUpcoming(String seekerEmail, int page, int size) {
        Pageable pageable = PaginationUtils.pageable(page, size, Sort.by(Sort.Direction.ASC, "scheduledAt"));
        Page<Interview> results = interviewRepository.findUpcomingForSeeker(
                seekerEmail, InterviewStatus.SCHEDULED, LocalDateTime.now(), pageable);
        return PaginationUtils.mapPage(results, interviewMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public long countUpcomingForSeeker(String seekerEmail) {
        return interviewRepository.countUpcomingForSeeker(
                seekerEmail, InterviewStatus.SCHEDULED, LocalDateTime.now());
    }

    private Interview getEmployerInterview(String employerEmail, UUID interviewId) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found: " + interviewId));
        getEmployerApplication(employerEmail, interview.getApplication().getId());
        return interview;
    }

    private JobApplication getEmployerApplication(String employerEmail, UUID applicationId) {
        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));
        Company company = companyService.getLinkedCompany(employerEmail);
        if (!application.getJob().getCompany().getId().equals(company.getId())) {
            throw new ResourceNotFoundException("Application not found: " + applicationId);
        }
        return application;
    }

    private JobApplication getParticipantApplication(String email, UUID applicationId) {
        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));

        if (application.getJobSeeker().getUser().getEmail().equals(email)) {
            return application;
        }

        try {
            Company company = companyService.getLinkedCompany(email);
            if (application.getJob().getCompany().getId().equals(company.getId())) {
                return application;
            }
        } catch (BadRequestException | ResourceNotFoundException ignored) {
            // not an employer on this application
        }

        throw new ResourceNotFoundException("Application not found: " + applicationId);
    }
}
